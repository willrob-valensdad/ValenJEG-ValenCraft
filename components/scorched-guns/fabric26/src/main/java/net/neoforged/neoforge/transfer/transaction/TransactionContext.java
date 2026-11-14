package net.neoforged.neoforge.transfer.transaction;
/** Distinguishes a simulated transfer from one that changes the handler. */
public interface TransactionContext {
    boolean simulating();
    TransactionContext EXECUTE = () -> false;
    TransactionContext SIMULATE = () -> true;
}
