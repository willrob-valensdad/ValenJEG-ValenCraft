package com.zurrtum.create.infrastructure.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ZINC_ORE = register("zinc_ore");
    public static final ResourceKey<PlacedFeature> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final ResourceKey<PlacedFeature> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static ResourceKey<PlacedFeature> register(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static void register() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, ZINC_ORE);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, STRIATED_ORES_OVERWORLD);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.UNDERGROUND_ORES, STRIATED_ORES_NETHER);
    }
}
