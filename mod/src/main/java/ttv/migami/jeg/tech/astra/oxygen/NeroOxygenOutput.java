package ttv.migami.jeg.tech.astra.oxygen;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import za.co.neroland.nerospace.gas.GasResource;
import za.co.neroland.nerospace.gas.NerospaceGasStorage;
import za.co.neroland.nerospace.storage.CoreTankBridge;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/** Existing gas identities and official Core adapter; creates no fluid or oxygen platform. */
public final class NeroOxygenOutput {
    private final OxygenLoaderProcess process;
    private final NerospaceGasStorage space = new NerospaceGasStorage() {
        @Override public GasResource getGas() { return process.oxygen() == 0 ? GasResource.EMPTY : GasResource.OXYGEN; }
        @Override public long getAmount() { return process.oxygen(); }
        @Override public long getCapacity() { return OxygenLoaderProcess.TANK_MB; }
        @Override public long fill(GasResource gas, long maximum, boolean simulate) { return 0; }
        @Override public long drain(long maximum, boolean simulate) {
            long amount = Math.min(Math.max(0, maximum), process.oxygen());
            if (amount == 0 || simulate) return amount;
            try (var transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                long moved = process.extractOxygen(amount, transaction);
                transaction.commit();
                return moved;
            }
        }
    };
    private final NeroGasStorage core;
    public NeroOxygenOutput(OxygenLoaderProcess process) { this.process = process; core = CoreTankBridge.toCore(space); }
    public NerospaceGasStorage space() { return space; }
    public NeroGasStorage core() { return core; }
    public long pushTo(NerospaceGasStorage target, long maximum) {
        if (target == null || target == space) return 0;
        if (Transaction.getCurrentUnsafe() != null) throw new IllegalStateException("Native gas deposit cannot join a Fabric parent transaction");
        long requested = Math.min(Math.min(Math.max(0, maximum), OxygenLoaderProcess.FLUID_TRANSFER_MB), process.oxygen());
        if (requested == 0) return 0;
        long admitted = target.fill(GasResource.OXYGEN, requested, true);
        if (admitted <= 0) return 0;
        if (admitted > requested) throw new IllegalStateException("Native gas provider preview exceeds request");
        long actual = target.fill(GasResource.OXYGEN, admitted, false);
        if (actual < 0 || actual > admitted) throw new IllegalStateException("Native gas provider changed admission inconsistently");
        // The official Nero gas API is simulate/execute, not Fabric transactional.
        // Debit exactly its actual accepted amount; never claim a foreign tank rollback.
        long debited = space.drain(actual, false);
        if (debited != actual) throw new IllegalStateException("Oxygen buffer debit mismatch");
        return actual;
    }
}
