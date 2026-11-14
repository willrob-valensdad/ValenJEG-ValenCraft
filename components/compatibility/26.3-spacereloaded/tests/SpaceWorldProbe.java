package valen.spacereloadedprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.alex_melan.spacereloaded.registry.ModItems;
import org.alex_melan.spacereloaded.rocket.*;
import org.alex_melan.spacereloaded.sealing.VacuumHazard;

/** Disposable actual terrain, survival hazard, assembled-rocket flight proof. */
public final class SpaceWorldProbe implements ModInitializer {
    private int ticks,started,phase,orbitTick;
    private boolean done;
    private ServerPlayer pilot;
    private double initialFuel,orbitFuel,maxHeight;
    private final List<Map<String,Object>> checks=new ArrayList<>();
    private final Map<String,Object> terrain=new LinkedHashMap<>();
    private final BlockPos pad=new BlockPos(208,240,144);
    public void onInitialize(){
        if(!Boolean.getBoolean("valen.space.world"))throw new IllegalStateException("Disposable world flag required");
        ServerTickEvents.END_SERVER_TICK.register(server->{
            if(done)return;ticks++;
            if(ticks==40){server.overworld().setChunkForced(pad.getX()>>4,pad.getZ()>>4,true);server.overworld().getChunkAt(pad);}
            try {
                if(started==0){
                    if(ticks<120||server.overworld().getGameTime()%10!=0)return;
                    generatedTerrain(server);oxygen(server);launch(server);started=ticks;
                }else observe(server);
            }catch(Throwable error){error.printStackTrace();finish(server,error.toString());}
        });
    }
    private void check(String name,boolean pass,Object detail){
        checks.add(Map.of("name",name,"pass",pass,"detail",detail));if(!pass)throw new AssertionError(name+" "+detail);
    }
    private void generatedTerrain(MinecraftServer server){
        for(String id:List.of("spacereloaded:moon","spacereloaded:mars","spacereloaded:asteroid_belt")){
            var level=SpaceGravityProbe.level(server,id);Map<String,Integer> counts=new TreeMap<>();int sampled=0;
            for(int i=0;i<(id.endsWith("asteroid_belt")?16:1);i++){
                var chunk=level.getChunk(i%4,i/4);sampled++;
                for(var section:chunk.getSections())section.getStates().count((state,count)->{
                    if(!state.isAir())counts.merge(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),count,Integer::sum);
                });
                if(counts.keySet().stream().anyMatch(k->k.startsWith("spacereloaded:")))break;
            }
            terrain.put(id,Map.of("generated_full_chunks",sampled,"non_air_blocks",counts));
            check(id+" native generator produces author terrain",counts.keySet().stream().anyMatch(k->k.startsWith("spacereloaded:")),counts);
        }
    }
    private void oxygen(MinecraftServer server){
        var moon=SpaceGravityProbe.level(server,"spacereloaded:moon");
        var player=SpaceGravityProbe.player(server,moon,"SpaceOxygen",true);
        try{
            player.setGameMode(GameType.SURVIVAL);SpaceGravityProbe.acknowledgeLoaded(player);player.setHealth(20);
            player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.OXYGEN_MASK));
            player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.SPACE_SUIT_CHESTPLATE));
            player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.SPACE_SUIT_LEGGINGS));
            player.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.SPACE_SUIT_BOOTS));
            var canister=new ItemStack(ModItems.OXYGEN_CANISTER);player.getInventory().setItem(0,canister);
            check("native hazard gate reached",moon.getGameTime()%10==0,moon.getGameTime());
            VacuumHazard.tick(moon);
            check("full suit and oxygen prevent vacuum damage",player.getHealth()==20&&canister.getDamageValue()==1,Map.of("health",player.getHealth(),"canister_used",canister.getDamageValue()));
            canister.setDamageValue(canister.getMaxDamage());VacuumHazard.tick(moon);
            check("empty oxygen canister causes native vacuum damage",player.getHealth()<20,player.getHealth());
            var exposed=SpaceGravityProbe.player(server,moon,"SpaceExposed",true);
            try {
                exposed.setGameMode(GameType.SURVIVAL);SpaceGravityProbe.acknowledgeLoaded(exposed);exposed.setHealth(20);
                exposed.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.OXYGEN_MASK));
                var air=new ItemStack(ModItems.OXYGEN_CANISTER);exposed.getInventory().setItem(0,air);VacuumHazard.tick(moon);
                check("oxygen without full suit retains exposure hazard",exposed.getHealth()<20&&air.getDamageValue()==1,exposed.getHealth());
            } finally {moon.removePlayerImmediately(exposed,Entity.RemovalReason.DISCARDED);}
        }finally{moon.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);}
    }
    private void launch(MinecraftServer server){
        var level=server.overworld();level.setChunkForced(pad.getX()>>4,pad.getZ()>>4,true);level.getChunkAt(pad);
        for(var old:level.getEntitiesOfClass(RocketEntity.class,new AABB(pad).inflate(5)))old.discard();
        for(int y=pad.getY();y<level.getMaxY();y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlock(pad.offset(x,y-pad.getY(),z),Blocks.AIR.defaultBlockState(),2);
        level.setBlockAndUpdate(pad.below(),Blocks.STONE.defaultBlockState());
        String[] parts={"spacereloaded:rocket_engine","spacereloaded:fuel_tank","spacereloaded:command_module"};
        for(int i=0;i<parts.length;i++)level.setBlockAndUpdate(pad.above(i),BuiltInRegistries.BLOCK.getValue(Identifier.parse(parts[i])).defaultBlockState());
        var tank=(FuelTankBlockEntity)level.getBlockEntity(pad.above());check("actual rocket tank accepts2000kg",tank.fill(2000,"spacereloaded:kerolox")==2000,tank.propellantKg());
        pilot=SpaceGravityProbe.player(server,level,"SpaceRocket",true);pilot.setGameMode(GameType.SURVIVAL);SpaceGravityProbe.acknowledgeLoaded(pilot);pilot.setPos(pad.getX()+0.5,pad.getY()+3,pad.getZ()+0.5);
        var scanned=RocketAssembler.scan(level,pad.above(2),new PartPropertiesResolver(level),org.alex_melan.spacereloaded.SpaceReloaded.config().rocketMaxBlocks);
        check("native assembly scan accepts actual placed parts",scanned instanceof RocketAssembler.Result.Ok,scanned.toString());
        RocketInteractions.assemble(level,pad.above(2),pilot);
        var rockets=level.getEntitiesOfClass(RocketEntity.class,new AABB(pad).inflate(5));check("native assembler creates one real rocket",rockets.size()==1,rockets.size());
        var rocket=rockets.get(0);initialFuel=rocket.propellantKg();maxHeight=rocket.getY();
        check("pilot mounts assembled rocket",pilot.startRiding(rocket,true,true),rocket.getUUID().toString());
        check("author orbit destination accepted",rocket.setDestination(level,Identifier.parse("spacereloaded:earth_orbit")),"earth_orbit");
        check("native ignition admits real thrust/fuel",rocket.ignite(level),initialFuel);
        pilot.setLastClientInput(new Input(false,false,false,false,true,false,false));phase=1;
    }
    private void observe(MinecraftServer server){
        checkDeadline();
        SpaceGravityProbe.acknowledgeLoaded(pilot);
        if(!(pilot.getVehicle() instanceof RocketEntity rocket))throw new AssertionError("Pilot lost actual rocket; dimension="+pilot.level().dimension().identifier());
        maxHeight=Math.max(maxHeight,rocket.getY());String dimension=pilot.level().dimension().identifier().toString();
        if(phase==1&&dimension.equals("spacereloaded:earth_orbit")){
            orbitFuel=rocket.propellantKg();orbitTick=ticks;
            check("real powered ascent reaches author orbit",maxHeight>pad.getY()+100&&orbitFuel>0&&orbitFuel<initialFuel,Map.of("ticks",ticks-started,"peak_y_observed_at_tick_end",maxHeight,"fuel_kg",orbitFuel));
            check("pressurized cabin protects survival pilot in vacuum",rocket.hasPressurizedCabin()&&pilot.getHealth()>0,Map.of("health",pilot.getHealth(),"canisters",pilot.getInventory().countItem(ModItems.OXYGEN_CANISTER)));
            pilot.setLastClientInput(Input.EMPTY);phase=2;
        }else if(phase==2&&ticks>=orbitTick+20){
            check("orbit cabin consumes no oxygen or health",pilot.getHealth()==20,pilot.getHealth());
            check("author Earth return selected",rocket.setDestination(pilot.level(),Identifier.parse("spacereloaded:earth")),"earth");
            check("native return ignition",rocket.ignite(pilot.level()),rocket.propellantKg());
            pilot.setLastClientInput(new Input(false,false,false,false,true,false,false));phase=3;
        }else if(phase==3&&dimension.equals("minecraft:overworld")){
            pilot.setLastClientInput(Input.EMPTY);
            check("native powered return carries pilot and fuel",rocket.propellantKg()>0&&rocket.propellantKg()<orbitFuel,Map.of("ticks",ticks-started,"fuel_kg",rocket.propellantKg(),"y",rocket.getY()));
            phase=4;
        }else if(phase==4){
            // Ordinary pilot thrust follows the author's own descent-braking threshold.
            pilot.setLastClientInput(new Input(false,false,false,false,rocket.getDeltaMovement().y()*20 < -12.0,false,false));
            if(rocket.isParked()){
                pilot.setLastClientInput(Input.EMPTY);
                double remaining=rocket.propellantKg();
                BlockPos base=new BlockPos((int)Math.round(rocket.getX()-0.5),(int)Math.round(rocket.getY()),(int)Math.round(rocket.getZ()-0.5));
                check("ordinary thrust brakes into native soft landing",remaining>0&&pilot.getHealth()>0,
                        Map.of("ticks",ticks-started,"base",base.toString(),"fuel_kg",remaining,"pilot_health",pilot.getHealth()));
                pilot.setShiftKeyDown(true);
                var result=rocket.interact(pilot,net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
                pilot.setShiftKeyDown(false);
                check("native sneak interaction disassembles parked rocket",rocket.isRemoved(),result.toString());
                String[] parts={"spacereloaded:rocket_engine","spacereloaded:fuel_tank","spacereloaded:command_module"};
                for(int i=0;i<parts.length;i++)check("disassembly restores "+parts[i],
                        BuiltInRegistries.BLOCK.getKey(pilot.level().getBlockState(base.above(i)).getBlock()).toString().equals(parts[i]),base.above(i).toString());
                var tank=(FuelTankBlockEntity)pilot.level().getBlockEntity(base.above());
                check("disassembly conserves remaining propellant",Math.abs(tank.propellantKg()-remaining)<1e-6,
                        Map.of("rocket_kg",remaining,"tank_kg",tank.propellantKg(),"tank_pos",base.above().toString()));
                finish(server,null);
            }
        }
        if(ticks%100==0)System.out.println("SPACE_FLIGHT_PROGRESS phase="+phase+" dimension="+dimension+" y="+rocket.getY()+" fuel="+rocket.propellantKg());
    }
    private void checkDeadline(){if(ticks-started>1800)throw new AssertionError("Native flight exceeded90seconds of ticks");}
    private void finish(MinecraftServer server,String error){
        if(done)return;done=true;
        try{
            if(pilot!=null){if(pilot.getVehicle() instanceof RocketEntity r){pilot.stopRiding();r.discard();}pilot.level().removePlayerImmediately(pilot,Entity.RemovalReason.DISCARDED);}
            server.overworld().setChunkForced(pad.getX()>>4,pad.getZ()>>4,false);
            var result=new LinkedHashMap<String,Object>();result.put("pass",error==null);result.put("error",error);result.put("checks",checks);result.put("terrain",terrain);result.put("elapsed_ticks",ticks-started);
            result.put("scope","native generated chunks, hazard invocation, actual assembly and ordinary input-driven physics; packet-sink player; native soft landing and public sneak-interaction disassembly; no client visuals");
            Files.writeString(Path.of("space-world-contracts.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");System.out.println("SPACE_WORLD_CONTRACTS "+result);
        }catch(Exception failure){throw new RuntimeException(failure);}
    }
}
