package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.MagazineItem;
import ttv.migami.jeg.item.attachment.*;
/** Temporary synthetic-inventory setup only; all menu/reload actions remain native client packets. */
final class CustomizationServerActions {
 private static List<ItemStack> saved;private static int selected;
 static void action(MinecraftServer server,ServerPlayer player,JsonObject request,JsonObject result){
  if(!Boolean.getBoolean("valen.customization.audit")||!player.getUUID().toString().equals("08d8976e-e59a-3751-ac2c-140b28fb5309")||!server.getServerDirectory().toAbsolutePath().normalize().toString().equals(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server"))throw new IllegalStateException("Refusing owner/nonfixture customization action");
  String action=request.get("action").getAsString();
  if(action.equals("customization_prepare")){
   if(saved==null){player.closeContainer();saved=new ArrayList<>();for(int i=0;i<player.getInventory().getContainerSize();i++)saved.add(player.getInventory().getItem(i).copy());selected=player.getInventory().getSelectedSlot();}
   GunItem.cancelReloadForImmediateAction(player,player.getMainHandItem());player.closeContainer();player.getInventory().clearContent();player.getInventory().setSelectedSlot(0);
   var id=Identifier.parse(request.get("gun").getAsString());Item item=BuiltInRegistries.ITEM.getValue(id);if(!(item instanceof GunItem))throw new IllegalArgumentException("Not a registered gun");
   ItemStack gun=new ItemStack(item);gun.set(ModDataComponents.GUN_AMMO.get(),0);player.getInventory().setItem(0,gun);
   if(request.has("attachment")){
    Item attachment=BuiltInRegistries.ITEM.getValue(Identifier.parse(request.get("attachment").getAsString()));ItemStack input=new ItemStack(attachment,request.has("count")?request.get("count").getAsInt():1);
    if(attachment instanceof MagazineItem&&request.has("ammo")){input.set(ModDataComponents.MAGAZINE_AMMO_ITEM.get(),request.get("ammo").getAsString());input.set(ModDataComponents.MAGAZINE_AMMO_COUNT.get(),request.get("rounds").getAsInt());}
    player.getInventory().setItem(9,input);
    if(request.has("legacy_installed")&&request.get("legacy_installed").getAsBoolean()){
     AttachmentType type=AttachmentType.valueOf(request.get("type").getAsString());GunAttachments.updateStoredStack(gun,type,input);player.getInventory().setItem(9,ItemStack.EMPTY);
    }
   }
   player.inventoryMenu.broadcastChanges();result.addProperty("setup","Temporary synthetic gun/attachment prepared; normal client interaction still required");
  }else if(action.equals("customization_restore")){
   if(saved==null)throw new IllegalStateException("No synthetic baseline saved");
   GunItem.cancelReloadForImmediateAction(player,player.getMainHandItem());player.closeContainer();for(int i=0;i<saved.size();i++)player.getInventory().setItem(i,saved.get(i).copy());player.getInventory().setSelectedSlot(selected);player.inventoryMenu.broadcastChanges();saved=null;result.addProperty("original_inventory_restored",true);
  }else if(!action.equals("customization_state"))throw new IllegalArgumentException(action);
  ItemStack gun=player.getMainHandItem();result.addProperty("held_item",BuiltInRegistries.ITEM.getKey(gun.getItem()).toString());result.addProperty("draw_locked",GunItem.isDrawOperationLocked(gun));result.addProperty("reloading",GunItem.isReloading(gun));result.addProperty("ammo",gun.getOrDefault(ModDataComponents.GUN_AMMO.get(),0));
  if(gun.getItem() instanceof GunItem item)result.addProperty("capacity",item.magazineSize(gun));
  JsonObject installed=new JsonObject();for(AttachmentType type:AttachmentType.values())installed.addProperty(type.key(),GunAttachments.id(gun,type).map(Identifier::toString).orElse(""));result.add("installed",installed);
  JsonArray inventory=new JsonArray();for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack item=player.getInventory().getItem(i);JsonObject row=new JsonObject();row.addProperty("slot",i);row.addProperty("id",BuiltInRegistries.ITEM.getKey(item.getItem()).toString());row.addProperty("count",item.getCount());row.addProperty("mag_rounds",item.getOrDefault(ModDataComponents.MAGAZINE_AMMO_COUNT.get(),0));inventory.add(row);}result.add("customization_inventory",inventory);
  if(player.containerMenu instanceof ttv.migami.jeg.menu.AttachmentMenu menu){JsonArray slots=new JsonArray();for(int i=0;i<AttachmentType.values().length;i++){var slot=menu.getSlot(i);JsonObject row=new JsonObject();row.addProperty("index",i);row.addProperty("type",menu.typeAt(i).key());row.addProperty("active",slot.isActive());row.addProperty("id",BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString());slots.add(row);}result.add("attachment_menu_slots",slots);}
 }
}
