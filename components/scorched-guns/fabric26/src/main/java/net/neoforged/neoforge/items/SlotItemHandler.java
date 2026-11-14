package net.neoforged.neoforge.items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class SlotItemHandler extends Slot {
    private final IItemHandler handler;
    private final int index;
    public SlotItemHandler(IItemHandler handler, int index, int x, int y) {
        super(new net.minecraft.world.SimpleContainer(0), index, x, y);
        this.handler = handler;
        this.index = index;
    }
    @Override public boolean mayPlace(ItemStack stack) { return handler.isItemValid(index, stack); }
    @Override public ItemStack getItem() { return handler.getStackInSlot(index); }
    @Override public void set(ItemStack stack) { if (handler instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(index, stack); }
    @Override public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {}
    @Override public int getMaxStackSize() { return handler.getSlotLimit(index); }
    @Override public int getMaxStackSize(ItemStack stack) { return Math.min(stack.getMaxStackSize(), handler.getSlotLimit(index)); }
    @Override public boolean mayPickup(Player player) { return !handler.extractItem(index, 1, true).isEmpty(); }
    @Override public ItemStack remove(int amount) { return handler.extractItem(index, amount, false); }
}
