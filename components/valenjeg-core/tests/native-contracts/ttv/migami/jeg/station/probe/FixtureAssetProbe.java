package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import ttv.migami.jeg.item.AttachmentItem;
import ttv.migami.jeg.station.StationMenu;
/** Read-only actual selected-pack/baked-model/atlas diagnostic, guarded by the existing fixture action inbox. */
final class FixtureAssetProbe {
 static void run(Minecraft client,JsonObject result)throws Exception {
  JsonObject probe=new JsonObject();JsonArray resources=new JsonArray();probe.add("resources",resources);
  List<Identifier> sourceResources=new ArrayList<>();
  for(String path:List.of("items/drum_mag.json","models/item/drum_mag.json","textures/item/magazines.png","models/block/scrap_workbench.json","models/block/gunmetal_workbench.json","models/block/gunnite_workbench.json","blockstates/scrap_workbench.json","blockstates/gunmetal_workbench.json","blockstates/gunnite_workbench.json")) {
   sourceResources.add(Identifier.fromNamespaceAndPath("jeg",path));
  }
  for(var id:ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.keySet())sourceResources.add(Identifier.fromNamespaceAndPath("mteg","items/"+id.getPath()+".json"));
  for(Identifier id:sourceResources){JsonObject row=new JsonObject();row.addProperty("resource",id.toString());JsonArray stack=new JsonArray();
   for(var resource:client.getResourceManager().getResourceStack(id)) {
    JsonObject entry=new JsonObject();entry.addProperty("pack",resource.sourcePackId());try(var stream=resource.open()){byte[] bytes=stream.readAllBytes();entry.addProperty("sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));entry.addProperty("bytes",bytes.length);}stack.add(entry);
   }
   row.add("stack",stack);var selected=client.getResourceManager().getResource(id);if(selected.isPresent()){row.addProperty("selected_pack",selected.get().sourcePackId());try(var stream=selected.get().open()){row.addProperty("selected_sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}resources.add(row);
  }
  JsonArray items=new JsonArray();probe.add("items",items);
  Object missing=client.getModelManager().getItemModel(Identifier.parse("jeg:__fixture_missing__"));
  List<String> itemIds=new ArrayList<>(List.of("jeg:drum_mag","jeg:combat_scope","jeg:scrap_workbench","jeg:gunmetal_workbench","jeg:gunnite_workbench","mteg:mo_workbench","mteg:trumpet","mteg:taki","mteg:flare","jeg:valen_coal_generator"));
  ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.keySet().stream().map(Identifier::toString).sorted().forEach(itemIds::add);
  for(String item:itemIds) {
   JsonObject row=new JsonObject();row.addProperty("item",item);
   try {
    ItemStack stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));var modelId=stack.get(DataComponents.ITEM_MODEL);row.addProperty("model_component",String.valueOf(modelId));
    var baked=client.getModelManager().getItemModel(modelId);row.addProperty("baked_class",baked.getClass().getName());row.addProperty("same_as_missing_model",baked==missing);
    ItemStackRenderState render=new ItemStackRenderState();client.getItemModelResolver().updateForLiving(render,stack,ItemDisplayContext.GUI,client.player);row.addProperty("render_empty",render.isEmpty());row.addProperty("bounds",String.valueOf(render.getModelBoundingBox()));
    var material=render.pickParticleMaterial(RandomSource.create(1));if(material!=null){row.addProperty("particle_atlas",material.sprite().atlasLocation().toString());row.addProperty("particle_sprite",material.sprite().contents().name().toString());}
   }catch(Throwable error){row.addProperty("error",error.toString());}items.add(row);
  }
  JsonArray sprites=new JsonArray();probe.add("atlas_entries",sprites);
  for(String sprite:List.of("item/magazines","item/revolver","block/station_parts/revolver"))for(String atlas:List.of("blocks","items")) {
   JsonObject row=new JsonObject();row.addProperty("sprite","jeg:"+sprite);row.addProperty("requested_atlas",atlas);
   try{var actual=client.getAtlasManager().get(new SpriteId(Identifier.parse("minecraft:textures/atlas/"+atlas+".png"),Identifier.parse("jeg:"+sprite)));row.addProperty("resolved_sprite",actual.contents().name().toString());row.addProperty("resolved_atlas",actual.atlasLocation().toString());}catch(Throwable error){row.addProperty("error",error.toString());}sprites.add(row);
  }
  if(client.player.containerMenu instanceof StationMenu menu)menu.recipes.stream().map(recipe->recipe.output()).filter(stack->stack.getItem() instanceof AttachmentItem).findFirst().ifPresent(stack->{probe.addProperty("current_menu_first_attachment",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());probe.addProperty("current_menu_attachment_model",String.valueOf(stack.get(DataComponents.ITEM_MODEL)));});
  probe.addProperty("proof","Actual selected resources and native baked runtime objects; this action changes no screen, inventory, world, pack or graphics setting");result.add("asset_probe",probe);Files.writeString(client.gameDirectory.toPath().resolve("valen-assets-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(probe));
 }
}
