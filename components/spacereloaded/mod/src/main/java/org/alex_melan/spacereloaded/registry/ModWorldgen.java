package org.alex_melan.spacereloaded.registry;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.alex_melan.spacereloaded.SpaceReloaded;
import org.alex_melan.spacereloaded.worldgen.CrashedProbeFeature;
import org.alex_melan.spacereloaded.worldgen.LavaTubeFeature;
import org.alex_melan.spacereloaded.worldgen.CraterFeature;

/**
 * Генерация руд (T040): configured/placed features лежат датапаком в
 * data/spacereloaded/worldgen/, сюда — только привязка к биомам Оверворлда.
 */
public final class ModWorldgen {

    public static final ResourceKey<PlacedFeature> ORE_TITANIUM = placed("ore_titanium");
    public static final ResourceKey<PlacedFeature> ORE_TUNGSTEN = placed("ore_tungsten");
    public static final ResourceKey<PlacedFeature> OIL_SHALE = placed("oil_shale");
    /** 006: эвапориты (галит), гидротермальный флюорит, пегматитовый сподумен. */
    public static final ResourceKey<PlacedFeature> ORE_HALITE = placed("ore_halite");
    public static final ResourceKey<PlacedFeature> ORE_FLUORITE = placed("ore_fluorite");
    public static final ResourceKey<PlacedFeature> ORE_SPODUMENE = placed("ore_spodumene");
    /** 008: пегматитовый берилл, эвапоритовая бура, урановая смолка в глубинном сланце. */
    public static final ResourceKey<PlacedFeature> ORE_BERYL = placed("ore_beryl");
    public static final ResourceKey<PlacedFeature> ORE_BORAX = placed("ore_borax");
    public static final ResourceKey<PlacedFeature> ORE_URANINITE = placed("ore_uraninite");
    public static final ResourceKey<PlacedFeature> ORE_GALENA = placed("ore_galena");

    /** Ударный кратер безатмосферного тела: рельеф, а не разрушение. */
    public static final MapCodec<CraterFeature> CRATER = CraterFeature.CODEC;

    /** Лавовые трубки Луны (004): chunk-local вырезание детерминированных трубок регионов. */
    public static final MapCodec<LavaTubeFeature> LAVA_TUBE = LavaTubeFeature.CODEC;

    /** Место крушения зонда (004): кратер, обломки, контейнер с добычей. */
    public static final MapCodec<CrashedProbeFeature> CRASHED_PROBE = CrashedProbeFeature.CODEC;

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(SpaceReloaded.MOD_ID, name));
    }

    public static void init() {
        net.minecraft.core.Registry.register(
                net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(SpaceReloaded.MOD_ID, "crater"), CRATER);
        net.minecraft.core.Registry.register(
                net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(SpaceReloaded.MOD_ID, "lava_tube"), LAVA_TUBE);
        net.minecraft.core.Registry.register(
                net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(SpaceReloaded.MOD_ID, "crashed_probe"), CRASHED_PROBE);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ORE_TITANIUM);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ORE_TUNGSTEN);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, OIL_SHALE);
        for (ResourceKey<PlacedFeature> ore : java.util.List.of(ORE_HALITE, ORE_FLUORITE, ORE_SPODUMENE,
                ORE_BERYL, ORE_BORAX, ORE_URANINITE, ORE_GALENA)) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, ore);
        }
    }

    private ModWorldgen() {
    }
}
