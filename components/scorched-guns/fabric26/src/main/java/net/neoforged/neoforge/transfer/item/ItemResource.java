package net.neoforged.neoforge.transfer.item;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
public final class ItemResource {
    private final ItemStack stack;
    private ItemResource(ItemStack stack) { this.stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy(); }
    public static ItemResource of(ItemStack stack) { return new ItemResource(stack); }
    public boolean isEmpty() { return stack.isEmpty(); }
    public ItemStack toStack() { return stack.copy(); }
    public ItemStack toStack(int count) { ItemStack copy = stack.copy(); copy.setCount(count); return copy; }
    public boolean matches(ItemStack other) { return ItemStack.isSameItemSameComponents(stack, other); }
    public <T> T get(DataComponentType<T> type) { return stack.get(type); }
}

