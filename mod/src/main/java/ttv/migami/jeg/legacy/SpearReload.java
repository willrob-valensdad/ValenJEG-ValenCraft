package ttv.migami.jeg.legacy;

import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.gun.GunStats;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.GunItem;

/** Original ReloadTracker manual path (MigaMiDev/JEG71bbf39, GPLv3), scoped to the
 * restored nonanimated spear. Native GunItem's generic reload clock is animation-gated.
 * Original start-relative modulo cadence and first-round additional timer are retained. */
public final class SpearReload {
    private SpearReload() {}
    private static final Map<Player,Reload> ACTIVE=new WeakHashMap<>();
    private static boolean registered;
    private static final class Reload {
        final ItemStack stack;final InteractionHand hand;final int slot;final ServerLevel level;
        int elapsed;boolean first=true;
        Reload(Player player,ItemStack stack,InteractionHand hand,ServerLevel level){this.stack=stack;this.hand=hand;this.slot=player.getInventory().getSelectedSlot();this.level=level;}
        boolean held(Player player){return player.isAlive()&&!player.isSpectator()&&player.level()==level&&player.getInventory().getSelectedSlot()==slot&&player.getItemInHand(hand)==stack&&!stack.isEmpty();}
    }
    public static void register() {
        if(registered)return;registered=true;
        ServerTickEvents.END_SERVER_TICK.register(server->{for(Player player:java.util.List.copyOf(ACTIVE.keySet())){Reload reload=ACTIVE.get(player);if(reload!=null&&reload.level.getServer()==server)tick(player);}});
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{Player player=handler.getPlayer();Reload reload=ACTIVE.get(player);if(reload!=null)cancel(player,reload.stack);});
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->{for(Player player:java.util.List.copyOf(ACTIVE.keySet())){Reload reload=ACTIVE.get(player);if(reload!=null&&reload.level.getServer()==server)cancel(player,reload.stack);}});
    }
    /** Exact original: empty base+additional, then first manual round adds additional again. */
    public static int interval(GunStats stats,int ammo,boolean first) {
        return Math.max(1,stats.reloadTime()+(ammo==0?stats.additionalReloadTime():0)+(first?stats.additionalReloadTime():0));
    }
    public static boolean ready(GunStats stats,int elapsed,int ammo,boolean first) {return elapsed>0&&elapsed%interval(stats,ammo,first)==0;}
    public static boolean active(Player player){return ACTIVE.containsKey(player);}
    public static boolean begin(Player player,ItemStack stack,InteractionHand hand) {
        if(!(player.level() instanceof ServerLevel level)||!(stack.getItem() instanceof AtlanteanSpearItem spear)||player.getItemInHand(hand)!=stack||!player.isAlive()||player.isSpectator()||GunItem.isDrawOperationLocked(stack))return false;
        Reload old=ACTIVE.get(player);
        if(old!=null){if(old.held(player)&&old.stack==stack)return false;cancel(player,old.stack);}
        // Old process/drop copies have no live original tracker; do not retain orphan visual state.
        clear(stack);
        if(ammo(stack)>=spear.getStats().magazineSize()||!hasAmmo(player))return false;
        Reload reload=new Reload(player,stack,hand,level);ACTIVE.put(player,reload);markers(reload,spear.getStats(),ammo(stack));return true;
    }
    public static void tick(Player player) {
        Reload reload=ACTIVE.get(player);if(reload==null)return;
        if(!reload.held(player)||!GunItem.isReloading(reload.stack)){cancel(player,reload.stack);return;}
        AtlanteanSpearItem spear=(AtlanteanSpearItem)reload.stack.getItem();GunStats stats=spear.getStats();int ammo=ammo(reload.stack);
        if(ammo>=stats.magazineSize()||!hasAmmo(player)){cancel(player,reload.stack);return;}
        reload.elapsed++;
        if(reload.elapsed==4)play(reload,player,stats.reloadStartSoundEvent());
        if(ready(stats,reload.elapsed,ammo,reload.first)) {
            if(!player.getAbilities().instabuild){
                ItemStack water=findAmmo(player);if(water.isEmpty()){cancel(player,reload.stack);return;}water.shrink(1);player.getInventory().setChanged();
            }
            ammo++;reload.stack.set(ModDataComponents.GUN_AMMO.get(),ammo);reload.first=false;play(reload,player,stats.reloadLoadSoundEvent());
            if(ammo>=stats.magazineSize()||!hasAmmo(player)){
                cancel(player,reload.stack);
                ttv.migami.jeg.advancement.GameplayActions.action(player,"reload",stats.id());
                ttv.migami.jeg.advancement.GameplayActions.action(player,"reload_type",stats.reloadType());
                return;
            }
        }
        markers(reload,stats,ammo);
    }
    public static void cancel(Player player,ItemStack stack) {Reload reload=ACTIVE.get(player);if(reload!=null&&reload.stack==stack)ACTIVE.remove(player);clear(stack);}
    private static int ammo(ItemStack stack){return Math.max(0,stack.getOrDefault(ModDataComponents.GUN_AMMO.get(),0));}
    private static ItemStack findAmmo(Player player){
        var water=ttv.migami.jeg.init.ModItems.AMMO.get(ttv.migami.jeg.Reference.id("water_bomb")).get();
        for(int i=0;i<player.getInventory().getContainerSize();i++){var stack=player.getInventory().getItem(i);if(stack.is(water)&&!stack.isEmpty())return stack;}return ItemStack.EMPTY;
    }
    private static boolean hasAmmo(Player player){return player.getAbilities().instabuild||!findAmmo(player).isEmpty();}
    private static void markers(Reload reload,GunStats stats,int ammo){
        int total=interval(stats,ammo,reload.first),remaining=total-reload.elapsed%total;
        reload.stack.set(ModDataComponents.GUN_RELOAD_TICKS_TOTAL.get(),total);
        reload.stack.set(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get(),remaining);
        reload.stack.set(ModDataComponents.GUN_RELOAD_END_TICK.get(),reload.level.getGameTime()+remaining);
    }
    private static void clear(ItemStack stack){stack.remove(ModDataComponents.GUN_RELOAD_TICKS_TOTAL.get());stack.remove(ModDataComponents.GUN_RELOAD_TICKS_REMAINING.get());stack.remove(ModDataComponents.GUN_RELOAD_END_TICK.get());stack.remove(ModDataComponents.GUN_RELOAD_STAGE.get());}
    private static void play(Reload reload,Player player,java.util.Optional<net.minecraft.sounds.SoundEvent> sound){sound.ifPresent(value->reload.level.playSound(null,player.getX(),player.getY(),player.getZ(),value,SoundSource.PLAYERS,1F,1F));}
}
