package ttv.migami.jeg.tech.astra.oxygen;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;

/**
 * Ad Astra MIT water/energy processing adapted to one physical transactional state.
 * Water is in Fabric droplets; oxygen is in the existing Nero gas API's mB.
 * Native item/fluid lookup stays outside this class and requires proper Fabric.
 */
public final class OxygenLoaderProcess extends SnapshotParticipant<OxygenLoaderProcess.State> {
    public static final long ENERGY_CAPACITY = 20000, ENERGY_TRANSFER = 150, TANK_MB = 3000;
    public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;
    public static final long WATER_CAPACITY = TANK_MB * DROPLETS_PER_MB;
    public static final long FLUID_TRANSFER_MB = 200;
    public record State(long energy, long water, long oxygen, int progress,long inputGas) {}
    private long energy, water, oxygen, inputGas;
    private int progress;
    private final Runnable changed;
    private final EnergyStorage input = new EnergyStorage() {
        @Override public boolean supportsExtraction() { return false; }
        @Override public long getAmount() { return energy; }
        @Override public long getCapacity() { return ENERGY_CAPACITY; }
        @Override public long insert(long maximum, TransactionContext transaction) {
            nonnegative(maximum);
            long accepted = Math.min(Math.min(maximum, ENERGY_TRANSFER), ENERGY_CAPACITY - energy);
            if (accepted > 0) { updateSnapshots(transaction); energy += accepted; }
            return accepted;
        }
        @Override public long extract(long maximum, TransactionContext transaction) { nonnegative(maximum); return 0; }
    };
    public OxygenLoaderProcess(Runnable changed) {
        if (FluidConstants.BUCKET % 1000 != 0) throw new IllegalStateException("Unsupported native mB conversion");
        this.changed = changed;
    }
    public EnergyStorage energyInput() { return input; }
    public long energy() { return energy; }
    public long water() { return water; }
    public long oxygen() { return oxygen; }
    public int progress() { return progress; }
    public State state() { return new State(energy, water, oxygen, progress,inputGas); }
    public long inputGas(){return inputGas;}
    public long insertGas(long maximum,TransactionContext transaction){nonnegative(maximum);long accepted=water>0?0:Math.min(maximum,TANK_MB-inputGas);if(accepted>0){updateSnapshots(transaction);inputGas+=accepted;}return accepted;}
    public boolean stepPassthrough(OxygenLoadingRecipe recipe,TransactionContext transaction){
        if(recipe==null||energy<recipe.energy()||inputGas<recipe.input().amount()||TANK_MB-oxygen<recipe.result().amount()){if(progress!=0){updateSnapshots(transaction);progress=0;}return false;}
        updateSnapshots(transaction);energy-=recipe.energy();if(++progress<recipe.cookingTime())return false;inputGas-=recipe.input().amount();oxygen+=recipe.result().amount();progress=0;return true;
    }
    public long insertWater(long maximum, TransactionContext transaction) {
        nonnegative(maximum);
        long accepted = inputGas>0?0:Math.min(maximum, WATER_CAPACITY - water);
        if (accepted > 0) { updateSnapshots(transaction); water += accepted; }
        return accepted;
    }
    public long extractOxygen(long maximum, TransactionContext transaction) {
        nonnegative(maximum);
        long extracted = Math.min(maximum, oxygen);
        if (extracted > 0) { updateSnapshots(transaction); oxygen -= extracted; }
        return extracted;
    }
    /** Energy is charged each working tick; water and oxygen change together on craft. */
    public boolean step(OxygenLoadingRecipe recipe, TransactionContext transaction) {
        if (recipe == null || energy < recipe.energy()
            || water < recipe.input().amount() * DROPLETS_PER_MB
            || TANK_MB - oxygen < recipe.result().amount()) {
            if (progress != 0) { updateSnapshots(transaction); progress = 0; }
            return false;
        }
        updateSnapshots(transaction);
        energy -= recipe.energy();
        progress++;
        if (progress < recipe.cookingTime()) return false;
        water -= recipe.input().amount() * DROPLETS_PER_MB;
        oxygen += recipe.result().amount();
        progress = 0;
        return true;
    }
    public void save(ValueOutput output) {
        output.putLong("Energy", energy); output.putLong("WaterDroplets", water);
        output.putLong("OxygenMb", oxygen); output.putInt("CookTime", progress);output.putLong("InputOxygenMb",inputGas);
    }
    public void load(ValueInput input) {
        energy = Math.clamp(input.getLongOr("Energy", 0), 0, ENERGY_CAPACITY);
        water = Math.clamp(input.getLongOr("WaterDroplets", 0), 0, WATER_CAPACITY);
        oxygen = Math.clamp(input.getLongOr("OxygenMb", 0), 0, TANK_MB);
        progress = Math.clamp(input.getIntOr("CookTime", 0), 0, 20000);
        inputGas=water>0?0:Math.clamp(input.getLongOr("InputOxygenMb",0),0,TANK_MB);
    }
    private static void nonnegative(long value) { if (value < 0) throw new IllegalArgumentException("Negative transfer"); }
    @Override protected State createSnapshot() { return state(); }
    @Override protected void readSnapshot(State value) { energy=value.energy; water=value.water; oxygen=value.oxygen; progress=value.progress; inputGas=value.inputGas; }
    @Override protected void onFinalCommit() { changed.run(); }
}
