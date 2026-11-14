package net.neoforged.neoforge.energy;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public interface IEnergyStorage {
    int receiveEnergy(int maxReceive, boolean simulate);
    int extractEnergy(int maxExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
    static IEnergyStorage of(EnergyHandler handler) {
        return new IEnergyStorage() {
            @Override public int receiveEnergy(int maxReceive, boolean simulate) {
                return handler.insert(maxReceive, simulate ? TransactionContext.SIMULATE : TransactionContext.EXECUTE);
            }
            @Override public int extractEnergy(int maxExtract, boolean simulate) {
                return handler.extract(maxExtract, simulate ? TransactionContext.SIMULATE : TransactionContext.EXECUTE);
            }
            @Override public int getEnergyStored() { return (int) Math.min(Integer.MAX_VALUE, handler.getAmountAsLong()); }
            @Override public int getMaxEnergyStored() { return (int) Math.min(Integer.MAX_VALUE, handler.getCapacityAsLong()); }
            @Override public boolean canExtract() { return handler.getAmountAsLong() > 0; }
            @Override public boolean canReceive() { return handler.getAmountAsLong() < handler.getCapacityAsLong(); }
        };
    }
}
