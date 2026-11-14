package net.neoforged.neoforge.items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
    static IItemHandler of(ResourceHandler<ItemResource> handler) {
        return new IItemHandler() {
            private TransactionContext tx(boolean simulate) { return simulate ? TransactionContext.SIMULATE : TransactionContext.EXECUTE; }
            @Override public int getSlots() { return handler.size(); }
            @Override public ItemStack getStackInSlot(int slot) {
                int count = (int) Math.min(Integer.MAX_VALUE, handler.getAmountAsLong(slot));
                return count <= 0 ? ItemStack.EMPTY : handler.getResource(slot).toStack(count);
            }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                int moved = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx(simulate));
                if (moved <= 0) return stack;
                ItemStack remaining = stack.copy();
                remaining.shrink(moved);
                return remaining;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemResource resource = handler.getResource(slot);
                if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
                int moved = handler.extract(slot, resource, amount, tx(simulate));
                return moved <= 0 ? ItemStack.EMPTY : resource.toStack(moved);
            }
            @Override public int getSlotLimit(int slot) {
                return (int) Math.min(Integer.MAX_VALUE, handler.getCapacityAsLong(slot, handler.getResource(slot)));
            }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return stack.isEmpty() || handler.isValid(slot, ItemResource.of(stack));
            }
        };
    }
}
