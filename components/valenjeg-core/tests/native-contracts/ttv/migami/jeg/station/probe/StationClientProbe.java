package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.init.ModDataComponents;

/** Actual remote-server REI readback, with no recipe-local fallback or config mutation. */
public final class StationClientProbe implements ClientModInitializer {
    private boolean done;private int ticks,joinedTicks;
    @Override public void onInitializeClient() {
        if(!Boolean.getBoolean("valen.station.clientProbe"))return;
        LegacyWeaponClientActions.register();
        BunkerAccessClientActions.register();
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(++ticks%20!=0)return;
            StationClientActions.tick(client);
            if(done || client.player==null || client.getConnection()==null)return;
            joinedTicks+=20;
            JsonObject receipt=new JsonObject();JsonObject counts=new JsonObject();receipt.add("categories",counts);
            try {
                boolean ready=true;int schematicComponents=0;
                for(String station:List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycler","schematic_station")) {
                    var displays=DisplayRegistry.getInstance().get(CategoryIdentifier.of("jeg",station));
                    counts.addProperty(station,displays.size());if(displays.isEmpty())ready=false;
                    if(station.equals("schematic_station"))for(var display:displays)for(var ingredient:display.getOutputEntries())for(var entry:ingredient) {
                        if(entry.getValue() instanceof ItemStack stack && stack.get(ModDataComponents.BLUEPRINT_GUN.get())!=null)schematicComponents++;
                    }
                }
                String mode=ConfigManager.getInstance().getConfig().getForceLocalRecipes().name();
                receipt.addProperty("forceLocalRecipes",mode);
                receipt.addProperty("blueprint_component_outputs",schematicComponents);
                receipt.addProperty("connected_server",client.getConnection().getConnection().getRemoteAddress().toString());
                if(ready && schematicComponents>0) {
                    if(!mode.equals("NEVER"))throw new AssertionError("Expected forceLocalRecipes=NEVER, actual "+mode);
                    receipt.addProperty("status","pass");done=true;
                }else if(joinedTicks>=1200){receipt.addProperty("status","fail");receipt.addProperty("error","Server-synchronized categories/components not ready after1200ticks");done=true;}
                else receipt.addProperty("status","waiting");
            }catch(Throwable error) {
                receipt.addProperty("status","fail");receipt.addProperty("error",error.toString());error.printStackTrace();done=true;
            }
            try{Files.writeString(Path.of(System.getProperty("valen.station.clientProbe.output","valen-station-client-probe.json")),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));}
            catch(Exception error){error.printStackTrace();}
            if(done)System.out.println("VALEN_STATION_CLIENT_FIXTURE "+receipt);
        });
    }
}
