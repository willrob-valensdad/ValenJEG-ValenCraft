package ttv.migami.jeg.tech.astra.oxygen;

import java.lang.reflect.Proxy;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import team.reborn.energy.api.EnergyStorage;
import za.co.neroland.nerospace.fabric.NerospaceFabric;
import za.co.neroland.nerolandcore.platform.FabricGasLookup;

/** Optional bootstrap; Content is only loaded after the three installed APIs are present. */
public final class AstraOxygen {
    private AstraOxygen() {}
    public static void init() {
        var loader = FabricLoader.getInstance();
        if (loader.isModLoaded("team_reborn_energy") && loader.isModLoaded("nerospace") && loader.isModLoaded("nerolandcore"))
            Content.register();
    }
    public static final class Content {
        public static OxygenLoaderBlock BLOCK;
        public static BlockEntityType<OxygenLoaderBlockEntity> ENTITY;
        public static BlockItem ITEM;
        private Content() {}
        static Identifier id() { return Identifier.parse("jeg:valen_oxygen_loader"); }
        private static void register() {
            if (BLOCK != null) throw new IllegalStateException("Oxygen loader registered twice");
            BLOCK = Registry.register(BuiltInRegistries.BLOCK, id(), new OxygenLoaderBlock(
                BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,id()))
                    .strength(4).sound(SoundType.METAL).lightLevel(state -> state.getValue(OxygenLoaderBlock.ACTIVE) ? 10 : 0)));
            ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id(), type());
            ITEM = Registry.register(BuiltInRegistries.ITEM, id(), new BlockItem(BLOCK,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id()))
                    .component(DataComponents.ITEM_NAME, Component.literal("Oxygen Loader"))));
            EnergyStorage.SIDED.registerForBlockEntity((entity, side) -> entity.sideConfig().allows(2,side)?entity.process().energyInput():null, ENTITY);
            FluidStorage.SIDED.registerForBlockEntity((entity, side) -> entity.sideConfig().allows(3,side)?entity.waterInput(false):null, ENTITY);
            NerospaceFabric.GAS.registerForBlockEntity((entity, side) -> side!=null&&entity.sideConfig().allows(5,side)?entity.gasIntake().space():entity.sideConfig().allows(4,side)?entity.oxygenOutput().space():null, ENTITY);
            FabricGasLookup.GAS.registerForBlockEntity((entity, side) -> side!=null&&entity.sideConfig().allows(5,side)?entity.gasIntake().core():entity.sideConfig().allows(4,side)?entity.oxygenOutput().core():null, ENTITY);
            ttv.migami.jeg.tech.astra.oxygen.ui.OxygenUi.register();
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new OxygenLoadingRecipe.Loader());
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ttv.migami.jeg.tech.astra.oxygen.ui.OxygenPassthroughRecipe());
            CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.parse("minecraft:tools_and_utilities"))).register(entries -> entries.accept(ITEM));
        }
        @SuppressWarnings("unchecked")
        private static BlockEntityType<OxygenLoaderBlockEntity> type() {
            try {
                Class<?> supplier = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
                Object factory = Proxy.newProxyInstance(supplier.getClassLoader(), new Class<?>[]{supplier},
                    (proxy, method, args) -> new OxygenLoaderBlockEntity((net.minecraft.core.BlockPos)args[0],
                        (net.minecraft.world.level.block.state.BlockState)args[1]));
                var ctor = BlockEntityType.class.getDeclaredConstructor(supplier, Set.class);
                ctor.setAccessible(true);
                return (BlockEntityType<OxygenLoaderBlockEntity>)ctor.newInstance(factory, Set.of(BLOCK));
            } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Cannot register oxygen loader",failure); }
        }
    }
}
