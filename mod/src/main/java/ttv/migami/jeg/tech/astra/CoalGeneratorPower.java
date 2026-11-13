package ttv.migami.jeg.tech.astra;

import java.util.function.IntSupplier;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Ad Astra 45abf0ec coal-generator state machine, adapted from MIT Java code.
 * See tech/ad-astra/NOTICE.md and upstream hashes. TeamReborn units are used
 * directly, matching the installed Nero bridge's configured 1:1 conversion.
 */
public final class CoalGeneratorPower extends SimpleEnergyStorage {
    public static final long CAPACITY = 10_000;
    public static final long GENERATION = 20;
    public static final long TRANSFER = 100;
    public static final int MAX_BURN = 20_000;
    private final Runnable changed;
    private int burnRemaining;
    private int burnTotal;

    public CoalGeneratorPower(Runnable changed) {
        super(CAPACITY, 0, TRANSFER);
        this.changed = changed;
    }

    /** A full buffer pauses fuel consumption, as in the pinned author implementation. */
    public boolean tick(IntSupplier burnTime, Runnable consumeFuel) {
        if (amount >= capacity) return false;
        if (burnRemaining > 0) {
            burnRemaining--;
            amount += Math.min(GENERATION, capacity - amount);
            changed.run();
            return true;
        }
        int fuelTicks = Math.min(MAX_BURN, Math.max(0, burnTime.getAsInt()));
        if (fuelTicks > 0) {
            consumeFuel.run();
            burnRemaining = burnTotal = fuelTicks;
            changed.run();
        }
        // The author's first tick consumes fuel; generation begins on the next tick.
        return false;
    }

    public int burnRemaining() { return burnRemaining; }
    public int burnTotal() { return burnTotal; }

    public void restore(long stored, int remaining, int total) {
        amount = Math.clamp(stored, 0, CAPACITY);
        burnTotal = Math.clamp(total, 0, MAX_BURN);
        burnRemaining = Math.clamp(remaining, 0, burnTotal);
    }

    @Override protected void onFinalCommit() { changed.run(); }

    /** A distinct read/extract-only view; external networks cannot fuel the generator. */
    public EnergyStorage output() { return this; }
}
