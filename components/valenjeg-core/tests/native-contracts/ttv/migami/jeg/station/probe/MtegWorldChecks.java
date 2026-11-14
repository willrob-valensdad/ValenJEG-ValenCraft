package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.BuiltInRegistries;
import ttv.migami.jeg.mteg.*;
import ttv.migami.jeg.init.ModDataComponents;
/** Proper Fabric-phase actual native lifecycle reload proof; connected input remains separate. */
public final class MtegWorldChecks {
 private static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void run(MinecraftServer server,JsonObject outer)throws Exception{
  if(!Boolean.getBoolean("valen.mteg.probe"))return;
  Path game=Path.of("").toAbsolutePath();if(!game.equals(Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server"))||!Files.exists(game.resolve("../preparation.json")))throw new IllegalStateException("Not copied fixture");
  Path output=game.resolve("valen-mteg-probe.json");if(Files.exists(output))throw new IllegalStateException("MTEG receipt exists; archive/reset first");
  ServerLevel level=server.overworld();ServerPlayer player=new ServerPlayer(server,level,new GameProfile(UUID.fromString("7d0c2231-8e95-4a48-a395-7c711937e762"),"MtegNativeProbe"),net.minecraft.server.level.ClientInformation.createDefault());
  player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false));player.setPos(.5,224,-18.5);player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  net.minecraft.core.BlockPos arena=player.blockPosition();
  boolean tickingBefore=level.isPositionEntityTicking(arena);
  level.getChunk(arena.getX()>>4,arena.getZ()>>4);
  boolean forcedAdded=level.setChunkForced(arena.getX()>>4,arena.getZ()>>4,true);
  JsonObject report=new JsonObject();JsonArray passes=new JsonArray();report.add("passed",passes);report.addProperty("connected_client_input_proof",false);report.addProperty("status","running");
  report.addProperty("arena_chunk_x",arena.getX()>>4);report.addProperty("arena_chunk_z",arena.getZ()>>4);
  report.addProperty("entity_ticking_before_owned_ticket",tickingBefore);report.addProperty("owned_forced_ticket_added",forcedAdded);
  Files.writeString(output,report.toString());
  outer.addProperty("mteg_status","pending separate valen-mteg-probe.json");
  String[] ids={"scar_l","remington_870","flamer","famas","m1_garand"};int[] state={-1,0,0,0};ItemStack[] held={ItemStack.EMPTY};Set<Projectile> owned=Collections.newSetFromMap(new IdentityHashMap<>());
  JsonArray loaded=new JsonArray();report.add("actual_native_entity_loads",loaded);
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity,world)->{
   if(world==level&&entity instanceof MtegProjectile projectile&&projectile.getOwner()==player){
    owned.add(projectile);JsonObject row=new JsonObject();row.addProperty("type",BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).toString());
    row.addProperty("uuid",projectile.getUUID().toString());row.addProperty("gun",projectile.definition().id().toString());row.addProperty("actual_owner",player.getUUID().toString());
    row.addProperty("native_lookup_same_object",level.getEntity(projectile.getId())==projectile);loaded.add(row);
   }
  });
  boolean[] cleaned={false};
  Runnable cleanup=()->{
   if(cleaned[0])return;cleaned[0]=true;
   MtegReload.cancel(player);for(var projectile:owned)projectile.discard();owned.clear();
   player.getInventory().clearContent();player.discard();
   if(forcedAdded)level.setChunkForced(arena.getX()>>4,arena.getZ()>>4,false);
   report.addProperty("owned_forced_ticket_removed",forcedAdded);report.addProperty("preexisting_forced_ticket_preserved",!forcedAdded);
  };
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(active->{
   if(active==server&&!cleaned[0]){cleanup.run();report.addProperty("status","aborted on fixture stop");try{Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception io){io.printStackTrace();}}
  });
  ServerTickEvents.END_SERVER_TICK.register(active->{if(active!=server||state[0]>=ids.length)return;
   try{
    if(state[0]<0){
     check(++state[2]<=100,"Fixture actor chunk never became native entity-ticking");
     if(!level.isPositionEntityTicking(arena))return;
     report.addProperty("waited_for_native_entity_ticking_ticks",state[2]);report.addProperty("entity_ticking_before_first_shot",true);
     state[0]=0;state[2]=0;
    }
    player.getCooldowns().tick(); // Detached synthetic player's native cooldown lifecycle; no socket/world player added.
    MtegDefinition d=MtegContent.DEFINITIONS.get(MtegContent.id(ids[state[0]]));MtegGunItem gun=(MtegGunItem)BuiltInRegistries.ITEM.getValue(d.id());
    if(state[1]==0){
     player.getInventory().clearContent();held[0]=new ItemStack(gun);held[0].set(ModDataComponents.GUN_AMMO.get(),0);player.setItemInHand(InteractionHand.MAIN_HAND,held[0]);player.getInventory().setItem(1,new ItemStack(BuiltInRegistries.ITEM.getValue(d.reloadAmmo()),Math.min(BuiltInRegistries.ITEM.getValue(d.reloadAmmo()).getDefaultMaxStackSize(),d.singleItem()?1:d.magazineSize())));
     check(gun.tryReload(level,player,held[0],InteractionHand.MAIN_HAND,false),"Actual MTEG reload start "+d.id());state[1]=1;state[2]=0;return;
    }
    if(state[1]==1){
     state[2]++;int completion=d.manual()?10:d.nextReloadTicks(0);
     if(state[2]<completion)check(gun.getMagazineAmmo(held[0])==0,"Early native reload completion "+d.id()+" tick"+state[2]);
     if(state[2]<completion)return;
     check(gun.getMagazineAmmo(held[0])==(d.manual()?d.reloadAmount():d.magazineSize()),"Native lifecycle reload result "+d.id());
     if(d.singleItem())check(player.getInventory().countItem(Items.BUCKET)==1,"Original lava bucket remainder");
     MtegReload.cancel(player);passes.add(d.id()+" actual END_SERVER_TICK reload boundary/source consumption PASS");
     if(d.id().getPath().equals("m1_garand"))held[0].set(ModDataComponents.GUN_AMMO.get(),1);
     state[1]=2;state[2]=0;
    }
    if(state[1]==2){
     if(player.getCooldowns().isOnCooldown(held[0]))return;
     check(level.isPositionEntityTicking(arena),"Fixture actor lost native entity-ticking precondition");
     int before=gun.getMagazineAmmo(held[0]);int loadsBefore=loaded.size();
     if(d.burst()&&state[3]==3){check(!gun.tryShoot(level,player,InteractionHand.MAIN_HAND),"Fourth FAMAS burst shot accepted");check(gun.getMagazineAmmo(held[0])==before,"Rejected burst consumed ammo");MtegGunItem.release(held[0]);check(gun.tryShoot(level,player,InteractionHand.MAIN_HAND),"FAMAS trigger release failed to reset cap");}
     else{check(gun.tryShoot(level,player,InteractionHand.MAIN_HAND),"Native MTEG shot failed "+d.id());check(gun.getMagazineAmmo(held[0])==before-1,"Native shot budget "+d.id());if(d.burst()){state[3]++;return;}}
     var projectiles=level.getEntitiesOfClass(MtegProjectile.class,player.getBoundingBox().inflate(160),e->e.getOwner()==player);
     JsonObject readback=new JsonObject();readback.addProperty("gun",d.id().toString());readback.addProperty("visible_owned_projectiles",projectiles.size());
     readback.addProperty("entity_load_events_this_shot",loaded.size()-loadsBefore);readback.addProperty("actor_chunk_entity_ticking",level.isPositionEntityTicking(arena));report.add("last_shot_readback",readback);
     check(!projectiles.isEmpty(),"No actual accessible MTEG projectile "+d.id());owned.addAll(projectiles);
     for(var projectile:projectiles)check(level.getEntity(projectile.getId())==projectile,"Native world lookup lost actual shot entity "+d.id());
     for(var e:projectiles)check(e.getDeltaMovement().length()>0,"Projectile failed to launch");
     passes.add(d.id()+" native authoritative shot/ammo/projectile"+(d.burst()?"/3shotcap+release":"")+" PASS");
     for(var e:owned)e.discard();owned.clear();MtegGunItem.release(held[0]);state[0]++;state[1]=0;state[3]=0;
     if(state[0]>=ids.length){MtegSpecialWorldChecks.run(level,player,report);report.addProperty("status","pass");report.addProperty("families",ids.length);cleanup.run();Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report));}
    }
   }catch(Throwable failure){report.addProperty("status","fail");report.addProperty("error",failure.toString());failure.printStackTrace();cleanup.run();state[0]=ids.length;try{Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception io){io.printStackTrace();}}
  });
 }
}
