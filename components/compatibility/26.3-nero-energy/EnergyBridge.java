package dev.valen.compat.energy;

import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntConsumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import za.co.neroland.nerolandcore.energy.EnergyConversions;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.platform.FabricEnergyLookup;

/** Bridge only the two pinned Nero buffers; preserve their original sided admission rules. */
public final class EnergyBridge implements ModInitializer {
    private static final ConcurrentMap<NeroEnergyStorage, BufferSnapshot> BUFFERS =
        new MapMaker().weakKeys().weakValues().makeMap();
    private static volatile EnergyRate rate = EnergyRate.of(1.0);

    private static EnergyRate rate() {
        double configured = EnergyConversions.neroToForgeRatio();
        EnergyRate current = rate;
        if (current.configured() != configured) rate = current = EnergyRate.of(configured);
        return current;
    }
    private static boolean neroNamespace(String namespace) {
        return namespace.equals("nerospace") || namespace.equals("nerolandcore");
    }
    @Override public void onInitialize() {
        EnergyStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> {
            if (blockEntity == null || !neroNamespace(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace()))
                return null;
            // Null side returns the physical buffer. A real side returns the author's current gate.
            NeroEnergyStorage raw = FabricEnergyLookup.ENERGY.find(level, pos, state, blockEntity, null);
            NeroEnergyStorage view = FabricEnergyLookup.ENERGY.find(level, pos, state, blockEntity, side);
            if (view == null || !BufferSnapshot.supports(raw)) return null;
            BufferSnapshot snapshot = BUFFERS.computeIfAbsent(raw, key -> new BufferSnapshot(key, blockEntity));
            return new TeamView(view, snapshot, rate());
        });
        FabricEnergyLookup.ENERGY.registerFallback((level, pos, state, blockEntity, side) -> {
            // This also prevents the two fallback lookups recursing for an unsupported Nero block.
            if (neroNamespace(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace())) return null;
            EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, blockEntity, side);
            return storage == null ? null : new NeroView(storage, rate());
        });
    }

    private static final class BufferSnapshot extends SnapshotParticipant<Integer> {
        final NeroEnergyStorage raw;
        final IntConsumer restore;
        final BlockEntity owner;
        BufferSnapshot(NeroEnergyStorage raw, BlockEntity owner) {
            this.raw = raw;
            this.owner = owner;
            if (raw instanceof za.co.neroland.nerospace.energy.EnergyBuffer buffer) restore = buffer::setRaw;
            else restore = ((za.co.neroland.nerolandcore.energy.EnergyBuffer) raw)::setRaw;
        }
        static boolean supports(NeroEnergyStorage storage) {
            return storage instanceof za.co.neroland.nerospace.energy.EnergyBuffer
                || storage instanceof za.co.neroland.nerolandcore.energy.EnergyBuffer;
        }
        @Override protected Integer createSnapshot() { return (int) raw.getAmount(); }
        @Override protected void readSnapshot(Integer amount) { restore.accept(amount); }
        @Override protected void onFinalCommit() { owner.setChanged(); }
        void change(long delta, TransactionContext transaction) {
            updateSnapshots(transaction);
            restore.accept(Math.toIntExact(raw.getAmount() + delta));
        }
    }

    private record TeamView(NeroEnergyStorage gate, BufferSnapshot buffer, EnergyRate rate) implements EnergyStorage {
        @Override public boolean supportsInsertion() { return gate.canReceive(); }
        @Override public boolean supportsExtraction() { return gate.canExtract(); }
        @Override public long getAmount() { return rate.toTeam(buffer.raw.getAmount()); }
        @Override public long getCapacity() { return rate.toTeam(buffer.raw.getCapacity()); }
        @Override public long insert(long maximum, TransactionContext transaction) {
            if (maximum < 0) throw new IllegalArgumentException("Negative energy transfer");
            long accepted = rate.wholeNero(gate.insert(rate.toNero(maximum), true));
            if (accepted > 0) buffer.change(accepted, transaction);
            return rate.toTeam(accepted);
        }
        @Override public long extract(long maximum, TransactionContext transaction) {
            if (maximum < 0) throw new IllegalArgumentException("Negative energy transfer");
            long extracted = rate.wholeNero(gate.extract(rate.toNero(maximum), true));
            if (extracted > 0) buffer.change(-extracted, transaction);
            return rate.toTeam(extracted);
        }
    }

    private record NeroView(EnergyStorage storage, EnergyRate rate) implements NeroEnergyStorage {
        @Override public long getAmount() { return rate.toNero(storage.getAmount()); }
        @Override public long getCapacity() { return rate.toNero(storage.getCapacity()); }
        @Override public boolean canReceive() { return storage.supportsInsertion(); }
        @Override public boolean canExtract() { return storage.supportsExtraction(); }
        @Override public long insert(long maximum, boolean simulate) { return transfer(maximum, simulate, true); }
        @Override public long extract(long maximum, boolean simulate) { return transfer(maximum, simulate, false); }
        private long transfer(long maximum, boolean simulate, boolean insert) {
            if (maximum <= 0) return 0;
            // Preview inside a nested transaction, then commit only complete conversion units.
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                long possible;
                try (Transaction preview = transaction.openNested()) {
                    possible = insert ? storage.insert(rate.toTeam(maximum), preview)
                                      : storage.extract(rate.toTeam(maximum), preview);
                }
                long accepted = rate.wholeTeam(possible);
                if (accepted == 0 || simulate) return rate.toNero(accepted);
                long changed = insert ? storage.insert(accepted, transaction) : storage.extract(accepted, transaction);
                if (changed != accepted) return 0; // Abort if a provider changed its answer.
                transaction.commit();
                return rate.toNero(changed);
            }
        }
    }
}
