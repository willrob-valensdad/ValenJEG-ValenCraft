package ttv.migami.jeg.tech.astra;

import java.lang.reflect.Proxy;
import java.util.Set;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import team.reborn.energy.api.EnergyStorage;
import ttv.migami.jeg.Reference;

/** Native registration kept separate from the JEG and MTEG station registries. */
public final class AstraTechContent {
    private AstraTechContent() {}
    public static CoalGeneratorBlock COAL_GENERATOR;
    public static BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR_ENTITY;
    public static BlockItem COAL_GENERATOR_ITEM;

    public static void init() {
        registerContent();
        EnergyStorage.SIDED.registerForBlockEntity((generator, side) -> generator.power().output(), COAL_GENERATOR_ENTITY);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(entries -> entries.accept(COAL_GENERATOR_ITEM));
    }

    /** Also used by the offline native contract harness during its registration phase. */
    static void registerContent() {
        if (COAL_GENERATOR != null) throw new IllegalStateException("Ad Astra technology registered twice");
        var id = Reference.id("valen_coal_generator");
        COAL_GENERATOR = Registry.register(BuiltInRegistries.BLOCK, id,
            new CoalGeneratorBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)).strength(3.5F).sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(CoalGeneratorBlock.LIT) ? 13 : 0)));
        COAL_GENERATOR_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, entityType());
        COAL_GENERATOR_ITEM = Registry.register(BuiltInRegistries.ITEM, id,
            new BlockItem(COAL_GENERATOR, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                .component(DataComponents.ITEM_NAME, Component.literal("Coal Generator"))));
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<CoalGeneratorBlockEntity> entityType() {
        // Same native 26.3 supplier constructor used by the accepted legacy Ammo Box.
        try {
            Class<?> supplier = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object factory = Proxy.newProxyInstance(supplier.getClassLoader(), new Class<?>[]{supplier},
                (proxy, method, args) -> new CoalGeneratorBlockEntity((net.minecraft.core.BlockPos)args[0],
                    (net.minecraft.world.level.block.state.BlockState)args[1]));
            var constructor = BlockEntityType.class.getDeclaredConstructor(supplier, Set.class);
            constructor.setAccessible(true);
            return (BlockEntityType<CoalGeneratorBlockEntity>)constructor.newInstance(factory, Set.of(COAL_GENERATOR));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot register coal generator", error);
        }
    }
}
