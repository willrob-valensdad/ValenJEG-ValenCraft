package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.mteg.*;

/** Fixture-only real native flight/collision checks. Root alone launches the fixture.
 * Uses the registered module and actual native entities; no model or network proof claim. */
public final class MtegSpecialWorldChecks {
    private MtegSpecialWorldChecks(){}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void close(double actual,double expected,String message){check(Math.abs(actual-expected)<.00001,message+": "+actual+" != "+expected);}
    public static void run(ServerLevel level,ServerPlayer player,JsonObject receipt){
        if(!Boolean.getBoolean("valen.mteg.probe"))return;
        JsonArray checks=new JsonArray();receipt.add("mteg_special_checks",checks);
        Vec3 origin=player.position().add(0,20,0);
        List<Entity> owned=new ArrayList<>();
        var savedPosition=player.position();float savedYaw=player.getYRot(),savedPitch=player.getXRot();
        float savedHead=player.yHeadRot,savedOldHead=player.yHeadRotO,savedBody=player.yBodyRot,savedOldBody=player.yBodyRotO;
        var savedHeld=player.getMainHandItem().copy();boolean creative=player.getAbilities().instabuild;
        var trumpet=MtegContent.TRUMPET.getDefaultInstance();
        check(!player.getCooldowns().isOnCooldown(trumpet),"Refused existing native trumpet cooldown");

        try{
            var requiredTags=Map.of(
                MtegProjectile.HEAVY,List.of("minecraft:ghast","minecraft:hoglin","minecraft:polar_bear","minecraft:ravager","minecraft:sniffer","minecraft:turtle","minecraft:zoglin"),
                MtegProjectile.VERY_HEAVY,List.of("minecraft:elder_guardian","minecraft:ender_dragon","minecraft:warden","minecraft:wither"),
                MtegProjectile.UNDEAD,List.of("minecraft:zombie","minecraft:drowned","minecraft:husk","minecraft:phantom","minecraft:skeleton","minecraft:skeleton_horse","minecraft:stray","minecraft:wither","minecraft:wither_skeleton","minecraft:zoglin","minecraft:zombie_horse","minecraft:zombified_piglin","minecraft:zombie_villager","jeg:ghoul"));
            for(var entry:requiredTags.entrySet())for(String id:entry.getValue()){
                var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse(id));
                check(type!=null&&type.builtInRegistryHolder().is(entry.getKey()),"Unpopulated original MTEG advantage tag:"+entry.getKey()+"/"+id);
            }
            var booId=net.minecraft.resources.Identifier.parse("jeg:boo");
            if(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(booId))
                check(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(booId).builtInRegistryHolder().is(MtegProjectile.GHOST),"Original optional Boo ghost member lost");
            else receipt.addProperty("mteg_ghost_limit","Original ghost tag has only optional jeg:boo; that creature is absent from current native fork, no substitute added");
            checks.add("Private MTEG native advantage tags contain all exact available source members; missing original Boo remains optional");
            for(var definition:MtegContent.DEFINITIONS.values()){
                var p=new MtegProjectile(MtegContent.BULLET_ENTITY,level);owned.add(p);
                p.configure(player,definition,new Vec3(0,1,0),definition.damage());p.setPos(origin);
                close(p.getDeltaMovement().length(),definition.projectileSpeed(),definition.id()+" source speed");
                close(p.getDimensions(Pose.STANDING).width(),definition.projectileSize(),definition.id()+" source size");
                check(p.lifetime()==definition.projectileLife(),definition.id()+" source life");
                p.tick();
                close(p.getY()-origin.y,definition.projectileSpeed(),definition.id()+" move-before-gravity");
                close(p.getDeltaMovement().y,definition.projectileSpeed()+(definition.gravity()?-.04:0),definition.id()+" source gravity/no drag");
                check(p.age()==1,definition.id()+" exact lifetime clock");p.discard();
                var source=definition.source().getAsJsonObject("projectile");
                check(!source.has("visible")||!source.get("visible").getAsBoolean(),definition.id()+" unexpected item-geometry renderer need");
            }
            checks.add("35 actual registered definitions: native velocity, size, life, move-before-gravity/no-drag; all source invisible item geometry");
            String[] guns={"blue_heat","m2_flamethrower","flare_gun"};int[] seconds={5,10,20};
            for(int i=0;i<guns.length;i++){
                var definition=MtegContent.DEFINITIONS.get(MtegContent.id(guns[i]));
                var cow=(net.minecraft.world.entity.Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse("minecraft:cow")).create(level,EntitySpawnReason.COMMAND);check(cow!=null,"Native cow creation failed");owned.add(cow);
                cow.setPos(origin.add(1.5,0,0));cow.setNoAi(true);level.addFreshEntity(cow);
                var type=i==0?MtegContent.TAKI_ENTITY:i==1?MtegContent.FIREBALL_ENTITY:MtegContent.FLARE_ENTITY;
                var p=new MtegProjectile(type,level);owned.add(p);p.configure(player,definition,new Vec3(1,0,0),definition.damage());p.setPos(origin.add(0,.3,0));
                float health=cow.getHealth();p.tick();
                close(health-cow.getHealth(),definition.damage()/definition.pellets(),guns[i]+" real body damage/pellet count");
                check(cow.getRemainingFireTicks()==seconds[i]*20,guns[i]+" source seconds on fire");
                check(cow.getInvulnerableTime()==0,guns[i]+" original invulnerability reset");
                check(!p.isAlive(),guns[i]+" actual entity collision did not remove projectile");cow.discard();
            }
            checks.add("Actual native cow collisions: Taki5s, fireball10s, flare20s, source per-pellet damage, owner attribution/removal/invulnerability reset");
            var taki=MtegContent.TAKI.getDefaultInstance();var food=taki.get(DataComponents.FOOD);var consumable=taki.get(DataComponents.CONSUMABLE);
            check(food!=null&&food.nutrition()==4,"Taki native food missing");close(food.saturation(),.8,"Taki native saturation4*.1*2");
            check(consumable!=null&&consumable.consumeTicks()==16&&!consumable.onConsumeEffects().isEmpty(),"Taki original fast consumption/effect component missing");
            checks.add("Registered Taki actual native food4/.1 and16tick consumable/effect components");
            check(MtegProjectile.advantageMultiplier("jeg:very_heavy",false,true,false,false)==.25F,"Silently used later-master very-heavy advantage");
            check(MtegProjectile.advantageMultiplier("jeg:heavy",false,true,false,false)==1F,"Exact5322334 heavy advantage lost");
            var warden=(net.minecraft.world.entity.Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse("minecraft:warden")).create(level,EntitySpawnReason.COMMAND);
            check(warden!=null,"Native Warden creation failed");owned.add(warden);warden.setNoAi(true);
            // A detached actor has no ordinary player tick to synchronize body/head rotation.
            // Original pick(...,0,...) reads OLD HEAD yaw, so set the complete native pose.
            var trumpetOrigin=origin;player.setPos(trumpetOrigin);player.setYRot(-90);player.setXRot(0);
            player.setYHeadRot(-90);player.yHeadRotO=-90;player.yBodyRot=-90;player.yBodyRotO=-90;player.setOldPosAndRot();
            warden.setPos(trumpetOrigin.add(3,0,0));
            check(level.isPositionEntityTicking(player.blockPosition())&&level.isPositionEntityTicking(warden.blockPosition()),"Trumpet actors left the owned native entity-ticking chunk");
            check(level.addFreshEntity(warden),"Actual native Warden spawn rejected");
            player.getAbilities().instabuild=false;player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,trumpet);
            var rayStart=player.getEyePosition();var blockPick=player.pick(25,0,false);var rayEnd=blockPick.getLocation();
            var ray=player.getViewVector(0);
            var selected=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player,rayStart,rayEnd,new net.minecraft.world.phys.AABB(rayStart,rayEnd),entity->entity instanceof LivingEntity,rayStart.distanceToSqr(rayEnd));
            JsonObject diagnostic=new JsonObject();receipt.add("trumpet_native_preflight",diagnostic);
            diagnostic.addProperty("body_yaw",player.getYRot());diagnostic.addProperty("head_yaw",player.yHeadRot);diagnostic.addProperty("old_head_yaw",player.yHeadRotO);
            diagnostic.addProperty("view_partial0",ray.toString());diagnostic.addProperty("ray_start",rayStart.toString());diagnostic.addProperty("ray_end",rayEnd.toString());
            diagnostic.addProperty("block_pick_type",blockPick.getType().toString());diagnostic.addProperty("target_bounds",warden.getBoundingBox().toString());
            diagnostic.addProperty("target_native_lookup",level.getEntity(warden.getId())==warden);diagnostic.addProperty("target_entity_ticking",level.isPositionEntityTicking(warden.blockPosition()));
            diagnostic.addProperty("target_invulnerable",warden.isInvulnerable());diagnostic.addProperty("target_invulnerable_ticks",warden.getInvulnerableTime());
            diagnostic.addProperty("selected_target",selected==null?"none":net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(selected.getEntity().getType()).toString());
            diagnostic.addProperty("selected_exact_warden",selected!=null&&selected.getEntity()==warden);
            check(ray.x>.999&&Math.abs(ray.z)<.00001,"Detached native head pose is not aiming at the test Warden");
            check(level.getEntity(warden.getId())==warden&&selected!=null&&selected.getEntity()==warden,"Original native trumpet ray did not select the actual test Warden");
            check(!warden.isInvulnerable()&&warden.getInvulnerableTime()==0,"Native trumpet target is unexpectedly invulnerable");
            float health=warden.getHealth();MtegContent.TRUMPET.use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND);
            diagnostic.addProperty("target_health_before",health);diagnostic.addProperty("target_health_after",warden.getHealth());
            close(health-warden.getHealth(),10,"Actual native trumpet sonic damage");
            check(player.getCooldowns().isOnCooldown(trumpet)&&trumpet.getDamageValue()==1,"Trumpet native40tick cooldown or1durability not applied");
            var deaf=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ttv.migami.jeg.init.ModEffects.DEAFENED.get());
            check(warden.hasEffect(deaf)&&warden.getEffect(deaf).getDuration()==100,"Trumpet source deaf100 absent");
            check(warden.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)&&warden.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getDuration()==100,"Trumpet Warden weakness100 absent");
            check(warden.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)&&warden.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS).getDuration()==200&&warden.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS).getAmplifier()==2,"Trumpet Warden slow200amp2 absent");
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(trumpet));
            trumpet.setDamageValue(63);MtegContent.TRUMPET.use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND);
            check(!trumpet.isEmpty()&&trumpet.getDamageValue()==63,"Original trumpet63durability floor changed");
            checks.add("Actual native trumpet use: sonic10,40tick cooldown,1durability,source63floor,deaf100,Wardenweak100/slow200amp2");
            receipt.addProperty("mteg_special_status","pass");
        }finally{
            for(Entity entity:owned)entity.discard();
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(trumpet));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,savedHeld);player.getAbilities().instabuild=creative;
            player.setPos(savedPosition);player.setYRot(savedYaw);player.setXRot(savedPitch);player.setOldPosAndRot();
            player.yHeadRot=savedHead;player.yHeadRotO=savedOldHead;player.yBodyRot=savedBody;player.yBodyRotO=savedOldBody;
        }
    }
}
