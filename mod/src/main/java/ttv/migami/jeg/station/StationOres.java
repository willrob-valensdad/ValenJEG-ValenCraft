package ttv.migami.jeg.station;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import ttv.migami.jeg.Reference;
/** Fabric replacement for original forge:add_features declarations; configured features are copied upstream data. */
public final class StationOres {
    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Reference.id("scrap_ore_placed")));
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BADLANDS),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Reference.id("brimstone_ore_placed")));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DRIPSTONE_CAVES),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Reference.id("dripstone_brimstone_ore_placed")));
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_NETHER),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Reference.id("nether_brimstone_ore_placed")));
    }
}
