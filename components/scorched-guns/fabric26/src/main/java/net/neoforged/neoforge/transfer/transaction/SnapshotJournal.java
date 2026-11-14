package net.neoforged.neoforge.transfer.transaction;
import java.util.IdentityHashMap;
import java.util.Map;
/** Records one snapshot per executing transaction so a later close can restore it. */
public abstract class SnapshotJournal<T> {
    private final Map<TransactionContext, T> snapshots = new IdentityHashMap<>();
    protected abstract T createSnapshot();
    protected abstract void revertToSnapshot(T snapshot);
    public void updateSnapshots(TransactionContext transaction) {
        if (transaction == null || transaction.simulating()) return;
        snapshots.computeIfAbsent(transaction, ignored -> createSnapshot());
    }
    public void revert(TransactionContext transaction) {
        T snapshot = snapshots.remove(transaction);
        if (snapshot != null) revertToSnapshot(snapshot);
    }
}
