package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.*;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Reference.MOD_ID);
    public static final DeferredBlock<Block> FAKE_SOUL_FIRE = REGISTER.register("fake_soul_fire",
            registryName -> new FakeSoulFireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE).setId(ResourceKey.create(Registries.BLOCK, registryName)), 2.0F));
    public static final DeferredBlock<Block> TEMPORARY_LIGHT = REGISTER.register("temporary_light",
            registryName -> new TemporaryLightBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> GUN_SHELF = register("gun_shelf",
            registryName -> new GunShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.5F)));
    public static final DeferredBlock<Block> MOB_TRAP = register("mob_trap",
            registryName -> new MobTrapBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(12.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> AMMO_BOX = register("ammo_box",
            registryName -> new AmmoBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .strength(2.5F)));
    public static final DeferredBlock<Block> ADVANCED_COMPOSTER = register("advanced_composter",
            registryName -> new AdvancedComposterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COMPOSTER).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.5F)));
    public static final DeferredBlock<Block> POWDER_KEG = register("powder_keg",
            registryName -> new PowderKegBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.5F)));
    public static final DeferredBlock<Block> NITRO_KEG = register("nitro_keg",
            registryName -> new NitroKegBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.5F)));
    public static final DeferredBlock<Block> CRYONITER = register("cryoniter",
            registryName -> new CryoniterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final DeferredBlock<Block> THERMOLITH = register("thermolith",
            registryName -> new ThermolithBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final DeferredBlock<Block> BASIC_TURRET = register("basic_turret",
            registryName -> new BasicTurretBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> SHOTGUN_TURRET = register("shotgun_turret",
            registryName -> new ShotgunTurretBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> SNIPER_TURRET = register("sniper_turret",
            registryName -> new SniperTurretBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> CHARGED_AMETHYST_RELAY = register("charged_amethyst_relay",
            registryName -> new ChargedAmethystRelayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> SHOCK_COIL = register("shock_coil",
            registryName -> new ShockCoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(4.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> MINE_UNIT = register("mine_unit",
            registryName -> new MineUnitBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> AUTO_TURRET = register("auto_turret",
            registryName -> new AutoTurretBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> ENEMY_TURRET = register("enemy_turret",
            registryName -> new EnemyTurretBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(8.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> POLAR_GENERATOR = register("polar_generator",
            registryName -> new PolarGeneratorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.COW_BELL)
                    .strength(3.0F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> LIGHTNING_ROD_CONNECTOR = register("lightning_rod_connector",
            registryName -> new LightningRodConnectorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHTNING_ROD.weathering().unaffected()).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> LIGHTNING_BATTERY = register("lightning_battery",
            registryName -> new LightningBattery(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.COW_BELL)
                    .strength(3.0F)
                    .noOcclusion()
                    .lightLevel((state) -> state.getValue(LightningBattery.CHARGED) ? 15 : 0)));

    public static final DeferredBlock<Block> MACERATOR = register("macerator",
            registryName -> new MaceratorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.HAT)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> POWERED_MACERATOR = register("powered_macerator",
            registryName -> new PoweredMaceratorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.HAT)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> MECHANICAL_PRESS = register("mechanical_press",
            registryName -> new MechanicalPressBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BANJO)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> POWERED_MECHANICAL_PRESS = register("powered_mechanical_press",
            registryName -> new PoweredMechanicalPressBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BANJO)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> GUN_BENCH = register("gun_bench",
            registryName -> new GunBenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.GUITAR)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> ANTHRALITE_BLOCK = register("anthralite_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_GRATE = register("anthralite_grate",
            registryName -> new GrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> ANTHRALITE_GRATE_PANE = register("anthralite_grate_pane",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> ANTHRALITE_TILES = register("anthralite_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_TILES_SLAB = register("anthralite_tiles_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_TILES_STAIRS = register("anthralite_tiles_stairs",
            registryName -> new StairBlock(ModBlocks.ANTHRALITE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_PLATES = register("anthralite_plates",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> CHISELED_ANTHRALITE_BLOCK = register("chiseled_anthralite_block",
            registryName -> new RedstoneLampBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .lightLevel(state -> 0)));
    public static final DeferredBlock<Block> CUT_ANTHRALITE = register("cut_anthralite",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_ANTHRALITE_SLAB = register("cut_anthralite_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_ANTHRALITE_STAIRS = register("cut_anthralite_stairs",
            registryName -> new StairBlock(ModBlocks.CUT_ANTHRALITE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(2.5F)));
    public static final DeferredBlock<Block> ANTHRALITE_PILLAR = register("anthralite_pillar",
            registryName -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_LAMP = register("anthralite_lamp",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .lightLevel(state -> 15)));
    public static final DeferredBlock<Block> TREATED_IRON_BLOCK = register("treated_iron_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> CHISELED_TREATED_IRON_BLOCK = register("chiseled_treated_iron_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> CUT_TREATED_IRON = register("cut_treated_iron",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_TREATED_IRON_SLAB = register("cut_treated_iron_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_TREATED_IRON_STAIRS = register("cut_treated_iron_stairs",
            registryName -> new StairBlock(ModBlocks.CUT_TREATED_IRON.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(2.5F)));
    public static final DeferredBlock<Block> TREATED_IRON_LAMP = register("treated_iron_lamp",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .lightLevel(state -> 15)));
    public static final DeferredBlock<Block> TREATED_IRON_PLATES = register("treated_iron_plates",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> TREATED_IRON_BARS = register("treated_iron_bars",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> TREATED_IRON_GRATE_PANE = register("treated_iron_grate_pane",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> TREATED_BRASS_BLOCK = register("treated_brass_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> VEHEMENT_COAL_BLOCK = registerBurnable("vehement_coal_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.DIDGERIDOO)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)), 43200);

    public static final DeferredBlock<Block> PLASMA_BLOCK = registerBurnable("plasma_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 15)
                    .strength(3.0F)), 16200);


    public static final DeferredBlock<Block> DIAMOND_STEEL_BLOCK = register("diamond_steel_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_GRATE = register("diamond_steel_grate",
            registryName -> new GrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> DIAMOND_STEEL_GRATE_PANE = register("diamond_steel_grate_pane",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> CHISELED_DIAMOND_STEEL_BLOCK = register("chiseled_diamond_steel_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final DeferredBlock<Block> CUT_DIAMOND_STEEL = register("cut_diamond_steel",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5F)));
    public static final DeferredBlock<Block> CUT_DIAMOND_STEEL_SLAB = register("cut_diamond_steel_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5F)));
    public static final DeferredBlock<Block> CUT_DIAMOND_STEEL_STAIRS = register("cut_diamond_steel_stairs",
            registryName -> new StairBlock(ModBlocks.CUT_DIAMOND_STEEL.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(4.5F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_BARS = register("diamond_steel_bars",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> DIAMOND_STEEL_PANEL = register("diamond_steel_panel",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_PILLAR = register("diamond_steel_pillar",
            registryName -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_LAMP = register("diamond_steel_lamp",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .lightLevel(state -> 15)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_TILES = register("diamond_steel_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_TILES_SLAB = register("diamond_steel_tiles_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName)).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final DeferredBlock<Block> DIAMOND_STEEL_TILES_STAIRS = register("diamond_steel_tiles_stairs",
            registryName -> new StairBlock(ModBlocks.DIAMOND_STEEL_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(5.0F)));
    public static final DeferredBlock<Block> RAW_ANTHRALITE_BLOCK = register("raw_anthralite_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final DeferredBlock<Block> ANTHRALITE_ORE = register("anthralite_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 0), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_ANTHRALITE_ORE = register("deepslate_anthralite_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> SULFUR_BLOCK = registerBurnable("sulfur_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)), 7200);
    public static final DeferredBlock<Block> ANCIENT_BRASS_BLOCK = register("ancient_brass_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));

    public static final DeferredBlock<Block> SKIBIDI = register("skibidi",
            registryName -> new BasicDirectionalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> FELIX_MEMORIAL = register("felix_memorial",
            registryName -> new MemorialBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> TURRET_TARGETING_BLOCK = register("turret_targeting_module",
            registryName -> new TurretTargetingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> PLAYER_TURRET_TARGETING_BLOCK = register("player_turret_targeting_module",
            registryName -> new PlayerTurretTargetingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> HOSTILE_TURRET_TARGETING_BLOCK = register("hostile_turret_targeting_module",
            registryName -> new HostileTurretTargetingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> FIRE_RATE_TURRET_MODULE = register("fire_rate_turret_module",
            registryName -> new FireRateModuleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> DAMAGE_TURRET_MODULE = register("damage_turret_module",
            registryName -> new DamageModuleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> RANGE_TURRET_MODULE = register("range_turret_module",
            registryName -> new RangeModuleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> SHELL_CATCHER_TURRET_MODULE = register("shell_catcher_turret_module",
            registryName -> new ShellCatcherModuleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> AMMO_TURRET_MODULE = register("ammo_turret_module",
            registryName -> new AmmoModuleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .randomTicks()
                    .noOcclusion()));

    public static final DeferredBlock<Block> RICH_PHOSPHORITE = register("rich_phosphorite",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final DeferredBlock<Block> SULFUR_ORE = register("sulfur_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_SULFUR_ORE = register("deepslate_sulfur_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> NETHER_SULFUR_ORE = register("nether_sulfur_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_GOLD_ORE)));
    public static final DeferredBlock<Block> VEHEMENT_COAL_ORE = register("vehement_coal_ore",
            registryName -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)).mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_GOLD_ORE)));

    public static final DeferredBlock<Block> NITER_LAYER = register("niter",
            registryName -> new NiterLayerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> SULFUR_LAYER = register("sulfur",
            registryName -> new SulfurLayerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> BAT_GUANO_LAYER = registerWithoutItem("bat_guano_layer",
            registryName -> new BatGuanoLayerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> PHOSPHOR_LAYER = registerWithoutItem("phosphor_layer",
            registryName -> new PhosphorLayerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<ViciousAcidBlock> VICIOUS_ACID_BLOCK = registerWithoutItem("vicious_acid_block",
            registryName -> new ViciousAcidBlock(ModFluids.VICIOUS_ACID_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, registryName)).noLootTable()));

    public static final DeferredBlock<ViciousAcidCauldronBlock> VICIOUS_ACID_CAULDRON = registerWithoutItem("vicious_acid_cauldron",
            registryName -> new ViciousAcidCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> NITER_BLOCK = register("niter_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> GUANO_CANDLE = register("guano_candle",
            registryName -> new GuanoCandleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CANDLE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .lightLevel(state -> state.getValue(GuanoCandleBlock.LIT) ? 10 : 0)));

    public static final DeferredBlock<Block> PENETRATOR = register("penetrator",
            registryName -> new PenetratorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(1.0F), 10));

    public static final DeferredBlock<Block> PLASMA_LANTERN = register("plasma_lantern",
            registryName -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .strength(3.0F)
                    .lightLevel((state) -> 15)));

    public static final DeferredBlock<Block> GEOTHERMAL_VENT = register("geothermal_vent",
            registryName -> new GeothermalVentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()
                    .pushReaction(PushReaction.POPPED)
                    .randomTicks()));
    public static final DeferredBlock<Block> SULFUR_VENT = register("sulfur_vent",
            registryName -> new SulfurVentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(12.0F)
                    .noOcclusion()
                    .pushReaction(PushReaction.POPPED)
                    .randomTicks()));


    public static final DeferredBlock<Block> VENT_COLLECTOR = register("vent_collector",
            registryName -> new VentCollectorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName)).noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()
                    .randomTicks()));


    public static final DeferredBlock<Block> NITER_GLASS = register("niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> WHITE_NITER_GLASS = register("white_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> ORANGE_NITER_GLASS = register("orange_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> MAGENTA_NITER_GLASS = register("magenta_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> LIGHT_BLUE_NITER_GLASS = register("light_blue_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> YELLOW_NITER_GLASS = register("yellow_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> LIME_NITER_GLASS = register("lime_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> PINK_NITER_GLASS = register("pink_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> GRAY_NITER_GLASS = register("gray_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> LIGHT_GRAY_NITER_GLASS = register("light_gray_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> CYAN_NITER_GLASS = register("cyan_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> PURPLE_NITER_GLASS = register("purple_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> BLUE_NITER_GLASS = register("blue_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> BROWN_NITER_GLASS = register("brown_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> GREEN_NITER_GLASS = register("green_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> RED_NITER_GLASS = register("red_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> BLACK_NITER_GLASS = register("black_niter_glass",
            registryName -> new NiterGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<Block> RAW_PHOSPHOR_BLOCK = register("raw_phosphor_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final DeferredBlock<Block> PHOSPHORITE = register("phosphorite",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(1.5F)));
    public static final DeferredBlock<Block> SMOOTH_PHOSPHORITE = register("smooth_phosphorite",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> POLISHED_PHOSPHORITE = register("polished_phosphorite",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> PHOSPHORITE_BRICKS = register("phosphorite_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> CRACKED_PHOSPHORITE_BRICKS = register("cracked_phosphorite_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> PHOSPHORITE_BRICK_SLAB = register("phosphorite_brick_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> PHOSPHORITE_BRICK_STAIRS = register("phosphorite_brick_stairs",
            registryName -> new StairBlock(ModBlocks.PHOSPHORITE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));
    public static final DeferredBlock<Block> PHOSPHORITE_BRICK_WALL = register("phosphorite_brick_wall",
            registryName -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)));

    public static final DeferredBlock<Block> ASGHARIAN_PILLAR = register("asgharian_pillar",
            registryName -> new AsgharianPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> ASGHARIAN_BRICKS = register("asgharian_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> REINFORCED_ASGHARIAN_TILES = register("reinforced_asgharian_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(6.0F)));
    public static final DeferredBlock<Block> ASGHARIAN_TILES = register("asgharian_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> CRACKED_ASGHARIAN_TILES = register("cracked_asgharian_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> MOSSY_ASGHARIAN_BRICKS = register("mossy_asgharian_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> MOSSY_ASGHARIAN_TILES = register("mossy_asgharian_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> CHISELED_ASGHARIAN_BRICKS = register("chiseled_asgharian_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> POLISHED_ASGHARIAN_PANEL = register("polished_asgharian_panel",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));

    public static final DeferredBlock<Block> CRACKED_ASGHARIAN_BRICKS = register("cracked_asgharian_bricks",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> ASGHARIAN_BRICK_SLAB = register("asgharian_brick_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> ASGHARIAN_BRICK_STAIRS = register("asgharian_brick_stairs",
            registryName -> new StairBlock(ModBlocks.ASGHARIAN_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));
    public static final DeferredBlock<Block> ASGHARIAN_BRICK_WALL = register("asgharian_brick_wall",
            registryName -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.PLING)
                    .strength(4.0F)));

    public static final DeferredBlock<Block> TREATED_BRASS_PLATES = register("treated_brass_plates",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> CUT_TREATED_BRASS = register("cut_treated_brass",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_TREATED_BRASS_SLAB = register("cut_treated_brass_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(2.5F)));
    public static final DeferredBlock<Block> CUT_TREATED_BRASS_STAIRS = register("cut_treated_brass_stairs",
            registryName -> new StairBlock(ModBlocks.CUT_TREATED_BRASS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(2.5F)));
    public static final DeferredBlock<Block> TREATED_IRON_GRATE = register("treated_iron_grate",
            registryName -> new GrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> TREATED_BRASS_GRATE = register("treated_brass_grate",
            registryName -> new GrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> TREATED_BRASS_GRATE_PANE = register("treated_brass_grate_pane",
            registryName -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> CHISELED_TREATED_BRASS_BLOCK = register("chiseled_treated_brass_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> TREATED_BRASS_TILES = register("treated_brass_tiles",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> TREATED_BRASS_TILES_SLAB = register("treated_brass_tiles_slab",
            registryName -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> TREATED_BRASS_TILES_STAIRS = register("treated_brass_tiles_stairs",
            registryName -> new StairBlock(ModBlocks.TREATED_BRASS_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)));
    public static final DeferredBlock<Block> TREATED_BRASS_LAMP = register("treated_brass_lamp",
            registryName -> new RedstoneLampBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.FLUTE)
                    .strength(3.0F)
                    .lightLevel(state -> state.getValue(RedstoneLampBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> SCORCHED_BLOCK = register("scorched_block",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .requiresCorrectToolForDrops()
                    .strength(5.5F)
                    .lightLevel(state -> 10)));

    public static final DeferredBlock<Block> SANDBAG = register("sandbag",
            registryName -> new SandbagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(0.5F)
                    .noOcclusion()));
    public static final DeferredBlock<Block> SUPPLY_CRATE = register("supply_crate",
            registryName -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(1.0F)));


    private static <T extends Block> DeferredBlock<T> register(String id, Function<Identifier, T> blockFactory) {
        DeferredBlock<T> block = REGISTER.register(id, blockFactory);
        ModItems.REGISTER.registerSimpleBlockItem(id, block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerWithoutItem(String id, Function<Identifier, T> blockFactory) {
        return REGISTER.register(id, blockFactory);
    }

    private static <T extends Block> DeferredBlock<T> registerBurnable(String id, Function<Identifier, T> blockFactory, int burnTime) {
        DeferredBlock<T> block = REGISTER.register(id, blockFactory);
        ModItems.REGISTER.registerItem(id, props -> new BlockItem(block.get(), props.component(net.minecraft.core.component.DataComponents.COOKING_FUEL, new net.minecraft.world.item.component.CookingFuel(new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(burnTime), new net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant(1.0F)))));
        return block;
    }

}





