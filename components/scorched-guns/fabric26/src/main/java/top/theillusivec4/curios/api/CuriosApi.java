package top.theillusivec4.curios.api;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
/** Curios slot calls move items in the installed Trinkets inventory. */
public final class CuriosApi {
    private CuriosApi() {}
    public static Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity entity) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return Optional.empty();
        return Optional.of(() -> new TrinketSlots(attachment));
    }
    private static final class TrinketSlots implements IItemHandlerModifiable {
        private final List<TrinketSlotAccess> slots = new ArrayList<>();
        private TrinketSlots(TrinketAttachment attachment) {
            attachment.getInventory().values().forEach(group -> group.values().forEach(inventory -> {
                for (int i = 0; i < inventory.getContainerSize(); i++) slots.add(inventory.getSlotAccess(i));
            }));
        }
        @Override public int getSlots() { return slots.size(); }
        @Override public ItemStack getStackInSlot(int slot) { return slots.get(slot).get(); }
        @Override public void setStackInSlot(int slot, ItemStack stack) { slots.get(slot).set(stack == null ? ItemStack.EMPTY : stack); }
        @Override public int getSlotLimit(int slot) {
            ItemStack current = getStackInSlot(slot);
            return slots.get(slot).maxStackSize(current.isEmpty() ? ItemStack.EMPTY : current);
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slots.get(slot).isValid() && slots.get(slot).maxStackSize(stack) > 0; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
            TrinketSlotAccess access = slots.get(slot);
            ItemStack existing = access.get();
            int limit = Math.min(access.maxStackSize(stack), stack.getMaxStackSize());
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return stack;
            int room = limit - existing.getCount();
            if (room <= 0) return stack;
            int moved = Math.min(room, stack.getCount());
            if (!simulate) {
                ItemStack stored = stack.copyWithCount(existing.getCount() + moved);
                if (!access.set(stored)) return stack;
            }
            return stack.copyWithCount(stack.getCount() - moved);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            TrinketSlotAccess access = slots.get(slot);
            ItemStack existing = access.get();
            if (existing.isEmpty()) return ItemStack.EMPTY;
            int moved = Math.min(amount, existing.getCount());
            ItemStack extracted = existing.copyWithCount(moved);
            if (!simulate) {
                ItemStack remain = existing.getCount() == moved ? ItemStack.EMPTY : existing.copyWithCount(existing.getCount() - moved);
                if (!access.set(remain)) return ItemStack.EMPTY;
            }
            return extracted;
        }
    }
}
