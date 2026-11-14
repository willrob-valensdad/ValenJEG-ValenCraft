package ttv.migami.jeg.tech.astra.oxygen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import team.reborn.energy.api.EnergyStorage;
import za.co.neroland.nerospace.config.NerospaceConfig;
import za.co.neroland.nerospace.gas.GasResource;
import za.co.neroland.nerospace.platform.GasLookup;
import za.co.neroland.nerospace.platform.Services;
import za.co.neroland.nerospace.world.OxygenManager;

/** Opt-in source only. No standalone launcher; root owns staging and the proper Fabric server. */
public final class OxygenLoaderWorldChecks {
    private static boolean started;
    private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
    private final MinecraftServer server;
    private final JsonObject report=new JsonObject();
    private final JsonArray passed=new JsonArray();
    private final Path output;
    private ServerLevel level;
    private BlockPos pos;
    private List<BlockPos> owned;
    private final List<BlockState> original=new ArrayList<>();
    private OxygenLoaderBlockEntity loader;
    private EnergyStorage box;
    private net.fabricmc.fabric.api.transfer.v1.storage.Storage<FluidVariant> waterTank;
    private za.co.neroland.nerospace.gas.NerospaceGasStorage gasTank;
    private NativePlayer player;
    private boolean done,receiptOwned,worldOwned,forced;
    private int phase,tick,firstTick,reserveSeed;
    private long batteryInitial,batteryBefore;
    private ItemStack retainedBattery=ItemStack.EMPTY;

