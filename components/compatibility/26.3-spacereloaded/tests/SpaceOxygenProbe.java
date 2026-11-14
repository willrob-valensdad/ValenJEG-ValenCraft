package valen.spacereloadedprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.alex_melan.spacereloaded.registry.ModItems;
import org.alex_melan.spacereloaded.sealing.*;
import org.alex_melan.spacereloaded.lifesupport.LifeSupportState;
import org.alex_melan.spacereloaded.core.lifesupport.CabinAtmosphere;
import za.co.neroland.nerospace.platform.Services;
import za.co.neroland.nerospace.world.OxygenManager;

/** Disposable actual equipment, finite supplies, pressure and optional-delegation contracts. */
public final class SpaceOxygenProbe implements ModInitializer {
    private int ticks, serial;
    private boolean done;
    private final BlockPos room = new BlockPos(240,224,144);
    private final List<Map<String,Object>> checks = new ArrayList<>();
    public void onInitialize() {
        if (!Boolean.getBoolean("valen.space.oxygen")) throw new IllegalStateException("Disposable flag required");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (done) return;
            try {
                ticks++;
                if (ticks == 40) prepareRoom(server);
                if (ticks < 120 || server.overworld().getGameTime()%10 != 0) return;
                run(server);
                finish(server,null);
            } catch(Throwable error) { error.printStackTrace(); finish(server,error.toString()); }
        });
    }
    private void check(String name, boolean pass, Object detail) {
        checks.add(Map.of("name",name,"pass",pass,"detail",detail));
        if (!pass) throw new AssertionError(name+" "+detail);
    }
    private void player(MinecraftServer server, ServerLevel level, Consumer<ServerPlayer> action) {
        var p = SpaceGravityProbe.player(server,level,"Oxygen"+serial++,true);
        try {
            p.setGameMode(GameType.SURVIVAL); SpaceGravityProbe.acknowledgeLoaded(p); p.setHealth(20); p.tickCount=40;
            p.setPos(256.5,226,144.5);
            action.accept(p);
        } finally {
            if (p.getVehicle()!=null) p.stopRiding();
            level.removePlayerImmediately(p,Entity.RemovalReason.DISCARDED);
        }
    }
    private static void neroSuit(ServerPlayer p) {
        String[] names={"helmet","chestplate","leggings","boots"};
        EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        for(int i=0;i<slots.length;i++) p.setItemSlot(slots[i],new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("nerospace:oxygen_suit_"+names[i]))));
    }
    private static ItemStack spaceSuit(ServerPlayer p) {
        p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.OXYGEN_MASK));
        p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.SPACE_SUIT_CHESTPLATE));
        p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.SPACE_SUIT_LEGGINGS));
        p.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.SPACE_SUIT_BOOTS));
        var canister=new ItemStack(ModItems.OXYGEN_CANISTER);p.getInventory().setItem(0,canister);return canister;
    }
    private void prepareRoom(MinecraftServer server) {
        var moon=SpaceGravityProbe.level(server,"spacereloaded:moon");
        moon.setChunkForced(room.getX()>>4,room.getZ()>>4,true);moon.getChunkAt(room);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<=4;y++)
            moon.setBlockAndUpdate(room.offset(x,y,z),(Math.abs(x)==2||Math.abs(z)==2||y==0||y==4?Blocks.STONE:Blocks.AIR).defaultBlockState());
        moon.setBlockAndUpdate(room,org.alex_melan.spacereloaded.registry.ModBlocks.ATMOSPHERE_CONTROLLER.defaultBlockState());
    }
    private void run(MinecraftServer server) {
        var moon=SpaceGravityProbe.level(server,"spacereloaded:moon");
        var green=SpaceGravityProbe.level(server,"nerospace:greenxertz");
        for(boolean reverse:List.of(false,true)) player(server,moon,p->{
            neroSuit(p);Services.PLATFORM.setOxygen(p,900);
            var spare=new ItemStack(ModItems.OXYGEN_CANISTER);p.getInventory().setItem(0,spare);
            check("Nero full set recognized",OxygenManager.suitTier(p)!=OxygenManager.SuitTier.NONE,OxygenManager.suitTier(p).toString());
            if(reverse){VacuumHazard.tick(moon);OxygenManager.tick(p);}else{OxygenManager.tick(p);VacuumHazard.tick(moon);}
            check("Nero supply drains once in Space, order="+reverse,Services.PLATFORM.getOxygen(p)==897&&spare.getDamageValue()==0&&p.getHealth()==20,
                    Map.of("reserve",Services.PLATFORM.getOxygen(p),"canister",spare.getDamageValue(),"health",p.getHealth()));
        });
        player(server,moon,p->{
            neroSuit(p);Services.PLATFORM.setOxygen(p,0);VacuumHazard.tick(moon);
            check("Space does not duplicate Nero exhaustion damage",p.getHealth()==20,p.getHealth());
            OxygenManager.tick(p);check("Empty Nero reserve still suffocates",p.getHealth()<20,p.getHealth());
        });
        player(server,moon,p->{
            neroSuit(p);p.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);VacuumHazard.tick(moon);
            check("Partial Nero suit is not free Space protection",p.getHealth()<20,p.getHealth());
        });
        player(server,green,p->{
            var air=spaceSuit(p);Services.PLATFORM.setOxygen(p,123);OxygenManager.tick(p);VacuumHazard.tick(green);
            check("Space supply drains once in Nero",air.getDamageValue()==1&&Services.PLATFORM.getOxygen(p)==123&&p.getHealth()==20,
                    Map.of("canister",air.getDamageValue(),"nero_reserve",Services.PLATFORM.getOxygen(p),"health",p.getHealth()));
        });
        boolean originalVacuum=ZoneManager.isVacuumWorld(green);
        try {
            ZoneManager.setVacuumWorld(green,true);
            for(boolean reverse:List.of(false,true)) player(server,green,p->{
                var air=spaceSuit(p);Services.PLATFORM.setOxygen(p,123);
                if(reverse){VacuumHazard.tick(green);OxygenManager.tick(p);}else{OxygenManager.tick(p);VacuumHazard.tick(green);}
                check("Overlapping vacuum ownership debits once, order="+reverse,
                        air.getDamageValue()==1&&Services.PLATFORM.getOxygen(p)==123&&p.getHealth()==20,
                        Map.of("canister",air.getDamageValue(),"nero_reserve",Services.PLATFORM.getOxygen(p),"health",p.getHealth()));
            });
            player(server,green,p->{
                Services.PLATFORM.setOxygen(p,300);VacuumHazard.tick(green);OxygenManager.tick(p);
                check("Overlap preserves bare native breath reserve without duplicate damage",Services.PLATFORM.getOxygen(p)==270&&p.getHealth()==20,
                        Map.of("reserve",Services.PLATFORM.getOxygen(p),"health",p.getHealth()));
            });
            player(server,green,p->{
                Services.PLATFORM.setOxygen(p,0);VacuumHazard.tick(green);
                check("Overlap defers bare exhaustion to one owner",p.getHealth()==20,p.getHealth());
                OxygenManager.tick(p);check("Overlap retains bare exhaustion damage",p.getHealth()<20,p.getHealth());
            });
        } finally {ZoneManager.setVacuumWorld(green,originalVacuum);}
        player(server,green,p->{
            var air=spaceSuit(p);air.setDamageValue(air.getMaxDamage());OxygenManager.tick(p);
            check("Empty Space canister still suffocates in Nero",p.getHealth()<20,p.getHealth());
        });
        player(server,green,p->{
            var air=spaceSuit(p);p.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);OxygenManager.tick(p);
            check("Partial Space suit retains exposure in Nero",air.getDamageValue()==1&&p.getHealth()<20,Map.of("canister",air.getDamageValue(),"health",p.getHealth()));
        });
        for(String id:List.of("nerospace:cindara","nerospace:glacira")) player(server,SpaceGravityProbe.level(server,id),p->{
            var air=spaceSuit(p);OxygenManager.tick(p);
            check(id+" generic suit retains temperature cost",air.getDamageValue()==4&&p.getHealth()==20,Map.of("canister",air.getDamageValue(),"health",p.getHealth(),"frozen_ticks",p.getTicksFrozen()));
            if(id.endsWith("glacira"))check("Native cold feedback retained",p.getTicksFrozen()>0,p.getTicksFrozen());
        });
        player(server,SpaceGravityProbe.level(server,"nerospace:cindara"),p->{
            var air=spaceSuit(p);air.setDamageValue(air.getMaxDamage()-2);OxygenManager.tick(p);
            check("Partial temperature charge cannot buy free protection",air.getDamageValue()==air.getMaxDamage()&&p.getHealth()<20,
                    Map.of("used",air.getDamageValue(),"health",p.getHealth()));
        });
        player(server,green,p->{
            neroSuit(p);Services.PLATFORM.setOxygen(p,900);OxygenManager.tick(p);
            check("Native Nero rate unchanged",Services.PLATFORM.getOxygen(p)==897,Services.PLATFORM.getOxygen(p));
        });
        player(server,moon,p->{
            var air=spaceSuit(p);VacuumHazard.tick(moon);
            check("Native Space rate unchanged",air.getDamageValue()==1&&p.getHealth()==20,air.getDamageValue());
        });
        var zone=ZoneManager.zoneAt(moon,room);
        check("Real controller computes sealed room",zone!=null&&zone.isSealed(),zone==null?"null":zone.status().toString());
        var masses=CabinAtmosphere.fill(101.3,CabinAtmosphere.AIR_O2_FRACTION,zone.volume().size());
        LifeSupportState.add(moon,zone,masses[0],masses[1],0);
        check("Actual gas pressure qualifies",VacuumHazard.isInsidePressurizedZone(moon,room.above()),LifeSupportState.now(moon,zone).pressure());
        player(server,moon,p->{
            neroSuit(p);p.setPos(room.getX()+.5,room.getY()+1,room.getZ()+.5);Services.PLATFORM.setOxygen(p,40);
            OxygenManager.tick(p);VacuumHazard.tick(moon);
            check("Nero recognizes real pressurized Space room",Services.PLATFORM.getOxygen(p)==900&&p.getHealth()==20,Services.PLATFORM.getOxygen(p));
        });
        player(server,green,p->{
            var air=spaceSuit(p);var pos=p.blockPosition().offset(2,0,0);
            var old=green.getBlockState(pos);
            green.setBlockAndUpdate(pos,BuiltInRegistries.BLOCK.getValue(Identifier.parse("nerospace:landing_pod")).defaultBlockState());
            try { OxygenManager.tick(p);check("Space mask honors native Nero breathable pad",air.getDamageValue()==0&&p.getHealth()==20,air.getDamageValue()); }
            finally {green.setBlockAndUpdate(pos,old);}
        });
        player(server,moon,p->{
            BlockPos pad=room.offset(8,0,0);
            moon.setBlockAndUpdate(pad.below(),Blocks.STONE.defaultBlockState());
            String[] parts={"rocket_engine","fuel_tank","command_module"};
            for(int i=0;i<parts.length;i++)moon.setBlockAndUpdate(pad.above(i),BuiltInRegistries.BLOCK.getValue(Identifier.parse("spacereloaded:"+parts[i])).defaultBlockState());
            ((org.alex_melan.spacereloaded.rocket.FuelTankBlockEntity)moon.getBlockEntity(pad.above())).fill(2000,"spacereloaded:kerolox");
            p.setPos(pad.getX()+.5,pad.getY()+3,pad.getZ()+.5);
            org.alex_melan.spacereloaded.rocket.RocketInteractions.assemble(moon,pad.above(2),p);
            var rockets=moon.getEntitiesOfClass(org.alex_melan.spacereloaded.rocket.RocketEntity.class,new net.minecraft.world.phys.AABB(pad).inflate(5));
            check("Actual Space cabin assembles",rockets.size()==1,rockets.size());
            var rocket=rockets.get(0);
            try {
                neroSuit(p);Services.PLATFORM.setOxygen(p,0);
                check("Nero crew boards native Space cabin",p.startRiding(rocket,true,true),rocket.hasPressurizedCabin());
                OxygenManager.tick(p);VacuumHazard.tick(moon);
                check("Space cabin protects and refills Nero reserve",Services.PLATFORM.getOxygen(p)==900&&p.getHealth()==20,Services.PLATFORM.getOxygen(p));
                var air=spaceSuit(p);air.setDamageValue(air.getMaxDamage());OxygenManager.tick(p);VacuumHazard.tick(moon);
                check("Space cabin retains native empty-canister protection",p.getHealth()==20&&air.getDamageValue()==air.getMaxDamage(),p.getHealth());
                p.stopRiding();OxygenManager.tick(p);VacuumHazard.tick(moon);
                check("Leaving cabin restores real exhaustion hazard",p.getHealth()<20,p.getHealth());
            } finally {
                p.stopRiding();p.setShiftKeyDown(true);
                rocket.interact(p,net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
                p.setShiftKeyDown(false);
            }
        });
        for(var level:List.of(green,moon)) player(server,level,p->{
            var air=spaceSuit(p);
            var rocket=new za.co.neroland.nerospace.rocket.RocketEntity(level,p.getX(),p.getY(),p.getZ(),za.co.neroland.nerospace.rocket.RocketTier.TIER_1);
            level.addFreshEntity(rocket);rocket.addOxygen(10);
            try {
                check("Native Nero cabin boarding",p.startRiding(rocket,true,true),level.dimension().identifier().toString());
                OxygenManager.tick(p);VacuumHazard.tick(level);
                check("Finite Nero cabin supply used once",rocket.getOxygen()==8&&air.getDamageValue()==0&&p.getHealth()==20,
                        Map.of("dimension",level.dimension().identifier().toString(),"cabin",rocket.getOxygen(),"canister",air.getDamageValue()));
                rocket.drainOxygen(rocket.getOxygen());OxygenManager.tick(p);VacuumHazard.tick(level);
                check("Empty Nero cabin falls back to actual Space canister",air.getDamageValue()==1&&p.getHealth()==20,air.getDamageValue());
            } finally {p.stopRiding();rocket.discard();}
        });
    }
    private void finish(MinecraftServer server,String error) {
        done=true;
        try {
            SpaceGravityProbe.level(server,"spacereloaded:moon").setChunkForced(room.getX()>>4,room.getZ()>>4,false);
            var result=new LinkedHashMap<String,Object>();result.put("pass",error==null);result.put("error",error);result.put("checks",checks);
            result.put("scope","Actual transformed native hazard methods, gear, finite supplies, real sealed gas room and cabins; no graphical HUD acceptance");
            Files.writeString(Path.of("space-oxygen-contracts.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
            System.out.println("SPACE_OXYGEN_CONTRACTS "+result);
        }catch(Exception failure){throw new RuntimeException(failure);}
    }
}
