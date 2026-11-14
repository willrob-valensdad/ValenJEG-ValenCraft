package top.ribs.scguns.world.gen;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.world.GeothermalVentFeature;
import top.ribs.scguns.world.SulfurVentFeature;

/** 26.3 stores the ore configuration on the Feature itself. Keys stay scguns so the datapack ids do not move. */
public class ModConfiguredFeatures {
    public static final ResourceKey<Feature> ANTHRALITE_ORE_KEY = registerKey("anthralite_ore");
    public static final ResourceKey<Feature> SULFUR_ORE_KEY = registerKey("sulfur_ore");
    public static final ResourceKey<Feature> NETHER_SULFUR_ORE_KEY = registerKey("nether_sulfur_ore");
    public static final ResourceKey<Feature> GEOTHERMAL_VENT_KEY = registerKey("geothermal_vent");
    public static final ResourceKey<Feature> SULFUR_VENT_KEY = registerKey("sulfur_vent");
    public static final ResourceKey<Feature> VEHEMENT_COAL_ORE_KEY = registerKey("vehement_coal_ore");
    public static final ResourceKey<Feature> RICH_PHOSPHORITE_ORE_KEY = registerKey("rich_phosphorite");
    public static final ResourceKey<Feature> PHOSPHORITE_KEY = registerKey("phosphorite");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherrack = new BlockMatchTest(Blocks.NETHERRACK);
        RuleTest phosphorite = new BlockMatchTest(ModBlocks.PHOSPHORITE.get());
        context.register(PHOSPHORITE_KEY, new OreFeature(List.of(new BlockReplacement(stone, ModBlocks.PHOSPHORITE.get().defaultBlockState())), 33));
        context.register(RICH_PHOSPHORITE_ORE_KEY, new OreFeature(List.of(new BlockReplacement(phosphorite, ModBlocks.RICH_PHOSPHORITE.get().defaultBlockState())), 8));
        context.register(ANTHRALITE_ORE_KEY, new OreFeature(List.of(
                new BlockReplacement(stone, ModBlocks.ANTHRALITE_ORE.get().defaultBlockState()),
                new BlockReplacement(deepslate, ModBlocks.DEEPSLATE_ANTHRALITE_ORE.get().defaultBlockState())), 10));
        context.register(SULFUR_ORE_KEY, new OreFeature(List.of(
                new BlockReplacement(stone, ModBlocks.SULFUR_ORE.get().defaultBlockState()),
                new BlockReplacement(deepslate, ModBlocks.DEEPSLATE_SULFUR_ORE.get().defaultBlockState())), 9));
        context.register(NETHER_SULFUR_ORE_KEY, new OreFeature(List.of(new BlockReplacement(netherrack, ModBlocks.NETHER_SULFUR_ORE.get().defaultBlockState())), 12));
        context.register(VEHEMENT_COAL_ORE_KEY, new OreFeature(List.of(new BlockReplacement(netherrack, ModBlocks.VEHEMENT_COAL_ORE.get().defaultBlockState())), 6));
        context.register(GEOTHERMAL_VENT_KEY, new GeothermalVentFeature());
        context.register(SULFUR_VENT_KEY, new SulfurVentFeature());
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, name));
    }
}