    public static void start(MinecraftServer server){
        if(!Boolean.getBoolean("valen.astra.oxygen.probe"))return;
        if(started)throw new IllegalStateException("Oxygen fixture already started");
        started=true;
        var probe=new OxygenLoaderWorldChecks(server);
        try{probe.begin();}catch(Throwable failure){probe.finish(failure);}
        if(!probe.done)ServerTickEvents.END_SERVER_TICK.register(current->{
            if(current!=server||probe.done)return;
            try{probe.observe();}catch(Throwable failure){probe.finish(failure);}
        });
    }
    private OxygenLoaderWorldChecks(MinecraftServer server){
        this.server=server;output=server.getServerDirectory().resolve("valen-astra-oxygen-probe.json");
        report.add("passed",passed);report.addProperty("status","running");report.addProperty("started_utc",Instant.now().toString());
        report.addProperty("production_world_modified",false);
        report.addProperty("manual_loader_ticker_calls",0);
        report.addProperty("suit_callback_proof","Actual public OxygenManager.tick invoked on native Survival ServerPlayer; automatic connected-player scheduling is a separate proof");
    }
    private void require(boolean pass,String label){if(!pass)throw new AssertionError(label);}
    private void phase(int value){phase=value;tick=server.getTickCount();report.addProperty("phase",phase);write();}
    private int elapsed(){return server.getTickCount()-tick;}
    private void write(){try{Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}catch(Exception e){throw new IllegalStateException(e);}}
    private List<ItemEntity> drops(){return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));}
    private long drops(Item item){return drops().stream().filter(e->e.getItem().is(item)).mapToLong(e->e.getItem().getCount()).sum();}
    private void clearDrops(){for(var entity:drops())entity.discard();}
    private Item item(String id){var key=Identifier.parse(id);require(BuiltInRegistries.ITEM.containsKey(key),"Missing actual native item "+id);return BuiltInRegistries.ITEM.getValue(key);}
    private void place(BlockPos target,String id){
        var key=Identifier.parse(id);require(BuiltInRegistries.BLOCK.containsKey(key),"Missing actual native block "+id);
        level.setBlockAndUpdate(target,BuiltInRegistries.BLOCK.getValue(key).defaultBlockState());
    }
    private static final class NativePlayer extends ServerPlayer{
        NativePlayer(MinecraftServer server,ServerLevel level){
            super(server,level,new com.mojang.authlib.GameProfile(UUID.fromString("059d0207-b153-4676-b951-c7b10a98cc07"),"ValenOxygenCheck"),
                net.minecraft.server.level.ClientInformation.createDefault());
        }
        @Override public boolean hasDisconnected(){return false;}
        @Override public void sendSystemMessage(net.minecraft.network.chat.Component message){}
    }
    private void begin()throws Exception{
        require(server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath()),"Refusing oxygen test outside exact copied fixture");
        require(Boolean.getBoolean("valen.station.probe"),"Root station fixture opt-in required");
        require(!Files.exists(output),"Archive prior oxygen receipt before rerunning");
        receiptOwned=true;
        for(var entry:java.util.Map.of("product_sha256",AstraOxygen.class,"probe_sha256",OxygenLoaderWorldChecks.class).entrySet()){
            Path artifact=Path.of(entry.getValue().getProtectionDomain().getCodeSource().getLocation().toURI());
            require(Files.isRegularFile(artifact),"Native fixture code must come from its frozen JAR");
            report.addProperty(entry.getKey(),java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(artifact))));
        }
        require(OxygenLoadingRecipe.active()!=null,"Native oxygen recipe reload did not install the original water recipe");
        OxygenLoadingRecipe.active().validateBinding();
        require(OxygenLoadingRecipe.active().energy()==30&&OxygenLoadingRecipe.active().input().amount()==100&&OxygenLoadingRecipe.active().result().amount()==4,"Native water recipe does not match pinned source");
        level=server.getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse("nerospace:greenxertz")));
        require(level!=null&&OxygenManager.isAirless(level.dimension()),"Actual Nero airless Greenxertz level is unavailable");
        pos=new BlockPos(280,level.getMaxY()-8,280);
        owned=List.of(pos,pos.west(),pos.east(),pos.north(),pos.south(),pos.above(),pos.below());
        level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        for(BlockPos target:owned){require(level.getBlockState(target).isAir()&&level.getBlockEntity(target)==null,"Occupied copied-fixture position "+target);original.add(level.getBlockState(target));}
        require(drops().isEmpty(),"Unrelated item entities in oxygen test region");
        forced=level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);worldOwned=true;
        place(pos,"jeg:valen_oxygen_loader");
        require(level.getBlockEntity(pos) instanceof OxygenLoaderBlockEntity,"Real oxygen-loader block entity missing");
        loader=(OxygenLoaderBlockEntity)level.getBlockEntity(pos);
        player=new NativePlayer(server,level);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item("nerospace:oxygen_suit_helmet")));
        player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(item("nerospace:oxygen_suit_chestplate")));
        player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(item("nerospace:oxygen_suit_leggings")));
        player.setItemSlot(EquipmentSlot.FEET,new ItemStack(item("nerospace:oxygen_suit_boots")));
        require(OxygenManager.suitTier(player)!=OxygenManager.SuitTier.NONE,"Actual four-piece Nero suit was not recognized");
        reserveSeed=Math.max(1,Math.min(100,OxygenManager.maxOxygenFor(player)/2));
        report.addProperty("native_suited_reserve_capacity",OxygenManager.maxOxygenFor(player));
        report.addProperty("native_suit_test_seed",reserveSeed);
        nativeHazard(reserveSeed);
        require(Services.PLATFORM.getOxygen(player)<reserveSeed,"Empty-tank negative control is breathable/free-refilling; fixture is unsuitable");
        report.addProperty("empty_tank_negative_oxygen",Services.PLATFORM.getOxygen(player));
        ItemStack water=new ItemStack(Items.WATER_BUCKET);loader.insertHeld(water);
        require(water.isEmpty(),"Actual native water bucket did not enter original input slot");
        firstTick=server.getTickCount();phase(0);
    }
    private void nativeHazard(int initial){
        Services.PLATFORM.setOxygen(player,initial);
        player.tickCount=((player.tickCount/10)+1)*10;
        OxygenManager.tick(player);
    }
    private EnergyStorage battery(){
        var value=ContainerItemContext.ofSingleSlot(ContainerStorage.of(loader,null).getSlot(0)).find(EnergyStorage.ITEM);
        require(value!=null,"Actual TechReborn battery has no native item energy API");return value;
    }
    private long waterAmount(){
        long total=0;for(var view:waterTank)if(view.getResource().equals(FluidVariant.of(Fluids.WATER)))total+=view.getAmount();return total;
    }
    private void seedEnergy(EnergyStorage target,long amount){
        long remaining=amount;
        for(int n=0;remaining>0&&n<1000;n++)try(var transaction=Transaction.openOuter()){
            long moved=target.insert(remaining,transaction);require(moved>0,"Actual native power target rejected test seed");
            transaction.commit();remaining-=moved;
        }
        require(remaining==0,"Actual native power target could not hold seed");
    }
    private void reloadProcessing(long energy,long waterMb,long gasMb){
        CompoundTag tag=loader.saveWithFullMetadata(server.registryAccess());
        CompoundTag process=tag.getCompoundOrEmpty("Processing");
        process.putLong("Energy",energy);process.putLong("WaterDroplets",waterMb*OxygenLoaderProcess.DROPLETS_PER_MB);process.putLong("OxygenMb",gasMb);process.putInt("CookTime",0);
        tag.put("Processing",process);
        var restored=BlockEntity.loadStatic(pos,loader.getBlockState(),tag,server.registryAccess());
        require(restored instanceof OxygenLoaderBlockEntity,"Native oxygen entity metadata failed load");
        level.setBlockEntity(restored);loader=(OxygenLoaderBlockEntity)restored;
    }
    private void observe()throws Exception{
        require(server.getTickCount()-firstTick<=800&&elapsed()<=140,"Oxygen fixture native tick phase timed out: "+phase);
        switch(phase){
            case 0->{
                if(loader.getItem(2).isEmpty())return;
                require(loader.getItem(1).isEmpty()&&loader.getItem(2).is(Items.BUCKET)&&loader.getItem(2).getCount()==1,"Water bucket remainder did not reach source empty-container output");
                require(loader.process().water()==FluidConstants.BUCKET&&loader.process().energy()==0&&loader.process().oxygen()==0,"Unpowered bucket load generated gas or lost water");
                passed.add("Actual native water bucket fills 1000 mB; empty bucket returned; unpowered machine produces no oxygen");
                place(pos.west(),"techreborn:low_voltage_su");
                var nativeBox=level.getBlockEntity(pos.west());
                require(nativeBox!=null&&Class.forName("techreborn.blockentity.storage.energy.LowVoltageSUBlockEntity").isInstance(nativeBox),"Actual native Battery Box subclass missing");
                nativeBox.getClass().getMethod("onLoad").invoke(nativeBox);
                nativeBox.getClass().getMethod("setFacing",Direction.class).invoke(nativeBox,Direction.EAST);
                box=EnergyStorage.SIDED.find(level,pos.west(),Direction.EAST);require(box!=null,"Actual power source output API missing");
                var boxInput=EnergyStorage.SIDED.find(level,pos.west(),Direction.NORTH);require(boxInput!=null,"Actual native power source input missing");seedEnergy(boxInput,1000);
                place(pos.east(),"nerolandcore:fluid_tank");
                waterTank=FluidStorage.SIDED.find(level,pos.east(),Direction.WEST);require(waterTank!=null,"Actual Core fluid tank lacks native Fabric fluid binding");
                try(var t=Transaction.openOuter()){require(waterTank.insert(FluidVariant.of(Fluids.WATER),FluidConstants.BUCKET,t)==FluidConstants.BUCKET,"Actual native Core tank water admission failed");t.commit();}
                place(pos.north(),"nerolandcore:gas_tank");
                gasTank=GasLookup.INSTANCE.find(level,pos.north(),Direction.SOUTH);require(gasTank!=null,"Actual Core gas tank lacks native Nero gas binding");
                require(gasTank.getAmount()==0,"Actual gas tank was not initially empty");
                report.addProperty("native_water_provider_class",waterTank.getClass().getName());report.addProperty("native_gas_provider_class",gasTank.getClass().getName());
                phase(1);
            }
            case 1->{
                long gas=loader.process().oxygen()+gasTank.getAmount();
                require(gas%4==0,"Produced native gas is not whole source batches");
                long batches=gas/4;
                require(box.getAmount()+loader.process().energy()+30*batches==1000,"Real power source/machine/work energy does not conserve");
                require(waterAmount()+loader.process().water()+100*81*batches==2*FluidConstants.BUCKET,"Real Core tank/bucket water quantities do not conserve");
                if(gas<80||loader.process().oxygen()!=0)return;
                require(gasTank.getGas()==GasResource.OXYGEN&&gasTank.getAmount()==80,"Produced gas is not the actual native tank oxygen identity");
                report.addProperty("actual_produced_oxygen_mb",gasTank.getAmount());
                int ratio=NerospaceConfig.scale(2,NerospaceConfig.fuelCostMultiplier());
                int baseline=report.get("empty_tank_negative_oxygen").getAsInt();
                long gasBefore=gasTank.getAmount();nativeHazard(reserveSeed);
                int expected=Math.min(Math.min(90,OxygenManager.maxOxygenFor(player)-baseline),(int)(gasBefore/ratio));
                require(expected>0,"Produced native tank gas cannot buy positive reserve under current configured cost");
                require(Services.PLATFORM.getOxygen(player)==baseline+expected,"Actual native suited reserve did not receive exactly tank-paid air");
                require(gasTank.getAmount()==gasBefore-(long)expected*ratio,"Native suit refill did not debit the actual tank by its configured mB/air");
                report.addProperty("native_gas_mb_per_air",ratio);report.addProperty("actual_suited_reserve_after_tank_refill",Services.PLATFORM.getOxygen(player));
                passed.add("Actual Battery Box + Core water tank power twenty original batches; 80 native oxygen mB refill genuine Nero suit reserve through unchanged OxygenManager with exact tank debit");
                level.setBlockAndUpdate(pos.west(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.east(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.north(),Blocks.AIR.defaultBlockState());
                reloadProcessing(30,100,2999);phase(2);
            }
            case 2->{
                require(loader.process().energy()==30&&loader.process().water()==8100&&loader.process().oxygen()==2999,"Backpressured loader consumed input or overflowed output");
                if(elapsed()<12)return;
                var before=loader.process().state();
                var water=FluidStorage.SIDED.find(level,pos,Direction.UP);var power=EnergyStorage.SIDED.find(level,pos,Direction.UP);
                require(water!=null&&power!=null,"Native own loader input APIs missing");
                try(var t=Transaction.openOuter()){require(water.insert(FluidVariant.of(Fluids.LAVA),8100,t)==0,"Loader accepts wrong native fluid");water.insert(FluidVariant.of(Fluids.WATER),8100,t);power.insert(30,t);}
                require(loader.process().state().equals(before),"Real input API abort did not restore full processing state");
                var gas=GasLookup.INSTANCE.find(level,pos,null);require(gas!=null&&gas.fill(GasResource.OXYGEN,10,false)==0,"Output-only native gas API accepts external refill");
                try(var t=Transaction.openOuter()){require(gas.drain(10,false)==10,"Native own oxygen drain failed");}
                require(loader.process().state().equals(before),"Native gas drain within aborted Fabric parent lost oxygen");
                place(pos.north(),"nerolandcore:gas_tank");
                var nativeTarget=GasLookup.INSTANCE.find(level,pos.north(),Direction.SOUTH);
                require(nativeTarget!=null&&nativeTarget.getAmount()==0,"Actual gas target for native transaction boundary is unavailable");
                boolean refused=false;
                try(var parent=Transaction.openOuter()){
                    try{loader.oxygenOutput().pushTo(nativeTarget,20);}catch(IllegalStateException expected){refused=true;}
                }
                require(refused&&nativeTarget.getAmount()==0&&loader.process().state().equals(before),"Native nontransactional target deposit joined a Fabric parent or mutated either buffer");
                level.setBlockAndUpdate(pos.north(),Blocks.AIR.defaultBlockState());
                reloadProcessing(0,0,0);loader.clearContent();
                var charged=new net.minecraft.world.SimpleContainer(new ItemStack(item("techreborn:red_cell_battery")));
                var energy=ContainerItemContext.ofSingleSlot(ContainerStorage.of(charged,null).getSlot(0)).find(EnergyStorage.ITEM);require(energy!=null,"Actual battery target unsupported");seedEnergy(energy,2000);
                ItemStack held=charged.removeItemNoUpdate(0);loader.insertHeld(held);require(held.isEmpty(),"Actual charged native battery input failed");
                batteryInitial=battery().getAmount();require(batteryInitial==2000,"Actual battery test seed not retained");
                passed.add("Proper-Fabric backpressure, wrong-fluid rejection, input rollback and native gas parent-transaction rollback");
                phase(3);
            }
            case 3->{
                require(loader.process().energy()+battery().getAmount()==batteryInitial,"Actual battery discharge fails conservation");
                if(elapsed()<5)return;
                require(loader.process().energy()>0&&battery().getAmount()<batteryInitial,"Real battery never discharged into source power slot");
                batteryBefore=battery().getAmount();long buffer=loader.process().energy();
                reloadProcessing(buffer,0,0);require(battery().getAmount()==batteryBefore,"Native inventory persistence lost actual battery energy component");
                level.setBlockAndUpdate(pos.above(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.DOWN));
                ((HopperBlockEntity)level.getBlockEntity(pos.above())).setItem(0,new ItemStack(Items.WATER_BUCKET));
                level.setBlockAndUpdate(pos.below(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.DOWN));
                passed.add("Actual TechReborn battery powers loader through native API and retains energy through native inventory save-load");phase(4);
            }
            case 4->{
                if(elapsed()<30)return;
                var above=(HopperBlockEntity)level.getBlockEntity(pos.above());var below=(HopperBlockEntity)level.getBlockEntity(pos.below());
                require(above.isEmpty()&&loader.getItem(1).isEmpty()&&loader.getItem(0).is(item("techreborn:red_cell_battery")),"Actual hopper input failed or accessed protected battery");
                int buckets=0;for(int slot=0;slot<below.getContainerSize();slot++)if(below.getItem(slot).is(Items.BUCKET))buckets+=below.getItem(slot).getCount();
                require(buckets==1,"Native lower hopper did not extract exactly the water-container remainder");
                above.clearContent();below.clearContent();level.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());
                retainedBattery=loader.removeItemNoUpdate(0);loader.clearContent();loader.setItem(2,new ItemStack(Items.BUCKET,5));
                for(int slot=0;slot<player.getInventory().getContainerSize();slot++)player.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
                int bucketLimit=new ItemStack(Items.BUCKET).getMaxStackSize();require(bucketLimit>=5,"Native bucket stack limit cannot support the required partial retrieval case");
                player.getInventory().setItem(0,new ItemStack(Items.BUCKET,bucketLimit-1));player.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.STONE,64));
                loader.retrieve(player);require(player.getInventory().getItem(0).getCount()==bucketLimit&&drops(Items.BUCKET)==4&&loader.isEmpty(),"Native nearly-full retrieval lost or duplicated remainder");
                loader.retrieve(player);require(drops(Items.BUCKET)==4,"Repeated empty retrieval duplicated drops");clearDrops();
                player.getInventory().clearContent();player.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
                loader.setItem(0,retainedBattery);loader.setItem(1,new ItemStack(Items.WATER_BUCKET));loader.setItem(2,new ItemStack(Items.BUCKET,2));
                long batteryAtBreak=battery().getAmount();
                require(level.destroyBlock(pos,true),"Actual native loader removal failed");
                require(drops(AstraOxygen.Content.ITEM)==1&&drops(item("techreborn:red_cell_battery"))==1&&drops(Items.WATER_BUCKET)==1&&drops(Items.BUCKET)==2,"Actual loader removal drops mismatch");
                var dropped=drops().stream().filter(e->e.getItem().is(item("techreborn:red_cell_battery"))).findFirst().orElseThrow();
                var api=ContainerItemContext.withConstant(dropped.getItem()).find(EnergyStorage.ITEM);
                require(api!=null&&api.getAmount()==batteryAtBreak,"Native dropped battery lost stored energy");
                level.removeBlock(pos,false);require(drops(AstraOxygen.Content.ITEM)==1&&drops(Items.BUCKET)==2,"Native repeated removal duplicated items");
                passed.add("Actual native hoppers, nearly-full retrieval and one-time removal drops preserve containers and genuine charged battery");
                finish(null);
            }
            default->throw new AssertionError("Unknown oxygen fixture phase");
        }
    }
    private static Throwable retain(Throwable first,Throwable next){
        if(first==null)return next;first.addSuppressed(next);return first;
    }
    private void finish(Throwable failure){
        if(done)return;done=true;Throwable error=failure;
        if(player!=null)try{
            player.getInventory().clearContent();
            for(EquipmentSlot slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.OFFHAND))player.setItemSlot(slot,ItemStack.EMPTY);
            player.discard();
        }catch(Throwable cleanup){error=retain(error,cleanup);}
        if(worldOwned){
            boolean restored=true;
            for(int i=owned.size()-1;i>=0;i--)try{
                var entity=level.getBlockEntity(owned.get(i));if(entity instanceof net.minecraft.world.Container c)c.clearContent();
                level.setBlockAndUpdate(owned.get(i),original.get(i));
            }catch(Throwable cleanup){restored=false;error=retain(error,cleanup);}
            try{clearDrops();report.addProperty("owned_item_entities_remaining",drops().size());}catch(Throwable cleanup){error=retain(error,cleanup);}
            try{if(forced)level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,false);report.addProperty("owned_forced_ticket_removed",forced);}catch(Throwable cleanup){error=retain(error,cleanup);}
            report.addProperty("owned_blocks_restored",restored);
        }
        report.addProperty("status",error==null?"pass":"fail");report.addProperty("finished_utc",Instant.now().toString());
        if(error!=null){report.addProperty("error",error.toString());error.printStackTrace();}
        if(receiptOwned)write();System.out.println("VALEN_ASTRA_OXYGEN_FIXTURE "+report);
    }
}
