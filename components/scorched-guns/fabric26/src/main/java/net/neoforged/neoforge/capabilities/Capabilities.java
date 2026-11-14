package net.neoforged.neoforge.capabilities;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
/** Block and item capabilities registered by Scorched machines, guns, and the exo suit. */
public final class Capabilities {
    private static final Map<BlockKey, BiFunction<BlockEntity, Direction, Object>> BLOCKS = new HashMap<>();
    private static final Map<ItemKey, BiFunction<ItemStack, ItemAccess, Object>> ITEMS = new HashMap<>();
    private Capabilities() {}
    public static final class ItemCaps {
        public static final BlockCapability<ResourceHandler<ItemResource>, Direction> BLOCK = new BlockCapability<>();
    }
    public static final class Energy {
        public static final BlockCapability<EnergyHandler, Direction> BLOCK = new BlockCapability<>();
        public static final ItemCapability<EnergyHandler, ItemAccess> ITEM = new ItemCapability<>();
    }
    /** Source name used by the NeoForge 26.2 call sites. */
    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, Direction> BLOCK = ItemCaps.BLOCK;
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, C> void registerBlock(BlockCapability<T, C> capability, BlockEntityType<?> type, BiFunction<? extends BlockEntity, C, T> provider) {
        BiFunction raw = provider;
        BLOCKS.put(new BlockKey(capability, type), (entity, side) -> raw.apply(entity, side));
    }
    @SuppressWarnings("unchecked")
    public static <T, C> void registerItem(ItemCapability<T, C> capability, BiFunction<ItemStack, C, T> provider, net.minecraft.world.item.Item... items) {
        for (net.minecraft.world.item.Item item : items) {
            ITEMS.put(new ItemKey(capability, item), (stack, access) -> provider.apply(stack, (C) access));
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T lookup(Level level, BlockCapability<T, Direction> capability, BlockPos pos, Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) return null;
        BiFunction<BlockEntity, Direction, Object> provider = BLOCKS.get(new BlockKey(capability, entity.getType()));
        return provider == null ? null : (T) provider.apply(entity, side);
    }
    @SuppressWarnings("unchecked")
    public static <T> T lookupItem(ItemStack stack, ItemCapability<T, ItemAccess> capability, ItemAccess access) {
        BiFunction<ItemStack, ItemAccess, Object> provider = ITEMS.get(new ItemKey(capability, stack.getItem()));
        return provider == null ? null : (T) provider.apply(stack, access);
    }
    private record BlockKey(Object capability, BlockEntityType<?> type) {}
    private record ItemKey(Object capability, net.minecraft.world.item.Item item) {}
}
