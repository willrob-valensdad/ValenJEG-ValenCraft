package net.neoforged.neoforge.registries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;

public class DeferredRegister<T> {
    private final Object registryKey;
    protected final String modId;
    protected final List<Entry<T, ? extends T>> entries = new ArrayList<>();

    protected DeferredRegister(Object registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(Object registryKey, String modId) {
        return new DeferredRegister<>(registryKey, modId);
    }

    public static Items createItems(String modId) { return new Items(modId); }
    public static Blocks createBlocks(String modId) { return new Blocks(modId); }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<I> supplier) {
        DeferredHolder<T, I> holder = new DeferredHolder<>(supplier);
        if (registryKey instanceof ResourceKey<?> key) holder.bindKey((ResourceKey) key, id(name));
        entries.add(new Entry<>(name, holder));
        return holder;
    }

    protected Identifier id(String name) { return Identifier.fromNamespaceAndPath(modId, name); }

    public void register(IEventBus bus) {
        Registry<T> registry = resolveRegistry();
        for (Entry<T, ? extends T> entry : entries) {
            Identifier id = id(entry.name);
            T value = entry.holder.get();
            if (registry != null && !registry.containsKey(id)) Registry.register(registry, id, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Registry<T> resolveRegistry() {
        if (registryKey == Registries.ITEM) return (Registry<T>) BuiltInRegistries.ITEM;
        if (registryKey == Registries.BLOCK) return (Registry<T>) BuiltInRegistries.BLOCK;
        if (registryKey == Registries.BLOCK_ENTITY_TYPE) return (Registry<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        if (registryKey == Registries.MENU) return (Registry<T>) BuiltInRegistries.MENU;
        if (registryKey == Registries.ENTITY_TYPE) return (Registry<T>) BuiltInRegistries.ENTITY_TYPE;
        if (registryKey == Registries.SOUND_EVENT) return (Registry<T>) BuiltInRegistries.SOUND_EVENT;
        if (registryKey == Registries.PARTICLE_TYPE) return (Registry<T>) BuiltInRegistries.PARTICLE_TYPE;
        if (registryKey == Registries.MOB_EFFECT) return (Registry<T>) BuiltInRegistries.MOB_EFFECT;
        if (registryKey == Registries.DATA_COMPONENT_TYPE) return (Registry<T>) BuiltInRegistries.DATA_COMPONENT_TYPE;
        if (registryKey == Registries.STRUCTURE_TYPE) return (Registry<T>) BuiltInRegistries.STRUCTURE_TYPE;
        if (registryKey == Registries.STRUCTURE_PIECE) return (Registry<T>) BuiltInRegistries.STRUCTURE_PIECE;
        if (registryKey == Registries.FLUID) return (Registry<T>) BuiltInRegistries.FLUID;
        if (registryKey == Registries.ATTRIBUTE) return (Registry<T>) BuiltInRegistries.ATTRIBUTE;
        if (registryKey == Registries.CREATIVE_MODE_TAB) return (Registry<T>) BuiltInRegistries.CREATIVE_MODE_TAB;
        if (registryKey == Registries.RECIPE_TYPE) return (Registry<T>) BuiltInRegistries.RECIPE_TYPE;
        if (registryKey == Registries.RECIPE_SERIALIZER) return (Registry<T>) BuiltInRegistries.RECIPE_SERIALIZER;
        if (registryKey == Registries.POINT_OF_INTEREST_TYPE) return (Registry<T>) BuiltInRegistries.POINT_OF_INTEREST_TYPE;
        if (registryKey == Registries.VILLAGER_PROFESSION) return (Registry<T>) BuiltInRegistries.VILLAGER_PROFESSION;
        if (registryKey.equals(Registries.TRIGGER_TYPE)) return (Registry<T>) BuiltInRegistries.TRIGGER_TYPES;
        return null;
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String modId) { super(Registries.ITEM, modId); }
        @Override public <I extends Item> DeferredItem<I> register(String name, Supplier<I> supplier) {
            DeferredItem<I> holder = new DeferredItem<>(supplier);
            this.entries.add(new Entry<>(name, holder));
            return holder;
        }
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, I> factory) {
            return register(name, () -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
        }
        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<?> block) {
            return registerItem(name, properties -> new BlockItem(block.get(), properties));
        }
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String modId) { super(Registries.BLOCK, modId); }
        public <B extends Block> DeferredBlock<B> register(String name, Function<Identifier, B> factory) {
            DeferredBlock<B> holder = new DeferredBlock<>(() -> factory.apply(id(name)));
            this.entries.add(new Entry<>(name, holder));
            return holder;
        }
    }

    
    private static final class Entry<R, V extends R> {
        private final String name;
        private final DeferredHolder<R, V> holder;
        private Entry(String name, DeferredHolder<R, V> holder) { this.name = name; this.holder = holder; }
    }
}
