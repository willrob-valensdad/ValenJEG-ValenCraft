package top.ribs.scguns.compat.transfer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Temporary, transaction-safe bridge used while Scorched Guns migrates its internal
 * 1.20/1.21-era item handlers to NeoForge 26.2's ResourceHandler API.
 *
 * <p>Capability boundaries are modern ({@code ResourceHandler<ItemResource>}). Existing
 * internal inventories can remain on {@code IItemHandlerModifiable} for the moment.
 * This class should be removed once those internal handlers are migrated natively.</p>
 */
@SuppressWarnings("removal")
public final class LegacyItemTransferBridge {
    private LegacyItemTransferBridge() {}

    /** Wrap a Scorched Guns legacy modifiable inventory as a modern item resource handler. */
    public static ResourceHandler<ItemResource> wrap(IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        if (!(handler instanceof IItemHandlerModifiable modifiable)) {
            throw new IllegalArgumentException(
                    "Scorched Guns tried to expose a non-modifiable legacy item handler through the 26.2 transfer API: "
                            + handler.getClass().getName());
        }
        return new ModifiableItemHandlerAdapter(modifiable);
    }

    /**
     * Present another mod's modern item capability to still-legacy Scorched Guns internals.
     * NeoForge's own compatibility adapter opens transactions for mutating operations.
     */
    public static IItemHandler legacy(ResourceHandler<ItemResource> handler) {
        return handler == null ? null : IItemHandler.of(handler);
    }

    private static final class ModifiableItemHandlerAdapter
            extends SnapshotJournal<List<ItemStack>>
            implements ResourceHandler<ItemResource> {
        private final IItemHandlerModifiable delegate;

        private ModifiableItemHandlerAdapter(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(delegate.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return delegate.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (!resource.isEmpty() && !isValid(index, resource)) {
                return 0L;
            }
            return delegate.getSlotLimit(index);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && delegate.isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty()) {
                throw new IllegalArgumentException("Cannot insert an empty ItemResource");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("Cannot insert a negative item amount: " + amount);
            }
            if (amount == 0) {
                return 0;
            }

            ItemStack simulatedRemainder = delegate.insertItem(index, resource.toStack(amount), true);
            int insertable = amount - simulatedRemainder.getCount();
            if (insertable <= 0) {
                return 0;
            }

            if (transaction != null && transaction.simulating()) return insertable;
            updateSnapshots(transaction);
            ItemStack remainder = delegate.insertItem(index, resource.toStack(insertable), false);
            return insertable - remainder.getCount();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty()) {
                throw new IllegalArgumentException("Cannot extract an empty ItemResource");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("Cannot extract a negative item amount: " + amount);
            }
            if (amount == 0) {
                return 0;
            }

            ItemStack simulated = delegate.extractItem(index, amount, true);
            if (simulated.isEmpty() || !resource.matches(simulated)) {
                return 0;
            }

            int extractable = Math.min(amount, simulated.getCount());
            if (transaction != null && transaction.simulating()) return extractable;
            updateSnapshots(transaction);
            ItemStack extracted = delegate.extractItem(index, extractable, false);
            if (extracted.isEmpty() || !resource.matches(extracted)) {
                return 0;
            }
            return extracted.getCount();
        }

        @Override
        protected List<ItemStack> createSnapshot() {
            List<ItemStack> snapshot = new ArrayList<>(delegate.getSlots());
            for (int slot = 0; slot < delegate.getSlots(); slot++) {
                snapshot.add(delegate.getStackInSlot(slot).copy());
            }
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            int slots = Math.min(delegate.getSlots(), snapshot.size());
            for (int slot = 0; slot < slots; slot++) {
                delegate.setStackInSlot(slot, snapshot.get(slot).copy());
            }
        }
    }
}
