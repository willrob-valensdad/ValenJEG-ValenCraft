package top.ribs.scguns.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import org.jetbrains.annotations.NotNull;

public class MoldItem extends Item implements StackCraftingRemainder {

    public MoldItem(Properties properties) {
        super(properties);
    }

        public boolean isDamageable(ItemStack stack) {
        return true;
    }

    // Return a damaged copy after crafting; null means the mold broke on this use.
    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        ItemStack remainingItem = instance instanceof ItemStack stack
                ? stack.copy()
                : ((ItemStackTemplate) instance).create();
        remainingItem.setDamageValue(remainingItem.getDamageValue() + 1);
        if (remainingItem.getDamageValue() >= remainingItem.getMaxDamage()) {
            return null;
        }
        return ItemStackTemplate.fromStack(remainingItem);
    }
}
