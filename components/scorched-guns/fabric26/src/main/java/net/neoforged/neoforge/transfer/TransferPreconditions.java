package net.neoforged.neoforge.transfer;
public final class TransferPreconditions {
    private TransferPreconditions() {}
    public static void checkNonNegative(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non-negative, was " + amount);
    }
}
