package top.ribs.scguns.fabric.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.item.StackCraftingRemainder;

@Mixin(CraftingRecipe.class)
public interface ScorchedCraftingRemainderMixin {
    @Inject(method = "defaultCraftingReminder", at = @At("HEAD"), cancellable = true)
    private static void scguns$remainder(CraftingInput input, CallbackInfoReturnable<NonNullList<ItemStack>> callback) {
        boolean custom = false;
        for (int slot = 0; slot < input.size(); slot++) {
            if (input.getItem(slot).getItem() instanceof StackCraftingRemainder) {
                custom = true;
                break;
            }
        }
        if (!custom) return;
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < result.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            Item item = stack.getItem();
            ItemStackTemplate remainder = item instanceof StackCraftingRemainder customItem
                    ? customItem.getCraftingRemainder(stack)
                    : item.getCraftingRemainder();
            result.set(slot, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
        callback.setReturnValue(result);
    }
}
