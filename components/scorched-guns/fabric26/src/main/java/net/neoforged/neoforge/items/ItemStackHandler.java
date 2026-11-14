package net.neoforged.neoforge.items;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
public class ItemStackHandler implements IItemHandlerModifiable, ValueIOSerializable {
    private final List<ItemStack> stacks = new ArrayList<>();
    public ItemStackHandler() { this(1); }
    public ItemStackHandler(int size) { setSize(size); }
    public void setSize(int size) {
        stacks.clear();
        for (int i = 0; i < size; i++) stacks.add(ItemStack.EMPTY);
    }
    @Override public int getSlots() { return stacks.size(); }
    @Override public ItemStack getStackInSlot(int slot) { return stacks.get(slot); }
    @Override public void setStackInSlot(int slot, ItemStack stack) { stacks.set(slot, stack == null ? ItemStack.EMPTY : stack); }
    @Override public int getSlotLimit(int slot) { return 64; }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return stack;
        int room = limit - existing.getCount();
        if (room <= 0) return stack;
        int moved = Math.min(room, stack.getCount());
        if (!simulate) {
            ItemStack result = stack.copyWithCount(existing.getCount() + moved);
            stacks.set(slot, result);
            onContentsChanged(slot);
        }
        return stack.copyWithCount(stack.getCount() - moved);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int moved = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copyWithCount(moved);
        if (!simulate) {
            stacks.set(slot, existing.getCount() == moved ? ItemStack.EMPTY : existing.copyWithCount(existing.getCount() - moved));
            onContentsChanged(slot);
        }
        return extracted;
    }
    protected void onContentsChanged(int slot) {}
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Slot", i);
                list.add(tag);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", list);
        return tag;
    }
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {}

    @Override public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStack> list = output.list("Items", ItemStack.CODEC);
        for (ItemStack stack : stacks) list.add(stack == null ? ItemStack.EMPTY : stack);
    }
    @Override public void deserialize(ValueInput input) {
        int index = 0;
        for (ItemStack stack : input.listOrEmpty("Items", ItemStack.CODEC)) {
            if (index < stacks.size()) stacks.set(index, stack);
            index++;
        }
    }
}
