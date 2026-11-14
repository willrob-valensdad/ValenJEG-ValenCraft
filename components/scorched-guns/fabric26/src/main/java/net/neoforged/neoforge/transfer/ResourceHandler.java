package net.neoforged.neoforge.transfer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public interface ResourceHandler<T> {
    int size();
    T getResource(int index);
    long getAmountAsLong(int index);
    long getCapacityAsLong(int index, T resource);
    boolean isValid(int index, T resource);
    int insert(int index, T resource, int amount, TransactionContext transaction);
    int extract(int index, T resource, int amount, TransactionContext transaction);
}
