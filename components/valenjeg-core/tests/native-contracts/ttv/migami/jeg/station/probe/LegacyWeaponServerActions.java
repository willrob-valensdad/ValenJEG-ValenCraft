package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.entity.TimedThrowableItemProjectile;
import ttv.migami.jeg.legacy.*;
/** Authoritative readback/preparation only. No direct weapon use, shoot, reload or hit invocation. */
final class LegacyWeaponServerActions {
    private static final Set<String> ITEMS=Set.of("jeg:compound_bow","jeg:atlantean_spear","jeg:explosive_charge","jeg:ammo_box");
    static void prepare(MinecraftServer server,ServerPlayer player,JsonObject request,JsonObject result) {
        if(!Boolean.getBoolean("valen.station.probe")||server.getPort()!=25589||!player.getName().getString().equals("ValenPackProbe"))throw new IllegalStateException("Requires copied fixture and synthetic player");
        String id=request.get("item").getAsString();if(!ITEMS.contains(id)&&!ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.containsKey(Identifier.parse(id))&& !id.equals("mteg:trumpet"))throw new IllegalArgumentException("Unscoped fixture item "+id);
        if(!player.isAlive())throw new IllegalStateException("Use the ordinary Respawn button before fixture preparation");
        Integer initialAmmo=null,reserveCount=null;
        if(request.has("initial_ammo")||request.has("reserve_count")){
            Identifier parsed=Identifier.parse(id);var definition=ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.get(parsed);
            Item registered=BuiltInRegistries.ITEM.getValue(parsed);
            if(definition==null||!(registered instanceof ttv.migami.jeg.mteg.MtegGunItem))throw new IllegalArgumentException("initial_ammo and reserve_count require a registered MTEG gun");
            Item ammoItem=BuiltInRegistries.ITEM.getValue(definition.reloadAmmo());int stackLimit=ammoItem.getDefaultMaxStackSize();
            if(request.has("initial_ammo")){initialAmmo=fixtureInt(request.get("initial_ammo"),"initial_ammo");if(initialAmmo>definition.magazineSize())throw new IllegalArgumentException("initial_ammo exceeds maxAmmo "+definition.magazineSize());}
            if(request.has("reserve_count")){reserveCount=fixtureInt(request.get("reserve_count"),"reserve_count");if(reserveCount>stackLimit)throw new IllegalArgumentException("reserve_count exceeds ammo stack limit "+stackLimit);}
        }
        ServerLevel level=server.overworld();player.closeContainer();player.getInventory().clearContent();player.setGameMode(GameType.SURVIVAL);
        // Own a bounded dry arena before teleport, including clearance and explicit native readback.
        for(int x=-4;x<=4;x++)for(int z=-32;z<=-8;z++){
            level.setBlock(new BlockPos(x,219,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
            for(int y=220;y<=222;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        BlockPos feet=new BlockPos(0,220,-13);
        if(!level.getBlockState(feet.below()).is(Blocks.SMOOTH_STONE)||!level.getBlockState(feet).isAir()||!level.getBlockState(feet.above()).isAir())
            throw new IllegalStateException("Native fixture spawn floor/clearance readback failed");
        player.teleportTo(level,.5,220,-12.5,Set.of(),180,0,false);
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);player.fallDistance=0;
        player.setYHeadRot(180);player.yHeadRotO=180;player.yBodyRot=180;player.yBodyRotO=180;player.setOldPosAndRot();
        player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);player.getFoodData().setSaturation(5);
        player.setAirSupply(player.getMaxAirSupply());player.setRemainingFireTicks(0);
        JsonObject spawn=new JsonObject();spawn.addProperty("floor",BuiltInRegistries.BLOCK.getKey(level.getBlockState(feet.below()).getBlock()).toString());spawn.addProperty("feet_air",level.getBlockState(feet).isAir());spawn.addProperty("head_air",level.getBlockState(feet.above()).isAir());spawn.addProperty("x",player.getX());spawn.addProperty("y",player.getY());spawn.addProperty("z",player.getZ());spawn.addProperty("health",player.getHealth());spawn.addProperty("food",player.getFoodData().getFoodLevel());spawn.addProperty("head_yaw",player.getYHeadRot());result.add("verified_weapon_spawn",spawn);
        ItemStack held=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)),id.equals("jeg:explosive_charge")?2:1);
        if(id.equals("jeg:atlantean_spear")) {held.set(ModDataComponents.GUN_AMMO.get(),0);player.getInventory().setItem(1,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:water_bomb")),12));}
        if(id.equals("jeg:compound_bow"))player.getInventory().setItem(1,new ItemStack(Items.ARROW,8));
        if(held.getItem() instanceof ttv.migami.jeg.mteg.MtegGunItem gun){
            if(initialAmmo==null&&reserveCount==null){held.set(ModDataComponents.GUN_AMMO.get(),0);var ammo=BuiltInRegistries.ITEM.getValue(gun.definition().reloadAmmo());player.getInventory().setItem(1,new ItemStack(ammo,Math.min(ammo.getDefaultMaxStackSize(),Math.max(2,gun.magazineSize()*2))));}
            else{
                int ammoCount=initialAmmo==null?0:initialAmmo;held.set(ModDataComponents.GUN_AMMO.get(),ammoCount);
                var ammo=BuiltInRegistries.ITEM.getValue(gun.definition().reloadAmmo());
                int reserve=reserveCount==null?Math.min(ammo.getDefaultMaxStackSize(),Math.max(2,gun.magazineSize()*2)):reserveCount;
                if(reserve>0)player.getInventory().setItem(1,new ItemStack(ammo,reserve));
                result.addProperty("weapon_behavior_invoked",false);result.addProperty("setup_health_reset",true);result.addProperty("setup_initial_ammo",ammoCount);result.addProperty("setup_reserve_count",reserve);result.addProperty("earned_ammo",false);
            }
        }
        player.getInventory().setSelectedSlot(0);player.setItemInHand(InteractionHand.MAIN_HAND,held);player.inventoryMenu.broadcastChanges();
        result.addProperty("result","Actual native items prepared on separate copied-fixture arena; no weapon behavior invoked; client input still required");
    }
    private static int fixtureInt(JsonElement element,String key){
        if(element==null||element.isJsonNull()||!element.isJsonPrimitive()||!element.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException(key+" must be an integer");
        String raw=element.getAsString();if(!raw.matches("\\d+"))throw new IllegalArgumentException(key+" must be a non-negative integer");
        return Integer.parseInt(raw); // Reject overflow before any arena or inventory mutation.
    }
    static void state(ServerLevel level,ServerPlayer player,JsonObject receipt) {
        JsonObject state=new JsonObject();state.addProperty("server_game_time",level.getGameTime());state.addProperty("held_item",BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString());state.addProperty("held_count",player.getMainHandItem().getCount());state.addProperty("gun_ammo",player.getMainHandItem().getOrDefault(ModDataComponents.GUN_AMMO.get(),0));state.addProperty("reload_remaining_ticks",player.getMainHandItem().getOrDefault(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get(),0));state.addProperty("is_using_item",player.isUsingItem());state.addProperty("use_remaining_ticks",player.getUseItemRemainingTicks());state.addProperty("cooldown",player.getCooldowns().isOnCooldown(player.getMainHandItem()));
        JsonArray entities=new JsonArray();for(Entity entity:level.getEntitiesOfClass(Entity.class,player.getBoundingBox().inflate(128),e->e instanceof ttv.migami.jeg.mteg.MtegProjectile||e instanceof LegacyArrowEntity||e instanceof WhirlpoolProjectile||e instanceof LandWhirlpool||e instanceof ExplosiveChargeEntity)) {
            JsonObject row=new JsonObject();row.addProperty("type",BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());row.addProperty("uuid",entity.getUUID().toString());row.addProperty("alive",entity.isAlive());row.addProperty("x",entity.getX());row.addProperty("y",entity.getY());row.addProperty("z",entity.getZ());row.addProperty("speed",entity.getDeltaMovement().length());
            if(entity instanceof Projectile projectile&&projectile.getOwner()!=null)row.addProperty("owner",projectile.getOwner().getUUID().toString());
            if(entity instanceof ExplosiveChargeEntity charge){row.addProperty("landed",charge.landed());row.addProperty("fuse",charge.getFuse());row.addProperty("passenger",charge.isPassenger());}
            if(entity instanceof WhirlpoolProjectile water)row.addProperty("fuse",water.getFuse());entities.add(row);
        }
        state.add("actual_entities",entities);JsonArray drops=new JsonArray();for(var item:level.getEntitiesOfClass(ItemEntity.class,new AABB(-5,218,-34,5,224,-6))) {JsonObject row=new JsonObject();row.addProperty("item",BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString());row.addProperty("count",item.getItem().getCount());drops.add(row);}state.add("actual_arena_drops",drops);receipt.add("weapon_authoritative_state",state);
    }
}
