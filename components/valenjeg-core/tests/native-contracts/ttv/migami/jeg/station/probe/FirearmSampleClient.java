package ttv.migami.jeg.station.probe;

import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import ttv.migami.jeg.client.GunRecoilHandler;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.GunItem;

/** Read-only per-tick firearm sample. It never fires, edits NBT, or changes a model. */
final class FirearmSampleClient {
    private static final Path GAME = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/client");
    private static final UUID PROBE = UUID.fromString("08d8976e-e59a-3751-ac2c-140b28fb5309");
    private static boolean registered;
    private static Session session;
    private static int renders;

    private FirearmSampleClient() {}

    static void register() {
        if (registered) return;
        registered = true;
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> {
            renders++;
            Session current = session;
            if (current != null && current.signalTick >= 0 && renders > current.signalFrame) current.captureDue = true;
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Session current = session;
            if (current == null || !current.capture || !current.captureDue || current.captureRequested) return;
            try {
                guard(client);
                Path inbox = client.gameDirectory.toPath().resolve("valen-capture-request.txt");
                if (Files.exists(inbox)) {
                    current.captureBlocked = true;
                    return;
                }
                Files.writeString(inbox, current.captureName, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                current.captureRequested = true;
                current.captureAfterRender = renders;
            } catch (Throwable failure) {
                current.failure = failure.toString();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Session current = session;
            if (current == null) return;
            try {
                guard(client);
                JsonObject row = sample(client, current.ticks);
                current.log(row);
                boolean pump = row.get("cooldown_percent").getAsFloat() > 0.0F;
                boolean recoil = row.get("recoil").getAsFloat() > 0.0F || row.get("recoil_normal").getAsDouble() > 0.0D;
                boolean dynamic = pump && recoil;
                if (dynamic && current.signalTick < 0) {
                    current.signalTick = current.ticks;
                    current.signalFrame = renders;
                }
                current.ticks++;
                if (current.failure != null || current.ticks >= current.limit) finish(current);
            } catch (Throwable failure) {
                current.failure = failure.toString();
                finish(current);
            }
        });
    }

    static void start(Minecraft client, JsonObject request, JsonObject result) throws Exception {
        guard(client);
        if (session != null) throw new IllegalStateException("A firearm sample is already active");
        if (client.gui.screen() != null) throw new IllegalStateException("Firearm sample requires the world screen");
        String id = request.get("sample_id").getAsString();
        if (!id.matches("[a-z0-9]{8,24}")) throw new IllegalArgumentException("sample_id must be a short lowercase token");
        int ticks = request.get("ticks").getAsInt();
        if (ticks < 1 || ticks > 40) throw new IllegalArgumentException("Firearm sample is bounded to 1..40 native ticks");
        if (request.has("release_reload_after_one") && request.get("release_reload_after_one").getAsBoolean())
            throw new IllegalArgumentException("Reload is not stopped by a hotbar click");
        Session created = new Session(id, ticks, request.has("capture") && request.get("capture").getAsBoolean());
        if (Files.exists(created.log) || Files.exists(created.summary) || Files.exists(GAME.resolve("screenshots").resolve(created.captureName)))
            throw new IllegalStateException("Firearm sample id already has preserved outputs");
        var stack = client.player.getMainHandItem();
        if (client.player.getCooldowns().getCooldownPercent(stack, 0.0F) > 0.0F || GunRecoilHandler.current() > 0.0F || GunRecoilHandler.getGunRecoilNormal() > 0.0D)
            throw new IllegalStateException("Firearm sample requires a settled cooldown and recoil baseline");
        created.models = modelHashes();
        session = created;
        result.addProperty("action_result", "firearm sample armed; it does not fire or reload");
        result.addProperty("sample_id", id);
        result.addProperty("capture_name", created.captureName);
        result.add("model_hashes", created.models);
    }

    private static void finish(Session current) {
        if (session != current) return;
        session = null;
        JsonObject done = new JsonObject();
        done.addProperty("sample_id", current.id);
        done.addProperty("ticks", current.ticks);
        done.addProperty("signal_tick", current.signalTick);
        done.addProperty("signal_render_frame", current.signalFrame);
        done.addProperty("capture_requested", current.captureRequested);
        done.addProperty("capture_after_render_frame", current.captureAfterRender);
        done.addProperty("capture_blocked", current.captureBlocked);
        if (current.failure != null) done.addProperty("error", current.failure);
        done.add("model_hashes", current.models);
        try {
            Files.writeString(current.summary, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(done));
        } catch (Exception failure) {
            failure.printStackTrace();
        }
    }

    private static JsonObject sample(Minecraft client, int index) {
        var stack = client.player.getMainHandItem();
        JsonObject row = new JsonObject();
        row.addProperty("tick", index);
        row.addProperty("renders_completed", renders);
        row.addProperty("game_time", client.level.getGameTime());
        row.addProperty("held_item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        row.addProperty("selected_slot", client.player.getInventory().getSelectedSlot());
        row.addProperty("gun_ammo", stack.getOrDefault(ModDataComponents.GUN_AMMO.get(), 0));
        row.addProperty("cooldown_percent", client.player.getCooldowns().getCooldownPercent(stack, 0.0F));
        row.addProperty("recoil", GunRecoilHandler.current());
        row.addProperty("recoil_normal", GunRecoilHandler.getGunRecoilNormal());
        row.addProperty("recoil_angle", GunRecoilHandler.getGunRecoilAngle());
        row.addProperty("recoil_kick", GunRecoilHandler.getGunRecoilKick());
        row.addProperty("reload_ticks_remaining", stack.getOrDefault(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get(), 0));
        row.addProperty("reload_ticks_total", stack.getOrDefault(ModDataComponents.GUN_RELOAD_TICKS_TOTAL.get(), 0));
        row.addProperty("main_hand_gun", stack.getItem() instanceof GunItem);
        row.addProperty("x", client.player.getX());
        row.addProperty("y", client.player.getY());
        row.addProperty("z", client.player.getZ());
        row.addProperty("yaw", client.player.getYRot());
        row.addProperty("pitch", client.player.getXRot());
        return row;
    }


    private static JsonObject modelHashes() throws Exception {
        JsonObject hashes = new JsonObject();
        Path root = GAME.resolve("player_models");
        try (var files = Files.list(root)) {
            for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".cpmmodel")).sorted().toList()) {
                hashes.addProperty(file.getFileName().toString(), sha(file));
            }
        }
        if (hashes.size() != 6) throw new IllegalStateException("Expected the six approved runtime models");
        return hashes;
    }

    private static void guard(Minecraft client) throws Exception {
        Path game = client.gameDirectory.toPath().toRealPath();
        if (!Boolean.getBoolean("valen.fixture.weaponControls") || !game.equals(GAME.toRealPath()) || !Files.isRegularFile(game.resolve("../preparation.json"))
                || !client.getUser().getName().equals("ValenPackProbe") || client.player == null || !client.player.isAlive()
                || !client.player.getUUID().equals(PROBE) || !client.player.getName().getString().equals("ValenPackProbe") || client.getConnection() == null)
            throw new IllegalStateException("Firearm sample requires the alive synthetic copied fixture");
        String remote = client.getConnection().getConnection().getRemoteAddress().toString();
        if (!remote.contains("127.0.0.1") || !remote.endsWith(":25589")) throw new IllegalStateException("Firearm sample refuses a non-loopback connection");
    }

    private static String sha(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var in = Files.newInputStream(path)) {
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static final class Session {
        final String id;
        final int limit;
        final boolean capture;
        final String captureName;
        final Path log;
        final Path summary;
        int ticks;
        int signalTick = -1;
        int signalFrame;
        int captureAfterRender = -1;
        boolean captureDue;
        boolean captureRequested;
        boolean captureBlocked;
        String failure;
        JsonObject models = new JsonObject();

        Session(String id, int limit, boolean capture) {
            this.id = id;
            this.limit = limit;
            this.capture = capture;
            this.captureName = "firearm-" + id + ".png";
            Path dir = GAME.resolve("valen-firearm-samples");
            this.log = dir.resolve(id + ".jsonl");
            this.summary = dir.resolve(id + ".json");
        }

        void log(JsonObject row) throws Exception {
            Files.createDirectories(log.getParent());
            Files.writeString(log, row + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }
}
