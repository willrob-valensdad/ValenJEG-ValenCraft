package ttv.migami.jeg.mteg;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.GunItem;
/** Native lifecycle adapter for the exact dependency5322334 ReloadTracker paths.
 * Manual remains one source round per10ticks; magazine/single-item preserve strict timer boundary.
 * Like the original tracker, state belongs to one real held stack and ends on swap/full/no ammo. */
public final class MtegReload {
 private MtegReload(){}
 private static final Map<Player,Reload> ACTIVE=new WeakHashMap<>();private static boolean registered;
 private static final class Reload {
  final ItemStack stack;final InteractionHand hand;final int slot;final ServerLevel level;final MtegDefinition definition;final int completion;int elapsed;
  Reload(Player p,ItemStack s,InteractionHand h,ServerLevel l,MtegDefinition d){stack=s;hand=h;slot=p.getInventory().getSelectedSlot();level=l;definition=d;completion=d.nextReloadTicks(ammo(s));}
  boolean held(Player p){return p.isAlive()&&!p.isSpectator()&&p.level()==level&&p.getInventory().getSelectedSlot()==slot&&p.getItemInHand(hand)==stack&&!stack.isEmpty();}
 }
 public static void register(){
  if(registered)return;registered=true;
  ServerTickEvents.END_SERVER_TICK.register(server->{for(Player p:List.copyOf(ACTIVE.keySet())){Reload r=ACTIVE.get(p);if(r!=null&&r.level.getServer()==server)tick(p);}});
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->cancel(handler.getPlayer()));
  ServerLifecycleEvents.SERVER_STOPPING.register(server->{for(Player p:List.copyOf(ACTIVE.keySet())){Reload r=ACTIVE.get(p);if(r!=null&&r.level.getServer()==server)cancel(p);}});
 }
 public static boolean begin(Player player,ItemStack stack,InteractionHand hand){
  if(!(player.level() instanceof ServerLevel level)||!(stack.getItem() instanceof MtegGunItem gun)||player.getItemInHand(hand)!=stack||!player.isAlive()||player.isSpectator()||GunItem.isDrawOperationLocked(stack))return false;
  Reload old=ACTIVE.get(player);if(old!=null){if(old.held(player)&&old.stack==stack)return false;cancel(player);}
  clear(stack);MtegDefinition definition=gun.definition();
  if(ammo(stack)>=definition.magazineSize()||available(player,definition.reloadAmmo())==0&&!player.getAbilities().instabuild)return false;
  Reload reload=new Reload(player,stack,hand,level,definition);ACTIVE.put(player,reload);markers(reload);play(reload,player,definition.sound("reload"));return true;
 }
 public static void tick(Player player){
  Reload reload=ACTIVE.get(player);if(reload==null)return;
  if(!reload.held(player)||!GunItem.isReloading(reload.stack)){cancel(player);return;}
  MtegDefinition d=reload.definition;int ammo=ammo(reload.stack),available=player.getAbilities().instabuild?Integer.MAX_VALUE:available(player,d.reloadAmmo());
  if(ammo>=d.magazineSize()||available==0){cancel(player);return;}
  reload.elapsed++;
  boolean ready=d.manual()?reload.elapsed>0&&reload.elapsed%10==0:reload.elapsed>=reload.completion;
  if(ready){
   int rounds=d.roundsToLoad(ammo,available);if(rounds<=0){cancel(player);return;}
   if(!player.getAbilities().instabuild){consume(player,d.reloadAmmo(),d.singleItem()?1:rounds);if(d.singleItem()&&d.reloadAmmo().toString().equals("minecraft:lava_bucket"))player.getInventory().placeItemBackInInventory(new ItemStack(Items.BUCKET),net.minecraft.util.Prediction.SERVER_ONLY);}
   reload.stack.set(ModDataComponents.GUN_AMMO.get(),ammo+rounds);player.getInventory().setChanged();
   if(d.manual())play(reload,player,d.sound("reload"));
   if(!d.manual()||ammo+rounds>=d.magazineSize()||!player.getAbilities().instabuild&&available(player,d.reloadAmmo())==0){
    cancel(player);play(reload,player,d.sound("cock"));ttv.migami.jeg.advancement.GameplayActions.action(player,"reload",d.id());ttv.migami.jeg.advancement.GameplayActions.action(player,"reload_type",d.reloadType());return;
   }
  }
  markers(reload);
 }
 public static boolean active(Player player){return ACTIVE.containsKey(player);}
 public static void cancel(Player player){Reload reload=ACTIVE.remove(player);if(reload!=null)clear(reload.stack);}
 public static int ammo(ItemStack stack){return Math.max(0,stack.getOrDefault(ModDataComponents.GUN_AMMO.get(),0));}
 private static int available(Player player,net.minecraft.resources.Identifier id){Item item=BuiltInRegistries.ITEM.getValue(id);int n=0;for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack s=player.getInventory().getItem(i);if(s.is(item))n+=s.getCount();}return n;}
 private static void consume(Player player,net.minecraft.resources.Identifier id,int count){Item item=BuiltInRegistries.ITEM.getValue(id);for(int i=0;i<player.getInventory().getContainerSize()&&count>0;i++){ItemStack s=player.getInventory().getItem(i);if(!s.is(item))continue;int n=Math.min(count,s.getCount());s.shrink(n);count-=n;}if(count!=0)throw new IllegalStateException("MTEG original reload source budget changed");}
 private static void markers(Reload r){int remaining=r.definition.manual()?10-r.elapsed%10:Math.max(1,r.completion-r.elapsed);r.stack.set(ModDataComponents.GUN_RELOAD_TICKS_TOTAL.get(),r.definition.manual()?10:r.completion);r.stack.set(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get(),remaining);r.stack.set(ModDataComponents.GUN_RELOAD_END_TICK.get(),r.level.getGameTime()+remaining);}
 private static void clear(ItemStack s){s.remove(ModDataComponents.GUN_RELOAD_TICKS_TOTAL.get());s.remove(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get());s.remove(ModDataComponents.GUN_RELOAD_END_TICK.get());s.remove(ModDataComponents.GUN_RELOAD_STAGE.get());}
 private static void play(Reload r,Player p,net.minecraft.resources.Identifier id){if(id!=null&&BuiltInRegistries.SOUND_EVENT.containsKey(id))r.level.playSound(null,p.getX(),p.getY(),p.getZ(),BuiltInRegistries.SOUND_EVENT.getValue(id),SoundSource.PLAYERS,1F,1F);}
}
