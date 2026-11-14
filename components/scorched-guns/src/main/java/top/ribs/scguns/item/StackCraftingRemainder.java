package top.ribs.scguns.item;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;

/** Per-stack crafting remainder. 26.3 Item.getCraftingRemainder() is final and constant. */
public interface StackCraftingRemainder {
    ItemStackTemplate getCraftingRemainder(ItemInstance instance);
}
