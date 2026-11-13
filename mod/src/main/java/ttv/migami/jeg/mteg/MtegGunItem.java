package ttv.migami.jeg.mteg;
import java.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.GunAttachments;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.network.NetworkHandler;
/** Exact MTEG source weapons on the existing authoritative JEG input/ammo/attachment platform. */
public final class MtegGunItem extends GunItem {
 private final MtegDefinition definition;
 private static final Map<Player,Map<MtegGunItem,Spread>> SPREAD=new WeakHashMap<>();
 private static final Map<ItemStack,Integer> BURSTS=new WeakHashMap<>();
 private static final class Spread {long previous=-1;int count;}
 public MtegGunItem(Properties properties,MtegDefinition definition){super(properties,definition.stats());this.definition=definition;}
 @Override public void appendHoverText(ItemStack stack,net.minecraft.world.item.Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> add,net.minecraft.world.item.TooltipFlag flag){
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.damage",String.format(java.util.Locale.US,"%.1f",definition.damage()*GunAttachments.modifiers(stack).damageMultiplier())));
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.ammo",getMagazineAmmo(stack)+"/"+definition.magazineSize()));
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.ammo_type",new ItemStack(BuiltInRegistries.ITEM.getValue(definition.reloadAmmo())).getHoverName()));
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.headshot_multiplier","1.25x"));
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.advantage").append(net.minecraft.network.chat.Component.literal(definition.advantage().replace("jeg:",""))));
  add.accept(net.minecraft.network.chat.Component.translatable("info.jeg.projectiles",definition.pellets()));
 }
 public MtegDefinition definition(){return definition;}
 @Override public boolean isAutomatic(){return definition.automatic()||definition.burst();}
 @Override public boolean usesMagazineSwapReload(){return false;}
 @Override public boolean tryReload(Level level,Player player,ItemStack stack,InteractionHand hand,boolean notify){return !level.isClientSide()&&MtegReload.begin(player,stack,hand);}
 public static boolean isBurst(ItemStack stack){return stack.getItem() instanceof MtegGunItem gun&&gun.definition.burst();}
 public static void release(ItemStack stack){BURSTS.remove(stack);GunItem.clearTriggerLock(stack);}
 @Override public boolean tryShoot(Level level,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(definition.burst()&&(GunItem.isTriggerLocked(stack)||BURSTS.getOrDefault(stack,0)>=definition.burstAmount()))return false;
  if(!level.isClientSide())MtegReload.cancel(player);
  int before=getMagazineAmmo(stack);boolean success=super.tryShoot(level,player,hand);
  if(success&&!level.isClientSide()){
   updateSpread(player);
   if(definition.burst()) {int count=BURSTS.merge(stack,1,Integer::sum);if(count>=definition.burstAmount())stack.set(ModDataComponents.GUN_TRIGGER_LOCK.get(),true);}
   // Original GunFireEvent.Post precedes AmmoCount decrement; source checks one remaining round.
   if(definition.id().getPath().equals("m1_garand")&&hand==InteractionHand.MAIN_HAND&&before==1){
    var sound=BuiltInRegistries.SOUND_EVENT.getValue(MtegContent.id("item.m1_garand.ping"));
    level.playSound(null,player.getX(),player.getY(),player.getZ(),sound,SoundSource.MASTER,3F,1F);
   }
  }
  return success;
 }
 private void updateSpread(Player player){
  Spread spread=SPREAD.computeIfAbsent(player,p->new HashMap<>()).computeIfAbsent(this,g->new Spread());long now=System.currentTimeMillis();
  if(spread.previous!=-1){if(now-spread.previous<300){if(spread.count<10){spread.count++;if(spread.count<10&&!NetworkHandler.isAiming(player))spread.count++;}}else spread.count=0;}
  spread.previous=now;
 }
 public float spread(Player player,ItemStack stack){
  float value=definition.spread()*GunAttachments.modifiers(stack).spreadMultiplier();
  if(!definition.alwaysSpread()){var tracker=SPREAD.get(player);Spread spread=tracker==null?null:tracker.get(this);value*=spread==null?0:spread.count/10F;}
  return NetworkHandler.isAiming(player)?value*.5F:value;
 }
 @Override public void fireAt(Level level,LivingEntity shooter,ItemStack stack,LivingEntity target){
  float spread=shooter instanceof Player p?spread(p,stack):definition.spread()*GunAttachments.modifiers(stack).spreadMultiplier();
  Vec3 origin=shooter.getEyePosition();
  if(level instanceof ServerLevel server)NetworkHandler.sendGunFireFx(server,shooter.getId(),shooter.getRandom().nextFloat());
  for(int i=0;i<definition.pellets();i++){
   float pitch=shooter.getXRot(),yaw=shooter.getYRot();
   if(spread!=0){pitch+=shooter.getRandom().nextFloat()*spread-spread/2; yaw=shooter.getYHeadRot()+shooter.getRandom().nextFloat()*spread-spread/2;}
   spawn(level,shooter,stack,origin,Vec3.directionFromRotation(pitch,yaw));
  }
 }
 @Override public void fireDirectionallyFrom(Level level,LivingEntity shooter,ItemStack stack,Vec3 origin,Vec3 direction){
  for(int i=0;i<definition.pellets();i++)spawn(level,shooter,stack,origin,direction.normalize());
 }
 private void spawn(Level level,LivingEntity shooter,ItemStack stack,Vec3 origin,Vec3 direction){
  EntityType<MtegProjectile> type=switch(definition.ammo().toString()){
   case "mteg:taki" -> MtegContent.TAKI_ENTITY;case "minecraft:magma_cream" -> MtegContent.FIREBALL_ENTITY;case "mteg:flare" -> MtegContent.FLARE_ENTITY;default -> MtegContent.BULLET_ENTITY;
  };
  MtegProjectile projectile=new MtegProjectile(type,level);projectile.configure(shooter,definition,direction,Math.max(0,definition.damage()*GunAttachments.modifiers(stack).damageMultiplier()));projectile.setPos(origin);projectile.setLegacyEnchantments(ttv.migami.jeg.enchantment.LegacyGunEnchantments.capture(stack));level.addFreshEntity(projectile);
  if(level instanceof ServerLevel server&&type==MtegContent.BULLET_ENTITY){
   var packet=new MtegTrailPayload(new ttv.migami.jeg.network.BulletTrailPayload(projectile.getId(),projectile.position(),projectile.getDeltaMovement(),definition.stats().trailColor(),definition.projectileSize(),definition.projectileLife(),projectile.originalGravity(),shooter.getId(),true),definition.stats().trailLengthMultiplier());
   for(var player:server.getServer().getPlayerList().getPlayers())if(player.level()==server&&player.distanceToSqr(projectile)<=256D*256D)net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,packet);
  }
 }
}
