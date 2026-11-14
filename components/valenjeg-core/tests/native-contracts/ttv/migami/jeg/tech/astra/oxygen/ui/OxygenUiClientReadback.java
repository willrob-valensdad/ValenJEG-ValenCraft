package ttv.migami.jeg.tech.astra.oxygen.ui;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
/** Fixture-only actual native menu properties and received REI display readback. */
public final class OxygenUiClientReadback {
 public static void add(Minecraft client,JsonObject receipt){
  if(client.player!=null&&client.player.containerMenu instanceof OxygenLoaderMenu menu){
   var data=new JsonObject();data.addProperty("energy",menu.data.get(0));data.addProperty("water_droplets",menu.waterDroplets());data.addProperty("input_oxygen_mb",menu.data.get(13));data.addProperty("output_oxygen_mb",menu.data.get(3));
   JsonArray sides=new JsonArray();for(int i=0;i<OxygenSideConfiguration.CHANNELS;i++)sides.add(menu.data.get(6+i));data.add("side_masks",sides);receipt.add("oxygen_client_menu",data);
  }
  if(client.player==null||client.getConnection()==null)return;
  var received=new JsonArray();var registry=DisplayRegistry.getInstance();
  for(var display:registry.get(OxygenRecipeDisplay.CATEGORY)){
   var data=new JsonObject();data.addProperty("recipe",display.recipeId);data.addProperty("energy_per_tick",display.energy);data.addProperty("ticks",display.ticks);data.addProperty("input_id",display.inputId);data.addProperty("input_mb",display.inputMb);data.addProperty("output_id",display.gasId);data.addProperty("output_mb",display.gasMb);
   var origin=registry.getDisplayOrigin(display);data.addProperty("origin_type",origin==null?"none":origin.getClass().getName());received.add(data);
  }
  receipt.add("actual_received_oxygen_rei_displays",received);
 }
}
