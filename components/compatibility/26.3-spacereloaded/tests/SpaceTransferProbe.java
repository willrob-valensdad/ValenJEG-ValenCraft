package valen.spacereloadedprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.alex_melan.spacereloaded.fluid.ModFluids;
import team.reborn.energy.api.*;

/** Disposable endpoint/transaction proof, never included in production artifacts. */
public final class SpaceTransferProbe implements ModInitializer {
    private int tick;
    private final Map<String,BlockPos> positions = new LinkedHashMap<>();
    private final List<Map<String,Object>> checks = new ArrayList<>();
    private int next;
    private final Map<String,Object> savedExpected = new LinkedHashMap<>();
    public void onInitialize() {
        if (!Boolean.getBoolean("valen.space.transfers")) throw new IllegalStateException("Disposable flag required");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            try {
                if (tick == 40) prepare(server.overworld());
                if (tick != 100) return;
                run(server.overworld());
                captureSaved(server.overworld());
                write(null);
            } catch(Throwable failure) {
                failure.printStackTrace();
                try { write(failure.toString()); } catch(Exception error) { throw new RuntimeException(error); }
            }
        });
    }
    private void check(String name, boolean pass, Object detail) { checks.add(Map.of("name",name,"pass",pass,"detail",detail)); }
    private void prepare(ServerLevel level) {
        for(String id:List.of("spacereloaded:battery","spacereloaded:electric_furnace","nerolandcore:battery","techreborn:low_voltage_su","nerospace:nerosium_grinder")) place(level,id,id);
        for(var fuel:ModFluids.all()) {
            place(level,fuel.fuelId()+"/space","spacereloaded:fuel_tank");
            place(level,fuel.fuelId()+"/core","nerolandcore:fluid_tank");
            place(level,fuel.fuelId()+"/create","create:fluid_tank");
        }
    }
    private void place(ServerLevel level,String key,String id) {
        BlockPos pos=new BlockPos(176+(next%4)*4,224,144+(next/4)*4);next++;
        level.getChunkAt(pos);
        var block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        if(block==null||block==Blocks.AIR)throw new AssertionError("Missing "+id);
        level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,block.defaultBlockState());
        if(level.getBlockEntity(pos) instanceof reborncore.common.blockentity.MachineBaseBlockEntity machine)machine.onLoad();
        positions.put(key,pos);
    }
    private EnergyStorage energy(ServerLevel level,String key,boolean input) {
        for(Direction side:Direction.values()) {
            var store=EnergyStorage.SIDED.find(level,positions.get(key),side);if(store==null)continue;
            try(var tx=Transaction.openOuter()) {if((input?store.insert(1,tx):store.extract(1,tx))>0)return store;}
        }
        throw new AssertionError("Missing usable energy face "+key+" input="+input);
    }
    private void run(ServerLevel level) {
        for(String key:List.of("spacereloaded:battery","spacereloaded:electric_furnace")) {
            var store=energy(level,key,true);var chunk=level.getChunkAt(positions.get(key));long before=store.getAmount();
            try {
                chunk.tryMarkSaved();
                try(var tx=Transaction.openOuter()){store.insert(10,tx);}
                check(key+" abort restores energy and does not dirty clean chunk",store.getAmount()==before&&!chunk.isUnsaved(),store.getAmount());
                try(var tx=Transaction.openOuter()){store.insert(10,tx);tx.commit();}
                check(key+" external committed energy marks chunk dirty",store.getAmount()==before+10&&chunk.isUnsaved(),Map.of("energy",store.getAmount(),"chunk_dirty",chunk.isUnsaved()));
            } finally {chunk.markUnsaved();}
        }
        var space=energy(level,"spacereloaded:battery",true);
        try(var tx=Transaction.openOuter()){long seeded=space.insert(1000,tx);check("Space seed respects actual native limit",seeded>0&&seeded<=1000,seeded);tx.commit();}
        moveEnergy("Space to Core",energy(level,"spacereloaded:battery",false),energy(level,"nerolandcore:battery",true),100);
        moveEnergy("Core to Space",energy(level,"nerolandcore:battery",false),energy(level,"spacereloaded:battery",true),50);
        moveEnergy("Space to TechReborn",energy(level,"spacereloaded:battery",false),energy(level,"techreborn:low_voltage_su",true),32);
        moveEnergy("TechReborn to Space",energy(level,"techreborn:low_voltage_su",false),energy(level,"spacereloaded:battery",true),20);
        moveEnergy("Space to Nero grinder",energy(level,"spacereloaded:battery",false),energy(level,"nerospace:nerosium_grinder",true),100);
        for(var fuel:ModFluids.all()) {
            var variant=FluidVariant.of(fuel.source());
            var core=fluid(level,fuel.fuelId()+"/core"); var tank=fluid(level,fuel.fuelId()+"/space"); var create=fluid(level,fuel.fuelId()+"/create");
            try(var tx=Transaction.openOuter()){long inserted=core.insert(variant,FluidConstants.BUCKET*2,tx);check(fuel.fuelId()+" native Core seed",inserted==FluidConstants.BUCKET*2,inserted);tx.commit();}
            moveFluid(fuel.fuelId()+" Core to Space",core,tank,variant,FluidConstants.BUCKET);
            moveFluid(fuel.fuelId()+" Space to Create",tank,create,variant,FluidConstants.BUCKET/2);
            moveFluid(fuel.fuelId()+" Create back to Space",create,tank,variant,FluidConstants.BUCKET/4);
            check(fuel.fuelId()+" Space exports exact plain propellant identity",tank.iterator().next().getResource().equals(variant),tank.iterator().next().getResource().toString());
        }
    }
    private void captureSaved(ServerLevel level) {
        for(String key:List.of("spacereloaded:battery","spacereloaded:electric_furnace")) {
            var pos=positions.get(key);var tag=level.getBlockEntity(pos).saveWithoutMetadata(level.registryAccess());
            long value=tag.getLongOr("energy",-1);var store=EnergyStorage.SIDED.find(level,pos,Direction.UP);
            check(key+" serialized energy matches actual buffer before save",store!=null&&value==store.getAmount(),value);
            savedExpected.put(key,Map.of("position",List.of(pos.getX(),pos.getY(),pos.getZ()),"predicate","{energy:"+value+"L}"));
        }
        for(var fuel:ModFluids.all()) {
            String key=fuel.fuelId()+"/space";var pos=positions.get(key);var tag=level.getBlockEntity(pos).saveWithoutMetadata(level.registryAccess());
            double kg=tag.getDoubleOr("propellant",-1);String type=tag.getStringOr("fuel_type","");
            check(key+" serialized fuel matches exported amount",type.equals(fuel.fuelId())&&ModFluids.kgToDroplets(kg,fuel.kgPerBucket())==amount(fluid(level,key),FluidVariant.of(fuel.source())),kg);
            savedExpected.put(key,Map.of("position",List.of(pos.getX(),pos.getY(),pos.getZ()),"predicate","{propellant:"+kg+"d,fuel_type:\""+type+"\"}"));
        }
    }
    private Storage<FluidVariant> fluid(ServerLevel level,String key) {
        var storage=FluidStorage.SIDED.find(level,positions.get(key),Direction.UP);
        if(storage==null)throw new AssertionError("Missing fluid endpoint "+key);return storage;
    }
    private static long amount(Storage<FluidVariant> storage,FluidVariant fluid){long n=0;for(var v:storage)if(v.getResource().equals(fluid))n+=v.getAmount();return n;}
    private void moveEnergy(String name,EnergyStorage a,EnergyStorage b,long requested){
        long beforeA=a.getAmount(),beforeB=b.getAmount(),preview,moved;
        try(var tx=Transaction.openOuter()){preview=EnergyStorageUtil.move(a,b,requested,tx);}
        check(name+" abort",preview==requested&&a.getAmount()==beforeA&&b.getAmount()==beforeB,preview);
        try(var tx=Transaction.openOuter()){moved=EnergyStorageUtil.move(a,b,requested,tx);tx.commit();}
        check(name+" commit conservation",moved==requested&&a.getAmount()==beforeA-moved&&b.getAmount()==beforeB+moved,moved);
    }
    private void moveFluid(String name,Storage<FluidVariant> a,Storage<FluidVariant> b,FluidVariant fluid,long requested){
        long beforeA=amount(a,fluid),beforeB=amount(b,fluid),preview,moved;
        try(var tx=Transaction.openOuter()){preview=StorageUtil.move(a,b,fluid::equals,requested,tx);}
        check(name+" abort",preview==requested&&amount(a,fluid)==beforeA&&amount(b,fluid)==beforeB,preview);
        try(var tx=Transaction.openOuter()){moved=StorageUtil.move(a,b,fluid::equals,requested,tx);tx.commit();}
        check(name+" commit conservation",moved==requested&&amount(a,fluid)==beforeA-moved&&amount(b,fluid)==beforeB+moved,Map.of("moved",moved,"source",amount(a,fluid),"target",amount(b,fluid)));
    }
    private void write(String error)throws Exception {
        var data=new LinkedHashMap<String,Object>();data.put("pass",error==null&&!checks.isEmpty()&&checks.stream().allMatch(c->Boolean.TRUE.equals(c.get("pass"))));data.put("error",error);data.put("checks",checks);
        data.put("saved_expected",savedExpected);
        data.put("positions",positions.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,e->List.of(e.getValue().getX(),e.getValue().getY(),e.getValue().getZ()))));
        Files.writeString(Path.of("space-transfer-contracts.json"),new GsonBuilder().setPrettyPrinting().create().toJson(data)+"\n");
        System.out.println("SPACE_TRANSFER_CONTRACTS "+data);
    }
}
