package ttv.migami.jeg.tech.astra.oxygen.ui;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.tech.astra.oxygen.OxygenLoaderBlockEntity;
/** Authoritative native menu/BE identity, actual quantities and persisted inventory; no mutation. */
public final class OxygenUiServerReadback {
 public static void add(ServerPlayer player,JsonObject receipt){
  if(!(player.containerMenu instanceof OxygenLoaderMenu menu)||!(menu.inventory instanceof OxygenLoaderBlockEntity owner))return;
  var data=new JsonObject();data.addProperty("menu_id",menu.containerId);data.addProperty("native_owner_valid",menu.stillValid(player));data.addProperty("block_pos",owner.getBlockPos().toShortString());
  data.addProperty("energy",owner.process().energy());data.addProperty("water_droplets",owner.process().water());data.addProperty("input_oxygen_mb",owner.process().inputGas());data.addProperty("output_oxygen_mb",owner.process().oxygen());
  var sides=new JsonArray();for(int i=0;i<OxygenSideConfiguration.CHANNELS;i++)sides.add(owner.sideConfig().mask(i));data.add("side_masks",sides);
  var inventory=new JsonArray();for(int i=0;i<owner.getContainerSize();i++){var stack=owner.getItem(i);var row=new JsonObject();row.addProperty("slot",i);row.addProperty("item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());row.addProperty("count",stack.getCount());inventory.add(row);}data.add("persistent_inventory",inventory);receipt.add("oxygen_authoritative_menu",data);
 }
}
