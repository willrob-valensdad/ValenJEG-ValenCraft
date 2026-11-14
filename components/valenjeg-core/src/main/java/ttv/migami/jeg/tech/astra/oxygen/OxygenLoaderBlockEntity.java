package ttv.migami.jeg.tech.astra.oxygen;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import za.co.neroland.nerospace.platform.GasLookup;

public final class OxygenLoaderBlockEntity extends BlockEntity implements WorldlyContainer {
    private final ttv.migami.jeg.tech.astra.oxygen.ui.OxygenSideConfiguration sideConfig=new ttv.migami.jeg.tech.astra.oxygen.ui.OxygenSideConfiguration();
    public ttv.migami.jeg.tech.astra.oxygen.ui.OxygenSideConfiguration sideConfig(){return sideConfig;}
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3,ItemStack.EMPTY);
    private final ContainerStorage slots = ContainerStorage.of(this,null);
    private final OxygenLoaderProcess process = new OxygenLoaderProcess(this::setChanged);
    private final ttv.migami.jeg.tech.astra.oxygen.ui.OxygenGasIntake gasIntake=new ttv.migami.jeg.tech.astra.oxygen.ui.OxygenGasIntake(process);
    public ttv.migami.jeg.tech.astra.oxygen.ui.OxygenGasIntake gasIntake(){return gasIntake;}
    private final NeroOxygenOutput oxygen = new NeroOxygenOutput(process);
    private final Storage<FluidVariant> waterSided = new WaterInput(OxygenLoaderProcess.FLUID_TRANSFER_MB * OxygenLoaderProcess.DROPLETS_PER_MB);
    private final Storage<FluidVariant> waterContainer = new WaterInput(OxygenLoaderProcess.WATER_CAPACITY);
    public OxygenLoaderBlockEntity(BlockPos pos,BlockState state) { super(AstraOxygen.Content.ENTITY,pos,state); }
    public OxygenLoaderProcess process() { return process; }
    public NeroOxygenOutput oxygenOutput() { return oxygen; }
    public Storage<FluidVariant> waterInput(boolean container) { return container ? waterContainer : waterSided; }

    private final class WaterInput implements Storage<FluidVariant>, StorageView<FluidVariant> {
        private final long maximum;
        WaterInput(long maximum) { this.maximum=maximum; }
        @Override public boolean supportsExtraction() { return false; }
        @Override public long insert(FluidVariant variant,long amount,TransactionContext transaction) {
            if (amount < 0) throw new IllegalArgumentException("Negative water transfer");
            // Fabric normalizes flowing water to its source; first slice admits plain water.
            return variant.equals(FluidVariant.of(Fluids.WATER))
                ? process.insertWater(Math.min(amount,maximum),transaction) : 0;
        }
        @Override public long extract(FluidVariant variant,long amount,TransactionContext transaction) {
            if (amount < 0) throw new IllegalArgumentException("Negative water transfer"); return 0;
        }
        @Override public boolean isResourceBlank() { return process.water()==0; }
        @Override public FluidVariant getResource() { return process.water()==0 ? FluidVariant.blank() : FluidVariant.of(Fluids.WATER); }
        @Override public long getAmount() { return process.water(); }
        @Override public long getCapacity() { return OxygenLoaderProcess.WATER_CAPACITY; }
        @Override public Iterator<StorageView<FluidVariant>> iterator() { return List.<StorageView<FluidVariant>>of(this).iterator(); }
    }
    public static void tick(Level level,BlockPos pos,BlockState state,OxygenLoaderBlockEntity loader) {
        loader.pullEnergy();
        loader.dischargeBattery();
        loader.emptyWaterContainer();
        loader.pullGasInput();
        loader.pullWater();
        boolean worked;
        try (var transaction=Transaction.openOuter()) {
            long before=loader.process.energy();
            if(loader.process.inputGas()>0)loader.process.stepPassthrough(ttv.migami.jeg.tech.astra.oxygen.ui.OxygenPassthroughRecipe.active(),transaction);
            else loader.process.step(OxygenLoadingRecipe.active(),transaction);
            worked=loader.process.energy()<before;
            transaction.commit();
        }
        loader.pushOxygen();
        if (state.getValue(OxygenLoaderBlock.ACTIVE)!=worked)
            level.setBlock(pos,state.setValue(OxygenLoaderBlock.ACTIVE,worked),3);
    }
    private void pullEnergy() {
        if (level==null || process.energy()>=OxygenLoaderProcess.ENERGY_CAPACITY) return;
        for(Direction side:Direction.values()) {
            if(!sideConfig.allows(2,side))continue;
            BlockPos target=worldPosition.relative(side);
            if (!level.hasChunkAt(target)) continue;
            EnergyStorage source=EnergyStorage.SIDED.find(level,target,side.getOpposite());
            if (source==null) continue;
            try(var transaction=Transaction.openOuter()) {
                EnergyStorageUtil.move(source,process.energyInput(),OxygenLoaderProcess.ENERGY_TRANSFER,transaction);
                transaction.commit();
            }
        }
    }
    private void dischargeBattery() {
        if (inventory.get(0).isEmpty()) return;
        EnergyStorage source=ContainerItemContext.ofSingleSlot(slots.getSlot(0)).find(EnergyStorage.ITEM);
        if (source==null) return;
        try(var transaction=Transaction.openOuter()) {
            EnergyStorageUtil.move(source,process.energyInput(),OxygenLoaderProcess.ENERGY_TRANSFER,transaction);
            transaction.commit();
        }
    }
    /** Source isolated-one-item semantics with atomic real input/output inventory changes. */
    private void emptyWaterContainer() {
        ItemStack original=inventory.get(1);
        if (original.isEmpty()) return;
        var isolated=new SimpleContainer(original.copyWithCount(1));
        var context=ContainerItemContext.ofSingleSlot(ContainerStorage.of(isolated,null).getSlot(0));
        var fluids=context.find(FluidStorage.ITEM);
        if (fluids==null) return;
        try(var transaction=Transaction.openOuter()) {
            long moved=StorageUtil.move(fluids,waterContainer,v->v.equals(FluidVariant.of(Fluids.WATER)),
                OxygenLoaderProcess.WATER_CAPACITY,transaction);
            if (moved==0) return;
            ItemStack remainder=isolated.getItem(0);
            if (slots.getSlot(1).extract(ItemVariant.of(original),1,transaction)!=1) return;
            if (!remainder.isEmpty() && slots.getSlot(2).insert(ItemVariant.of(remainder),remainder.getCount(),transaction)!=remainder.getCount()) return;
            transaction.commit();
        }
    }
    private void pullWater() {
        if (level==null || process.water()>=OxygenLoaderProcess.WATER_CAPACITY) return;
        for(Direction side:Direction.values()) {
            if(!sideConfig.allows(3,side))continue;
            BlockPos target=worldPosition.relative(side);
            if (!level.hasChunkAt(target)) continue;
            Storage<FluidVariant> source=FluidStorage.SIDED.find(level,target,side.getOpposite());
            if (source==null) continue;
            try(var transaction=Transaction.openOuter()) {
                StorageUtil.move(source,waterSided,v->v.equals(FluidVariant.of(Fluids.WATER)),
                    OxygenLoaderProcess.FLUID_TRANSFER_MB*OxygenLoaderProcess.DROPLETS_PER_MB,transaction);
                transaction.commit();
            }
        }
    }
    private void pullGasInput(){
        if(level==null||process.water()>0||process.inputGas()>=OxygenLoaderProcess.TANK_MB)return;
        for(Direction side:Direction.values())if(sideConfig.allows(5,side)){var pos=worldPosition.relative(side);if(level.hasChunkAt(pos))gasIntake.pullFrom(GasLookup.INSTANCE.find(level,pos,side.getOpposite()));}
    }
    private void pushOxygen() {
        if (level==null || process.oxygen()==0) return;
        for(Direction side:Direction.values()) {
            if(!sideConfig.allows(4,side))continue;
            BlockPos target=worldPosition.relative(side);
            if (level.hasChunkAt(target)) oxygen.pushTo(GasLookup.INSTANCE.find(level,target,side.getOpposite()),OxygenLoaderProcess.FLUID_TRANSFER_MB);
        }
    }
    private static boolean waterContainer(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var storage=ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
        if (storage==null) return false;
        for(var view:storage) if (view.getAmount()>0 && view.getResource().equals(FluidVariant.of(Fluids.WATER))) return true;
        return false;
    }
    public void insertHeld(ItemStack held) {
        int slot=EnergyStorageUtil.isEnergyStorage(held)?0:waterContainer(held)?1:-1;
        if(slot<0) return;
        ItemStack current=inventory.get(slot);
        if(!current.isEmpty() && !ItemStack.isSameItemSameComponents(current,held)) return;
        int amount=Math.min(held.getCount(),held.getMaxStackSize()-current.getCount());
        if(amount<=0) return;
        if(current.isEmpty()) inventory.set(slot,held.copyWithCount(amount)); else current.grow(amount);
        held.shrink(amount);setChanged();
    }
    public void retrieve(Player player) {
        for(int slot=0;slot<inventory.size();slot++) {
            ItemStack stack=removeItemNoUpdate(slot);
            if(stack.isEmpty()) continue;
            player.getInventory().add(stack);
            if(!stack.isEmpty()) player.drop(stack,false,Prediction.SERVER_ONLY);
        }
        setChanged();
    }
    public void inspect(Player player) {
        OxygenLoadingRecipe recipe=OxygenLoadingRecipe.active();
        player.sendOverlayMessage(Component.literal("Oxygen Loader: "+process.energy()+"/20000 E | water "+process.water()/OxygenLoaderProcess.DROPLETS_PER_MB+
            "/3000 mB | oxygen "+process.oxygen()+"/3000 mB | "+(recipe==null?"recipe unavailable":recipe.energy()+" E + "+recipe.input().amount()+" mB water -> "+recipe.result().amount()+" mB oxygen")));
        player.sendSystemMessage(Component.literal("Insert a battery or water container; empty hand inspects; sneak with empty hands retrieves. Pipe oxygen into a Nero Core gas tank for native suit replenishment."));
    }
    @Override public int getContainerSize(){return inventory.size();}
    @Override public boolean isEmpty(){return inventory.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){return inventory.get(slot);}
    @Override public ItemStack removeItem(int slot,int amount){ItemStack stack=ContainerHelper.removeItem(inventory,slot,amount);if(!stack.isEmpty())setChanged();return stack;}
    @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(inventory,slot);}
    @Override public void setItem(int slot,ItemStack stack){inventory.set(slot,stack);stack.limitSize(getMaxStackSize(stack));setChanged();}
    @Override public boolean stillValid(Player player){return Container.stillValidBlockEntity(this,player);}
    @Override public void clearContent(){for(int slot=0;slot<inventory.size();slot++)inventory.set(slot,ItemStack.EMPTY);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==2 || slot==0 && EnergyStorageUtil.isEnergyStorage(stack) || slot==1 && waterContainer(stack);}
    @Override public int[] getSlotsForFace(Direction side){return sideConfig.allows(0,side)?(sideConfig.allows(1,side)?new int[]{1,2}:new int[]{1}):(sideConfig.allows(1,side)?new int[]{2}:new int[0]);}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return slot==1 && sideConfig.allows(0,side) && waterContainer(stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return slot==2 && sideConfig.allows(1,side);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output.child("Inventory"),inventory);process.save(output.child("Processing"));sideConfig.save(output.child("Sides"));}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);clearContent();input.child("Inventory").ifPresent(child->ContainerHelper.loadAllItems(child,inventory));process.load(input.childOrEmpty("Processing"));sideConfig.load(input.childOrEmpty("Sides"));}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level!=null && !level.isClientSide()){Containers.dropContents(level,pos,this);clearContent();}
        super.preRemoveSideEffects(pos,state);
    }
}
