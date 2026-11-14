package ttv.migami.jeg.station;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.client.renderer.item.*;
/** Native JSON model decode only; no client/window/world is launched. */
public final class RestoredModelNativeContracts {
 public static void main(String[] args)throws Exception {
  Path assets=Path.of("src/main/resources/assets/jeg");int faces=0;
  for(var row:Map.of("block/scrap_workbench",52,"block/gunmetal_workbench",51,"block/gunnite_workbench",102,"item/drum_mag",9).entrySet()) {
   Path path=assets.resolve("models/"+row.getKey()+".json");CuboidModel model;
   try(var reader=Files.newBufferedReader(path)){model=CuboidModel.fromStream(reader);}
   if(!(model.geometry() instanceof UnbakedCuboidGeometry geometry)||geometry.elements().size()!=row.getValue())throw new AssertionError("Original native geometry lost: "+row.getKey());
   JsonObject json=JsonParser.parseString(Files.readString(path)).getAsJsonObject();JsonObject textures=json.getAsJsonObject("textures");
   for(var element:geometry.elements())for(var face:element.faces().values()) {
    String texture=face.texture();Set<String> seen=new HashSet<>();
    while(texture.startsWith("#")){String key=texture.substring(1);if(!seen.add(key)||!textures.has(key))throw new AssertionError("Unresolved native face "+texture);texture=textures.get(key).getAsString();}
    if(texture.startsWith("jeg:")&&!Files.exists(assets.resolve("textures/"+texture.substring(4)+".png")))throw new AssertionError("Missing authored PNG "+texture);
    if(row.getKey().startsWith("block/")&&texture.startsWith("jeg:")&&!texture.startsWith("jeg:block/"))throw new AssertionError("Actual26.3blockbake rejects cross-atlas sprite "+texture);faces++;
   }
   System.out.println("Native model "+row.getKey()+": "+geometry.elements().size()+" original elements");
  }
  ItemModels.bootstrap();
  JsonElement definition=JsonParser.parseString(Files.readString(assets.resolve("items/drum_mag.json")));
  ClientItem.CODEC.parse(JsonOps.INSTANCE,definition).result().orElseThrow(()->new AssertionError("Native drum itemdefinition rejected"));
  JsonObject language=JsonParser.parseString(Files.readString(assets.resolve("lang/en_us.json"))).getAsJsonObject();
  if(!language.get("item.jeg.atlantean_spear").getAsString().equals("Atlantean Spear"))throw new AssertionError("Original spear name missing");
  System.out.println("RESTORED_MODELS_NATIVE_PASS:4models/214elements/"+faces+"face refs/native drum definition/originalEnglishname; GPU proof pending");
 }
}
