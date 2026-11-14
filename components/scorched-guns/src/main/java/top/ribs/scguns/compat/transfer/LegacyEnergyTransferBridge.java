package top.ribs.scguns.compat.transfer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Transitional energy bridge for the Scorched Guns 26.2 port.
 *
 * <p>Block capability boundaries are exposed as NeoForge 26.2 {@link EnergyHandler}s while
 * the existing block entities may still keep their legacy {@link IEnergyStorage} internals.
 * Item energy is exposed natively through {@link ItemAccess}, preserving the existing
 * CUSTOM_DATA keys used by released Scorched Guns items.</p>
 */
@SuppressWarnings("removal")
public final class LegacyEnergyTransferBridge {
    private LegacyEnergyTransferBridge() {}

    /**
     * Wrap a legacy block energy storage as a transactional 26.2 EnergyHandler.
     * The same delegate is used for rollback.
     */
    public static EnergyHandler wrap(IEnergyStorage delegate) {
        return wrap(delegate, delegate);
    }

    /**
     * Wrap a side-restricted legacy storage while using the unrestricted backing storage
     * for transaction rollback. This is needed for output-only faces such as the Polar Generator.
     */
    public static EnergyHandler wrap(IEnergyStorage delegate, IEnergyStorage rollbackDelegate) {
        if (delegate == null) {
            return null;
        }
        return new LegacyBlockEnergyAdapter(delegate, rollbackDelegate == null ? delegate : rollbackDelegate);
    }

    /** Present a modern energy handler to still-legacy Scorched Guns code. */
    public static IEnergyStorage legacy(EnergyHandler handler) {
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    /**
     * Query an item's modern 26.2 energy capability and present it to legacy internal code.
     * ItemAccess.forStack is correct here because all callers already own the mutable ItemStack.
     */
    public static IEnergyStorage legacy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ItemAccess access = ItemAccess.forStack(stack);
        return legacy(access.getCapability(Capabilities.Energy.ITEM));
    }

    /**
     * Create a modern item energy handler backed by one integer stored in CUSTOM_DATA.
     * This intentionally preserves the old Scorched Guns item data format instead of
     * silently resetting energy on existing items/worlds.
     */
    public static EnergyHandler customData(ItemAccess access, String key, int capacity) {
        return new CustomDataItemEnergyHandler(access, key, capacity);
    }

    /** Stateless creative/infinite energy source. */
    public static EnergyHandler infinite(int capacity) {
        return new InfiniteEnergyHandler(capacity);
    }

    private static final class LegacyBlockEnergyAdapter
            extends SnapshotJournal<Integer>
            implements EnergyHandler {
        private final IEnergyStorage delegate;
        private final IEnergyStorage rollbackDelegate;

        private LegacyBlockEnergyAdapter(IEnergyStorage delegate, IEnergyStorage rollbackDelegate) {
            this.delegate = delegate;
            this.rollbackDelegate = rollbackDelegate;
        }

        @Override
        public long getAmountAsLong() {
            return delegate.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return delegate.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0) {
                return 0;
            }

            int insertable = Math.max(0, delegate.receiveEnergy(amount, true));
            if (insertable <= 0) {
                return 0;
            }

            if (transaction != null && transaction.simulating()) return insertable;
            updateSnapshots(transaction);
            return Math.max(0, delegate.receiveEnergy(insertable, false));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0) {
                return 0;
            }

            int extractable = Math.max(0, delegate.extractEnergy(amount, true));
            if (extractable <= 0) {
                return 0;
            }

            if (transaction != null && transaction.simulating()) return extractable;
            updateSnapshots(transaction);
            return Math.max(0, delegate.extractEnergy(extractable, false));
        }

        @Override
        protected Integer createSnapshot() {
            return rollbackDelegate.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            int current = rollbackDelegate.getEnergyStored();
            if (current < snapshot) {
                int restored = rollbackDelegate.receiveEnergy(snapshot - current, false);
                if (restored != snapshot - current) {
                    throw new IllegalStateException("Could not roll back legacy energy insertion completely");
                }
            } else if (current > snapshot) {
                int restored = rollbackDelegate.extractEnergy(current - snapshot, false);
                if (restored != current - snapshot) {
                    throw new IllegalStateException("Could not roll back legacy energy extraction completely");
                }
            }
        }
    }

    /**
     * Native 26.2 item handler. Mutations are applied through ItemAccess.exchange, so they
     * participate in the caller's transaction rather than mutating a temporary ItemStack copy.
     */
    private static final class CustomDataItemEnergyHandler implements EnergyHandler {
        private final ItemAccess access;
        private final String key;
        private final int capacity;

        private CustomDataItemEnergyHandler(ItemAccess access, String key, int capacity) {
            if (access == null) {
                throw new IllegalArgumentException("ItemAccess cannot be null for an item energy capability");
            }
            if (capacity < 0) {
                throw new IllegalArgumentException("Energy capacity cannot be negative: " + capacity);
            }
            this.access = access;
            this.key = key;
            this.capacity = capacity;
        }

        @Override
        public long getAmountAsLong() {
            return readStored();
        }

        @Override
        public long getCapacityAsLong() {
            return capacity;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int current = readStored();
            int inserted = Math.max(0, Math.min(capacity - current, amount));
            if (inserted <= 0) {
                return 0;
            }
            return writeStored(current + inserted, transaction) ? inserted : 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int current = readStored();
            int extracted = Math.min(current, amount);
            if (extracted <= 0) {
                return 0;
            }
            return writeStored(current - extracted, transaction) ? extracted : 0;
        }

        private int readStored() {
            ItemResource resource = access.getResource();
            CustomData customData = resource.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                return 0;
            }
            CompoundTag tag = customData.copyTag();
            return Math.max(0, tag.getIntOr(key, 0));
        }

        private boolean writeStored(int value, TransactionContext transaction) {
            ItemResource current = access.getResource();
            int count = access.getAmount();
            if (current.isEmpty() || count <= 0) {
                return false;
            }

            ItemStack updatedStack = current.toStack(count);
            CustomData.update(DataComponents.CUSTOM_DATA, updatedStack, tag -> tag.putInt(key, value));
            ItemResource updated = ItemResource.of(updatedStack);
            return access.exchange(updated, count, transaction) == count;
        }
    }

    private static final class InfiniteEnergyHandler implements EnergyHandler {
        private final int capacity;

        private InfiniteEnergyHandler(int capacity) {
            this.capacity = Math.max(0, capacity);
        }

        @Override
        public long getAmountAsLong() {
            return capacity;
        }

        @Override
        public long getCapacityAsLong() {
            return capacity;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return amount;
        }
    }
}
