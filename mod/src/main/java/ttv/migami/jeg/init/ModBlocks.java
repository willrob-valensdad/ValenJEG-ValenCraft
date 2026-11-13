package ttv.migami.jeg.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ttv.migami.jeg.fabric.compat.neoforge.neoforge.registries.DeferredHolder;
import ttv.migami.jeg.fabric.compat.neoforge.neoforge.registries.DeferredRegister;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.block.DynamicLightBlock;
import ttv.migami.jeg.block.MagazineLoaderBlock;
import ttv.migami.jeg.vehicle.block.VehicleAssemblingTableBlock;
import ttv.migami.jeg.vehicle.block.VehicleChargingStationBlock;
import ttv.migami.jeg.vehicle.block.VehicleContainerBlock;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(Registries.BLOCK, Reference.MOD_ID);

    public static final DeferredHolder<Block, ttv.migami.jeg.structure.HelipadMarkerBlock> SOURCE_HELIPAD_MARKER = REGISTER.register(
            "source_helipad_marker", () -> new ttv.migami.jeg.structure.HelipadMarkerBlock(blockProperties(Reference.id("source_helipad_marker"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion())
    );

    public static final DeferredHolder<Block, Block> HBM_TAPE_RECORDER = REGISTER.register("hbm_tape_recorder", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.oriented(blockProperties(Reference.id("hbm_tape_recorder"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, Block> HBM_STEEL_POLE = REGISTER.register("hbm_steel_pole", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.oriented(blockProperties(Reference.id("hbm_steel_pole"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, Block> HBM_BARBED_WIRE = REGISTER.register("hbm_barbed_wire", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.oriented(blockProperties(Reference.id("hbm_barbed_wire"), net.minecraft.world.level.block.Blocks.IRON_BARS).strength(2.0F).sound(SoundType.METAL).noOcclusion().noCollision()));
    public static final DeferredHolder<Block, Block> HBM_SATELLITE_RECEIVER = REGISTER.register("hbm_satellite_receiver", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.oriented(blockProperties(Reference.id("hbm_satellite_receiver"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, Block> HBM_POLE_TOP = REGISTER.register("hbm_pole_top", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.poleTop(blockProperties(Reference.id("hbm_pole_top"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, ttv.migami.jeg.structure.mesh.HbmMeshBlocks.MetalFenceBlock> HBM_METAL_FENCE = REGISTER.register("hbm_metal_fence", () -> new ttv.migami.jeg.structure.mesh.HbmMeshBlocks.MetalFenceBlock(blockProperties(Reference.id("hbm_metal_fence"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, ttv.migami.jeg.structure.HbmCubicalBlocks.YellowBarrelBlock> HBM_YELLOW_BARREL = REGISTER.register("hbm_yellow_barrel", () -> new ttv.migami.jeg.structure.HbmCubicalBlocks.YellowBarrelBlock(blockProperties(Reference.id("hbm_yellow_barrel"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(2.0F).sound(SoundType.METAL)));
    public static final DeferredHolder<Block, Block> HBM_DECO_STEEL = REGISTER.register("hbm_deco_steel", () -> new Block(blockProperties(Reference.id("hbm_deco_steel"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(5.0F).sound(SoundType.METAL)));
    public static final DeferredHolder<Block, Block> HBM_REINFORCED_GLASS = REGISTER.register("hbm_reinforced_glass", () -> new Block(blockProperties(Reference.id("hbm_reinforced_glass"), net.minecraft.world.level.block.Blocks.GLASS).strength(2.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredHolder<Block, Block> HBM_REINFORCED_LAMP = REGISTER.register("hbm_reinforced_lamp", () -> new Block(blockProperties(Reference.id("hbm_reinforced_lamp"), net.minecraft.world.level.block.Blocks.REDSTONE_LAMP).strength(2.0F).sound(SoundType.GLASS).lightLevel(state -> 0)));

    public static final DeferredHolder<Block, ttv.migami.jeg.structure.BunkerVinylTile> HBM_VINYL_TILE = REGISTER.register("hbm_vinyl_tile", () -> new ttv.migami.jeg.structure.BunkerVinylTile(blockProperties(Reference.id("hbm_vinyl_tile"), net.minecraft.world.level.block.Blocks.SMOOTH_STONE).strength(10.0F, 60.0F).sound(SoundType.GLASS)));
    public static final DeferredHolder<Block, Block> HBM_FAN = REGISTER.register("hbm_fan", () -> ttv.migami.jeg.structure.mesh.HbmMeshBlocks.oriented(blockProperties(Reference.id("hbm_fan"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));

    public static final DeferredHolder<Block, DynamicLightBlock> DYNAMIC_LIGHT = REGISTER.register(
            "dynamic_light",
            () -> new DynamicLightBlock(blockProperties(Reference.id("dynamic_light")))
    );

    public static final DeferredHolder<Block, VehicleContainerBlock> VEHICLE_CONTAINER = REGISTER.register(
            "vehicle_container",
            () -> new VehicleContainerBlock(blockProperties(Reference.id("vehicle_container")).strength(3.0F).sound(SoundType.METAL).noOcclusion())
    );

    public static final DeferredHolder<Block, VehicleAssemblingTableBlock> VEHICLE_ASSEMBLING_TABLE = REGISTER.register(
            "vehicle_assembling_table",
            () -> new VehicleAssemblingTableBlock(blockProperties(Reference.id("vehicle_assembling_table")).strength(2.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion())
    );

    public static final DeferredHolder<Block, VehicleChargingStationBlock> VEHICLE_CHARGING_STATION = REGISTER.register(
            "vehicle_charging_station",
            () -> new VehicleChargingStationBlock(blockProperties(Reference.id("vehicle_charging_station")).strength(3.5F).sound(SoundType.METAL))
    );

    public static final DeferredHolder<Block, MagazineLoaderBlock> MAGAZINE_LOADER = REGISTER.register(
            "magazine_loader",
            () -> new MagazineLoaderBlock(blockProperties(Reference.id("magazine_loader")).strength(3.5F).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );

    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> SCRAP_WORKBENCH = REGISTER.register("scrap_workbench", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("scrap_workbench"), net.minecraft.world.level.block.Blocks.OAK_PLANKS).strength(1.5F)));
    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> GUNMETAL_WORKBENCH = REGISTER.register("gunmetal_workbench", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("gunmetal_workbench"), net.minecraft.world.level.block.Blocks.STONE).strength(3.0F).noOcclusion()));
    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> GUNNITE_WORKBENCH = REGISTER.register("gunnite_workbench", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("gunnite_workbench"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(5.0F).noOcclusion()));
    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> BLUEPRINT_WORKBENCH = REGISTER.register("blueprint_workbench", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("blueprint_workbench"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(5.0F).noOcclusion()));
    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> SCHEMATIC_STATION = REGISTER.register("schematic_station", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("schematic_station"), net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(5.0F).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, ttv.migami.jeg.station.StationBlock> RECYCLER = REGISTER.register("recycler", () -> new ttv.migami.jeg.station.StationBlock(blockProperties(Reference.id("recycler"), net.minecraft.world.level.block.Blocks.CAULDRON).strength(2.0F).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> SCRAP_BLOCK = REGISTER.register("scrap_block", () -> new Block(blockProperties(Reference.id("scrap_block"), net.minecraft.world.level.block.Blocks.IRON_BARS).strength(3.0F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> GUNMETAL_BLOCK = REGISTER.register("gunmetal_block", () -> new Block(blockProperties(Reference.id("gunmetal_block"), net.minecraft.world.level.block.Blocks.COPPER_BLOCK.weathering().unaffected()).strength(4.0F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> GUNNITE_BLOCK = REGISTER.register("gunnite_block", () -> new Block(blockProperties(Reference.id("gunnite_block"), net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK).strength(5.0F).requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> SCRAP_ORE = REGISTER.register("scrap_ore", () -> new Block(blockProperties(Reference.id("scrap_ore"), net.minecraft.world.level.block.Blocks.STONE).strength(3.0F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> DEEPSLATE_SCRAP_ORE = REGISTER.register("deepslate_scrap_ore", () -> new Block(blockProperties(Reference.id("deepslate_scrap_ore"), net.minecraft.world.level.block.Blocks.DEEPSLATE).strength(4.5F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> BRIMSTONE_ORE = REGISTER.register("brimstone_ore", () -> new Block(blockProperties(Reference.id("brimstone_ore"), net.minecraft.world.level.block.Blocks.DRIPSTONE_BLOCK).strength(1.5F).requiresCorrectToolForDrops().lightLevel(state -> 3)));
    public static final DeferredHolder<Block, Block> BLACKSTONE_BRIMSTONE_ORE = REGISTER.register("blackstone_brimstone_ore", () -> new Block(blockProperties(Reference.id("blackstone_brimstone_ore"), net.minecraft.world.level.block.Blocks.BLACKSTONE).strength(1.5F).requiresCorrectToolForDrops().lightLevel(state -> 3)));
    public static final DeferredHolder<Block, Block> BASALT_BRIMSTONE_ORE = REGISTER.register("basalt_brimstone_ore", () -> new Block(blockProperties(Reference.id("basalt_brimstone_ore"), net.minecraft.world.level.block.Blocks.BASALT).strength(1.5F).requiresCorrectToolForDrops().lightLevel(state -> 3)));

    private static BlockBehaviour.Properties blockProperties(Identifier id, Block source) {
        return BlockBehaviour.Properties.ofFullCopy(source).setId(ResourceKey.create(Registries.BLOCK,id));
    }
    private static BlockBehaviour.Properties blockProperties(Identifier id) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id));
    }
}
