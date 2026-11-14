package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.legacy.*;
/** Focused fixture-only real native player/inventory/component checks; root owns execution.
 * tick() is the exact callback used by the production ServerTickEvents listener. */
public final class SpearReloadNativeChecks {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static int ammo(ItemStack stack){return stack.getOrDefault(ModDataComponents.GUN_AMMO.get(),0);}
    private static ItemStack water(int count){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:water_bomb")),count);}
    private static ItemStack spear(){return BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:atlantean_spear")).getDefaultInstance();}
    public static void run(ServerLevel level,ServerPlayer player,JsonObject receipt) {
        if(!Boolean.getBoolean("valen.legacy.probe"))return;
        check(!SpearReload.active(player),"Focused reload probe refused an already active reload");
        List<ItemStack> saved=new ArrayList<>();for(int i=0;i<player.getInventory().getContainerSize();i++)saved.add(player.getInventory().getItem(i).copy());int selected=player.getInventory().getSelectedSlot();
        JsonArray passed=new JsonArray();receipt.add("spear_reload_checks",passed);
        try {
            player.getInventory().clearContent();player.getInventory().setSelectedSlot(0);ItemStack stack=spear();player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.getInventory().setItem(1,water(12));var gun=(AtlanteanSpearItem)stack.getItem();
            check(!gun.usesMagazineSwapReload(),"Spear incorrectly bound to magazine mode");
            check(gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Native nonanimated spear reload refused actual water bombs");
            check(GunItem.isReloading(stack)&&!gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Reload clock absent or duplicate request accepted");
            List<Integer> rounds=new ArrayList<>();int previous=0;
            for(int tick=1;tick<=112;tick++){
                SpearReload.tick(player);int current=ammo(stack);if(current!=previous){rounds.add(tick);previous=current;}
                check(player.getInventory().getItem(1).getCount()+current==12,"Reload duplicated/lost water bombs at tick"+tick);
                if(tick==26||tick==35)check(current==0,"Spear silently used generic26tick full-magazine reload");
            }
            check(rounds.equals(List.of(36,48,64,80,96,112)),"Original start-relative manual cadence changed:"+rounds);
            check(ammo(stack)==6&&player.getInventory().getItem(1).getCount()==6&&!GunItem.isReloading(stack)&&!SpearReload.active(player),"Full six-round completion did not clear native tracker/components");
            check(!gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Full spear consumed extra ammunition");passed.add("Real native request/tick path: empty36/48/64/80/96/112,1waterbomb per round,capacity6,12->6inventory,no26tick fake refill,no duplicate/full reload");
            stack.set(ModDataComponents.GUN_AMMO.get(),4);player.getInventory().setItem(1,water(12));check(gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Partial reload refused");
            for(int i=1;i<=32;i++){SpearReload.tick(player);if(i==25)check(ammo(stack)==4,"Partial first round early");if(i==26)check(ammo(stack)==5,"Partial first round not26");}
            check(ammo(stack)==6&&player.getInventory().getItem(1).getCount()==10,"Partial capacity/consumption changed");passed.add("Actual partial4->5at26/6at32 consumes exactly2waterbombs");
            stack.set(ModDataComponents.GUN_AMMO.get(),0);player.getInventory().setItem(1,water(1));check(gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Single available ammo rejected");for(int i=0;i<36;i++)SpearReload.tick(player);
            check(ammo(stack)==1&&player.getInventory().getItem(1).isEmpty()&&!SpearReload.active(player),"Insufficient-ammo termination changed");stack.set(ModDataComponents.GUN_AMMO.get(),0);check(!gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true)&&!GunItem.isReloading(stack),"No-ammo reload left a permanent lock");passed.add("Native1-ammo partial completion and no-ammo rejection clear markers");
            player.getInventory().setItem(1,water(2));check(gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Cancellation setup failed");for(int i=0;i<10;i++)SpearReload.tick(player);player.getInventory().setItem(2,new ItemStack(Items.STICK));player.getInventory().setSelectedSlot(2);SpearReload.tick(player);
            check(!SpearReload.active(player)&&!GunItem.isReloading(stack)&&ammo(stack)==0&&player.getInventory().getItem(1).getCount()==2,"Held-slot switch consumed ammo or retained tracker");player.getInventory().setSelectedSlot(0);
            check(gun.tryReload(level,player,stack,InteractionHand.MAIN_HAND,true),"Fresh reload after slot cancellation failed");GunItem.cancelReloadForImmediateAction(player,stack);SpearReload.tick(player);check(!SpearReload.active(player)&&ammo(stack)==0&&player.getInventory().getItem(1).getCount()==2,"Native immediate-action cancellation failed");passed.add("Actual selected-slot and existing GunItem immediate-action cancellation preserve ammo and unlock fresh requests");
            receipt.addProperty("spear_reload_status","pass");
        } finally {SpearReload.cancel(player,player.getMainHandItem());for(int i=0;i<saved.size();i++)player.getInventory().setItem(i,saved.get(i));player.getInventory().setSelectedSlot(selected);}
    }
}
