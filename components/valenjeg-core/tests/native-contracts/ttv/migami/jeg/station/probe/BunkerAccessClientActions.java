package ttv.migami.jeg.station.probe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Fixture-only movement and look. KeyMapping.setDown and LocalPlayer.turn; no OS focus, cursor warp, or teleport. */
final class BunkerAccessClientActions {
    private static final Path CLIENT = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/client");
    private static final UUID PROBE = UUID.fromString("08d8976e-e59a-3751-ac2c-140b28fb5309");
    private static final double MAX_STEP = 1.5;
    private static final double MAX_TARGET = 6.0;
    private static int tick, releaseAt, levelFrames;
    private static boolean forward, back, left, right, jump, sneak, look, held;
    private static double yawPerTick, pitchPerTick, targetX, targetY, targetZ, originX, originY, originZ, previousX, previousY, previousZ, maxStep;
    private static boolean toward, survivalEntireHold, consistent;
    private static String observation = "", releaseReason = "";
    private static int samples;

    private BunkerAccessClientActions() {}

    static void register() {
        if (!Boolean.getBoolean("valen.fixture.bunkerAccess")) return;
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> levelFrames++);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            tick++;
            if (!held) return;
            try {
                guard(client);
                if (client.gui.screen() != null) {
                    release(client, "screen_cleanup");
                    return;
                }
                if (client.gameMode == null || client.gameMode.getPlayerMode() != GameType.SURVIVAL) {
                    survivalEntireHold = false;
                    release(client, "nonsurvival_cleanup");
                    return;
                }
                if (tick >= releaseAt) {
                    release(client, "bounded_release");
                    return;
                }
                if (look) applyLook(client);
                double dx = client.player.getX() - previousX, dy = client.player.getY() - previousY, dz = client.player.getZ() - previousZ;
                double step = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (samples > 0) maxStep = Math.max(maxStep, step);
                if (step > MAX_STEP) {
                    consistent = false;
                    release(client, "step_exceeds_walk");
                    return;
                }
                previousX = client.player.getX();
                previousY = client.player.getY();
                previousZ = client.player.getZ();
                samples++;
            } catch (Throwable failure) {
                release(client, "guard_cleanup");
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> release(client, "disconnect_cleanup"));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> release(client, "client_stop_cleanup"));
    }

    static void action(Minecraft client, JsonObject request, JsonObject result) {
        guard(client);
        String action = request.get("action").getAsString();
        if (action.equals("bunker_state")) {
            result.addProperty("action_result", "read-only native movement, game-mode and frame acknowledgement");
            state(client, result);
            return;
        }
        if (action.equals("bunker_release")) {
            release(client, "requested_cleanup");
            result.addProperty("action_result", "owned movement and look released");
            state(client, result);
            return;
        }
        if (client.gui.screen() != null) throw new IllegalStateException("Bunker movement requires the world screen");
        if (client.gameMode == null || client.gameMode.getPlayerMode() != GameType.SURVIVAL)
            throw new IllegalStateException("Survival is required; a spectator or creative teleport is not a walk");
        if (held || client.options.keyUp.isDown() || client.options.keyDown.isDown() || client.options.keyLeft.isDown()
                || client.options.keyRight.isDown() || client.options.keyJump.isDown() || client.options.keyShift.isDown())
            throw new IllegalStateException("A movement input is already held");
        int duration = request.get("ticks").getAsInt();
        if (duration < 1 || duration > 80) throw new IllegalArgumentException("Bounded hold must be 1..80 ticks");
        boolean move = action.equals("bunker_move");
        if (!move && !action.equals("bunker_look")) throw new IllegalArgumentException("Unknown bunker access action " + action);
        JsonArray keys = request.has("keys") ? request.getAsJsonArray("keys") : new JsonArray();
        if (move && keys.isEmpty()) throw new IllegalArgumentException("bunker_move requires at least one movement key");
        if (!move && !keys.isEmpty()) throw new IllegalArgumentException("bunker_look cannot hold movement keys");
        boolean wantForward = false, wantBack = false, wantLeft = false, wantRight = false, wantJump = false, wantSneak = false;
        for (var element : keys) {
            switch (element.getAsString()) {
                case "forward" -> wantForward = true;
                case "back" -> wantBack = true;
                case "left" -> wantLeft = true;
                case "right" -> wantRight = true;
                case "jump" -> wantJump = true;
                case "sneak" -> {
                    if (client.options.toggleCrouch().get()) throw new IllegalStateException("Sneak hold requires toggleCrouch off");
                    wantSneak = true;
                }
                default -> throw new IllegalArgumentException("Movement key is not forward/back/left/right/jump/sneak");
            }
        }
        boolean hasToward = request.has("toward");
        double lookYaw = 0, lookPitch = 0;
        if (hasToward) {
            JsonObject at = request.getAsJsonObject("toward");
            targetX = at.get("x").getAsDouble();
            targetY = at.get("y").getAsDouble();
            targetZ = at.get("z").getAsDouble();
            if (!Double.isFinite(targetX) || !Double.isFinite(targetY) || !Double.isFinite(targetZ))
                throw new IllegalArgumentException("Look target must be finite");
            double dx = targetX - client.player.getX(), dy = targetY - client.player.getY(), dz = targetZ - client.player.getZ();
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) > MAX_TARGET)
                throw new IllegalArgumentException("Look/move target is farther than one stair step");
        } else if (action.equals("bunker_look")) {
            lookYaw = request.get("yaw_per_tick").getAsDouble();
            lookPitch = request.get("pitch_per_tick").getAsDouble();
            if (!Double.isFinite(lookYaw) || !Double.isFinite(lookPitch) || Math.abs(lookYaw) > 20 || Math.abs(lookPitch) > 20)
                throw new IllegalArgumentException("Look input per tick must stay within ±20 before LocalPlayer.turn scales it");
        } else if (request.has("yaw_per_tick") || request.has("pitch_per_tick")) {
            throw new IllegalArgumentException("bunker_move look uses toward, the source stair or door stand position");
        }
        observation = request.has("observation_id") ? request.get("observation_id").getAsString() : UUID.randomUUID().toString();
        forward = wantForward; back = wantBack; left = wantLeft; right = wantRight; jump = wantJump; sneak = wantSneak;
        toward = hasToward; yawPerTick = lookYaw; pitchPerTick = lookPitch; look = hasToward || action.equals("bunker_look");
        originX = previousX = client.player.getX(); originY = previousY = client.player.getY(); originZ = previousZ = client.player.getZ();
        maxStep = 0; samples = 0; survivalEntireHold = true; consistent = true; releaseReason = "";
        releaseAt = tick + duration + 1; held = true;
        if (forward) client.options.keyUp.setDown(true);
        if (back) client.options.keyDown.setDown(true);
        if (left) client.options.keyLeft.setDown(true);
        if (right) client.options.keyRight.setDown(true);
        if (jump) client.options.keyJump.setDown(true);
        if (sneak) client.options.keyShift.setDown(true);
        result.addProperty("action_result", "native movement/look queued; key state and LocalPlayer.turn are not survival-walk acceptance");
        state(client, result);
        event(client, "native_input_queued");
    }

    private static void applyLook(Minecraft client) {
        double yawArg, pitchArg;
        if (toward) {
            Vec3 eye = client.player.getEyePosition();
            double dx = targetX - eye.x, dy = targetY - eye.y, dz = targetZ - eye.z;
            float desiredYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float desiredPitch = (float)Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            yawArg = Mth.clamp(Mth.wrapDegrees(desiredYaw - client.player.getYRot()) / 0.15F, -20.0F, 20.0F);
            pitchArg = Mth.clamp((desiredPitch - client.player.getXRot()) / 0.15F, -20.0F, 20.0F);
        } else {
            yawArg = yawPerTick;
            pitchArg = pitchPerTick;
        }
        client.player.turn(yawArg, pitchArg);
    }

    private static void guard(Minecraft client) {
        Path game, expected;
        try {
            game = client.gameDirectory.toPath().toRealPath();
            expected = CLIENT.toRealPath();
        } catch (Exception failure) {
            throw new IllegalStateException("Bunker access cannot resolve the client directory", failure);
        }
        if (!Boolean.getBoolean("valen.fixture.bunkerAccess") || !game.equals(expected) || !Files.isRegularFile(game.resolve("../preparation.json"))
                || !client.getUser().getName().equals("ValenPackProbe") || client.player == null || !client.player.isAlive()
                || !client.player.getName().getString().equals("ValenPackProbe") || !client.player.getUUID().equals(PROBE)
                || client.getConnection() == null)
            throw new IllegalStateException("Bunker access requires the explicit alive synthetic copied-fixture client");
        String remote = client.getConnection().getConnection().getRemoteAddress().toString();
        if (!remote.contains("127.0.0.1") || !remote.endsWith(":25589"))
            throw new IllegalStateException("Bunker access refuses any connection except fixture 127.0.0.1:25589");
    }

    private static void release(Minecraft client, String reason) {
        boolean was = held;
        if (client != null && client.options != null) {
            if (forward) client.options.keyUp.setDown(false);
            if (back) client.options.keyDown.setDown(false);
            if (left) client.options.keyLeft.setDown(false);
            if (right) client.options.keyRight.setDown(false);
            if (jump) client.options.keyJump.setDown(false);
            if (sneak) client.options.keyShift.setDown(false);
        }
        forward = back = left = right = jump = sneak = look = held = false;
        releaseAt = 0;
        releaseReason = reason;
        if (was && client != null && client.player != null && toward) {
            double dx = client.player.getX() - targetX, dy = client.player.getY() - targetY, dz = client.player.getZ() - targetZ;
            double end = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double sx = client.player.getX() - originX, sy = client.player.getY() - originY, sz = client.player.getZ() - originZ;
            double start = Math.sqrt((originX - targetX) * (originX - targetX) + (originY - targetY) * (originY - targetY) + (originZ - targetZ) * (originZ - targetZ));
            if (!(end + 0.05 < start) || Math.sqrt(sx * sx + sy * sy + sz * sz) < 0.05) consistent = false;
        }
        if (was) event(client, reason);
    }

    private static void event(Minecraft client, String phase) {
        try {
            JsonObject receipt = new JsonObject();
            receipt.addProperty("phase", phase);
            state(client, receipt);
            Files.writeString(client.gameDirectory.toPath().resolve("valen-bunker-access.jsonl"), receipt + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception failure) {
            failure.printStackTrace();
        }
    }

    private static void state(Minecraft client, JsonObject result) {
        JsonObject state = new JsonObject();
        state.addProperty("observation_id", observation);
        state.addProperty("owned_hold", held);
        JsonObject keys = new JsonObject();
        keys.addProperty("forward", client.options.keyUp.isDown());
        keys.addProperty("back", client.options.keyDown.isDown());
        keys.addProperty("left", client.options.keyLeft.isDown());
        keys.addProperty("right", client.options.keyRight.isDown());
        keys.addProperty("jump", client.options.keyJump.isDown());
        keys.addProperty("sneak", client.options.keyShift.isDown());
        state.add("actual_native_keys", keys);
        state.addProperty("release_at_client_tick", releaseAt);
        state.addProperty("release_reason", releaseReason);
        state.addProperty("client_tick", tick);
        state.addProperty("native_level_frames", levelFrames);
        state.addProperty("native_frame_time_ns", client.getFrameTimeNs());
        state.addProperty("native_fps", client.getFps());
        state.addProperty("perspective", client.options.getCameraType().name());
        state.addProperty("screen", client.gui.screen() == null ? "world" : client.gui.screen().getClass().getName());
        state.addProperty("client_game_mode", client.gameMode == null ? "none" : client.gameMode.getPlayerMode().getName());
        state.addProperty("samples", samples);
        state.addProperty("max_single_tick_displacement", maxStep);
        state.addProperty("segment_consistent_with_walk", !held && "bounded_release".equals(releaseReason) && consistent && survivalEntireHold && samples > 0 && maxStep <= MAX_STEP);
        state.addProperty("spiral_walk_proved", false);
        state.addProperty("proof", "Client key and look trace only. Authoritative survival position, game mode, and server tick still require bunker_ack. Teleports are not walk proof.");
        if (client.player != null) {
            state.addProperty("x", client.player.getX());
            state.addProperty("y", client.player.getY());
            state.addProperty("z", client.player.getZ());
            state.addProperty("yaw", client.player.getYRot());
            state.addProperty("pitch", client.player.getXRot());
            state.addProperty("client_level_game_time", client.level == null ? -1 : client.level.getGameTime());
        }
        result.add("bunker_access", state);
    }
}
