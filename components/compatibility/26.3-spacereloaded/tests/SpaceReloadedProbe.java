package valen.spacereloadedprobe;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import dev.valen.compat.nerospace.PackRecipeDisplay;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Disposable registry/recipe proof; never packaged in a production mod. */
public final class SpaceReloadedProbe implements ModInitializer {
    private int ticks;
    private boolean completed;
    @Override public void onInitialize() {
        if (!Boolean.getBoolean("valen.spacereloaded.probe")) throw new IllegalStateException("Disposable probe flag required");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticks < 100 || completed) return;
            if (Boolean.getBoolean("valen.gravity.probe") && server.overworld().getGameTime() % 10 != 0) return;
            completed = true;
            Map<String,Object> result = new LinkedHashMap<>();
            try {
                boolean expected = Boolean.getBoolean("valen.spacereloaded.expected");
                var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                List<String> features = new ArrayList<>();
                for (var entry : server.registryAccess().lookupOrThrow(Registries.FEATURE).entrySet()) {
                    if (!entry.getKey().identifier().getNamespace().equals("spacereloaded")) continue;
                    var json = Feature.DIRECT_CODEC.encodeStart(ops, entry.getValue()).getOrThrow();
                    var restored = Feature.DIRECT_CODEC.parse(ops, json).getOrThrow();
                    require(restored.codec() == entry.getValue().codec(), "Feature codec identity " + entry.getKey());
                    features.add(entry.getKey().identifier().toString());
                }
                require(features.size() == (expected ? 18 : 0), "Feature count: " + features.size());
                Map<String,PackRecipeDisplay> displays = new TreeMap<>();
                ServerDisplayRegistry.getInstance().getAll().forEach((category, values) -> {
                    if (category.toString().startsWith("spacereloaded:"))
                        for (var display : values) {
                            require(display instanceof PackRecipeDisplay, "Unexpected display " + display.getClass());
                            displays.put(display.getDisplayLocation().orElseThrow().toString(), (PackRecipeDisplay) display);
                        }
                });
                Map<String,Integer> types = new TreeMap<>();
                for (var holder : server.getRecipeManager().getRecipes()) {
                    var type = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
                    if (!type.getNamespace().equals("spacereloaded")) continue;
                    types.merge(type.toString(), 1, Integer::sum);
                    var id = holder.id().identifier().toString();
                    var display = displays.get(id);
                    require(display != null, "Missing server display: " + id);
                    var json = PackRecipeDisplay.SERIALIZER.codec().codec().encodeStart(ops, display).getOrThrow();
                    var jsonRestored = PackRecipeDisplay.SERIALIZER.codec().codec().parse(ops, json).getOrThrow();
                    require(json.equals(PackRecipeDisplay.SERIALIZER.codec().codec().encodeStart(ops, jsonRestored).getOrThrow()), "JSON roundtrip " + id);
                    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
                    try {
                        PackRecipeDisplay.SERIALIZER.streamCodec().encode(buffer, display);
                        var restored = PackRecipeDisplay.SERIALIZER.streamCodec().decode(buffer);
                        require(buffer.readableBytes() == 0, "Unread network bytes " + id);
                        require(json.equals(PackRecipeDisplay.SERIALIZER.codec().codec().encodeStart(ops, restored).getOrThrow()), "Network roundtrip " + id);
                    } finally { buffer.release(); }
                }
                require(types.size() == (expected ? 6 : 0), "Recipe families " + types);
                require(displays.size() == types.values().stream().mapToInt(Integer::intValue).sum(), "Display count mismatch");
                List<String> dimensions = new ArrayList<>();
                for (var level : server.getAllLevels()) if (level.dimension().identifier().getNamespace().equals("spacereloaded"))
                    dimensions.add(level.dimension().identifier().toString());
                require(dimensions.size() == (expected ? 4 : 0), "Dimension count " + dimensions);
                if (Boolean.getBoolean("valen.gravity.probe")) result.put("gravity", SpaceGravityProbe.run(server));
                if (Boolean.getBoolean("valen.gravity.absence")) result.put("gravity_absence", checkGravityAbsence(server));
                result.put("pass", true);
                result.put("expected_spacereloaded", expected);
                result.put("feature_codec_roundtrips", features);
                result.put("recipe_family_counts", types);
                result.put("synced_display_json_and_network_roundtrips", displays.size());
                result.put("dimensions", dimensions);
            } catch (Throwable failure) {
                result.put("pass", false);result.put("error",failure.toString());failure.printStackTrace();
            }
            try { Files.writeString(Path.of("spacereloaded-contracts.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n"); }
            catch (Exception e) { throw new RuntimeException(e); }
            System.out.println("SPACERELOADED_CONTRACTS " + result);
        });
    }
    private static Map<String,Object> checkGravityAbsence(net.minecraft.server.MinecraftServer server) {
        require(!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("spacereloaded"), "Space must be absent");
        var profile = new com.mojang.authlib.GameProfile(UUID.nameUUIDFromBytes("GravityAbsent".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "GravityAbsent");
        var player = new net.minecraft.server.level.ServerPlayer(server, server.overworld(), profile,
            net.minecraft.server.level.ClientInformation.createDefault());
        var attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.GRAVITY);
        var other = net.minecraft.resources.Identifier.parse("valen_probe:absence_other");
        attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(other, 0.25,
            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        dev.valen.compat.energy.mixin.NeroGravityInvoker.valen$apply(player, 0.4);
        require(Math.abs(attribute.getValue() - attribute.getBaseValue() * 0.4 * 1.25) < 1e-9,
            "Nero's transformed updater retains supplied factor without Space");
        dev.valen.compat.energy.GravityBridge.refresh(player);
        double ambient = za.co.neroland.nerospace.world.gravity.GravityManager.factorAt(player.level(), player.blockPosition());
        require(Math.abs(attribute.getValue() - attribute.getBaseValue() * ambient * 1.25) < 1e-9,
            "Nero native ambient refresh works without optional Space classes");
        require(attribute.getModifier(other) != null, "Unrelated modifier survives optional absence");
        za.co.neroland.nerospace.platform.Services.PLATFORM.setOxygen(player, 7);
        za.co.neroland.nerospace.world.OxygenManager.tick(player);
        require(za.co.neroland.nerospace.platform.Services.PLATFORM.getOxygen(player)
                == za.co.neroland.nerospace.world.OxygenManager.maxOxygenFor(player),
                "Transformed oxygen tick works without optional Space classes");
        return Map.of("pass", true, "transformed_updater_executed", true, "ambient_factor", ambient,
                "transformed_oxygen_tick_executed", true);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
