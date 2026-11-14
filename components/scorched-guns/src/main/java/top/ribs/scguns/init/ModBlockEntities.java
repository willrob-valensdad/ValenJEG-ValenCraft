package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.blockentity.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "scguns");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GunShelfBlockEntity>> GUN_SHELF_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("gun_shelf", () ->
                    new BlockEntityType<>(GunShelfBlockEntity::new, Set.of(ModBlocks.GUN_SHELF.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobTrapBlockEntity>> MOB_TRAP =
            BLOCK_ENTITIES.register("mob_trap", () ->
                    new BlockEntityType<>(MobTrapBlockEntity::new, Set.of(ModBlocks.MOB_TRAP.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedComposterBlockEntity>> ADVANCED_COMPOSTER =
            BLOCK_ENTITIES.register("advanced_composter", () ->
                    new BlockEntityType<>(AdvancedComposterBlockEntity::new, Set.of(ModBlocks.ADVANCED_COMPOSTER.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowderKegBlockEntity>> POWDER_KEG =
            BLOCK_ENTITIES.register("powder_keg", () ->
                    new BlockEntityType<>(PowderKegBlockEntity::new, Set.of(ModBlocks.POWDER_KEG.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NitroKegBlockEntity>> NITRO_KEG =
            BLOCK_ENTITIES.register("nitro_keg", () ->
                    new BlockEntityType<>(NitroKegBlockEntity::new, Set.of(ModBlocks.NITRO_KEG.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargedAmethystRelayBlockEntity>> CHARGED_AMETHYST_RELAY =
            BLOCK_ENTITIES.register("charged_amethyst_relay", () ->
                    new BlockEntityType<>(ChargedAmethystRelayBlockEntity::new, Set.of(ModBlocks.CHARGED_AMETHYST_RELAY.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShockCoilBlockEntity>> SHOCK_COIL =
            BLOCK_ENTITIES.register("shock_coil", () ->
                    new BlockEntityType<>(ShockCoilBlockEntity::new, Set.of(ModBlocks.SHOCK_COIL.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MineUnitBlockEntity>> MINE_UNIT =
            BLOCK_ENTITIES.register("mine_unit", () ->
                    new BlockEntityType<>(MineUnitBlockEntity::new, Set.of(ModBlocks.MINE_UNIT.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GunBenchBlockEntity>> GUN_BENCH =
            BLOCK_ENTITIES.register("gun_bench", () ->
                    new BlockEntityType<>(GunBenchBlockEntity::new, Set.of(ModBlocks.GUN_BENCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CryoniterBlockEntity>> CRYONITER =
            BLOCK_ENTITIES.register("cryoniter", () ->
                    new BlockEntityType<>(CryoniterBlockEntity::new, Set.of(ModBlocks.CRYONITER.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermolithBlockEntity>> THERMOLITH =
            BLOCK_ENTITIES.register("thermolith", () ->
                    new BlockEntityType<>(ThermolithBlockEntity::new, Set.of(ModBlocks.THERMOLITH.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PolarGeneratorBlockEntity>> POLAR_GENERATOR =
            BLOCK_ENTITIES.register("polar_generator", () ->
                    new BlockEntityType<>(PolarGeneratorBlockEntity::new, Set.of(ModBlocks.POLAR_GENERATOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PenetratorBlockEntity>> PENETRATOR =
            BLOCK_ENTITIES.register("penetrator", () ->
                    new BlockEntityType<>(PenetratorBlockEntity::new, Set.of(ModBlocks.PENETRATOR.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaceratorBlockEntity>> MACERATOR =
            BLOCK_ENTITIES.register("macerator", () ->
                    new BlockEntityType<>(MaceratorBlockEntity::new, Set.of(ModBlocks.MACERATOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PoweredMaceratorBlockEntity>> POWERED_MACERATOR =
            BLOCK_ENTITIES.register("powered_macerator", () ->
                    new BlockEntityType<>(PoweredMaceratorBlockEntity::new, Set.of(ModBlocks.POWERED_MACERATOR.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalPressBlockEntity>> MECHANICAL_PRESS =
            BLOCK_ENTITIES.register("mechanical_press", () ->
                    new BlockEntityType<>(MechanicalPressBlockEntity::new, Set.of(ModBlocks.MECHANICAL_PRESS.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PoweredMechanicalPressBlockEntity>> POWERED_MECHANICAL_PRESS =
            BLOCK_ENTITIES.register("powered_mechanical_press", () ->
                    new BlockEntityType<>(PoweredMechanicalPressBlockEntity::new, Set.of(ModBlocks.POWERED_MECHANICAL_PRESS.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningBatteryBlockEntity>> LIGHTNING_BATTERY =
            BLOCK_ENTITIES.register("lightning_battery", () ->
                    new BlockEntityType<>(LightningBatteryBlockEntity::new, Set.of(ModBlocks.LIGHTNING_BATTERY.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicTurretBlockEntity>> BASIC_TURRET =
            BLOCK_ENTITIES.register("basic_turret", () ->
                    new BlockEntityType<>(BasicTurretBlockEntity::new, Set.of(ModBlocks.BASIC_TURRET.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoTurretBlockEntity>> AUTO_TURRET =
            BLOCK_ENTITIES.register("auto_turret", () ->
                    new BlockEntityType<>(AutoTurretBlockEntity::new, Set.of(ModBlocks.AUTO_TURRET.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnemyTurretBlockEntity>> ENEMY_TURRET =
            BLOCK_ENTITIES.register("enemy_turret", () ->
                    new BlockEntityType<>(EnemyTurretBlockEntity::new, Set.of(ModBlocks.ENEMY_TURRET.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShotgunTurretBlockEntity>> SHOTGUN_TURRET =
            BLOCK_ENTITIES.register("shotgun_turret", () ->
                    new BlockEntityType<>(ShotgunTurretBlockEntity::new, Set.of(ModBlocks.SHOTGUN_TURRET.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SniperTurretBlockEntity>> SNIPER_TURRET =
            BLOCK_ENTITIES.register("sniper_turret", () ->
                    new BlockEntityType<>(SniperTurretBlockEntity::new, Set.of(ModBlocks.SNIPER_TURRET.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShellCatcherModuleBlockEntity>> SHELL_CATCHER_MODULE =
            BLOCK_ENTITIES.register("shell_catcher_module", () ->
                    new BlockEntityType<>(ShellCatcherModuleBlockEntity::new, Set.of(ModBlocks.SHELL_CATCHER_TURRET_MODULE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmmoModuleBlockEntity>> AMMO_MODULE =
            BLOCK_ENTITIES.register("ammo_module", () ->
                    new BlockEntityType<>(AmmoModuleBlockEntity::new, Set.of(ModBlocks.AMMO_TURRET_MODULE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmmoBoxBlockEntity>> AMMO_BOX =
            BLOCK_ENTITIES.register("ammo_box", () ->
                    new BlockEntityType<>(AmmoBoxBlockEntity::new, Set.of(ModBlocks.AMMO_BOX.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VentCollectorBlockEntity>> VENT_COLLECTOR = BLOCK_ENTITIES.register("vent_collector",
            () -> new BlockEntityType<>(VentCollectorBlockEntity::new, Set.of(ModBlocks.VENT_COLLECTOR.get())));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}






