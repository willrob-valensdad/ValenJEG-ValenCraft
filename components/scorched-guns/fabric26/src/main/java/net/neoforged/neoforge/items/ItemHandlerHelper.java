package net.neoforged.neoforge.items;
import net.minecraft.world.item.ItemStack;
public final class ItemHandlerHelper {
    private ItemHandlerHelper() {}
    public static ItemStack copyStackWithSize(ItemStack stack, int size) { return stack.copyWithCount(size); }
    public static boolean canItemStacksStack(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameComponents(a, b); }
}
