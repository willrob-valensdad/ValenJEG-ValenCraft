package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.geckolib.renderer.base.GeoRenderState;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import ttv.migami.jeg.client.render.gun.AnimatedGunRenderer;
import ttv.migami.jeg.client.render.gun.GunAttachmentGeoModel;
import ttv.migami.jeg.item.attachment.*;
/** Read-only actual client texture-selection and native optic-bake evidence. */
public final class CustomizationClientProbe {
 private static final ConcurrentHashMap<String,JsonObject> observed=new ConcurrentHashMap<>();
 public static void texture(GeoRenderState state,Identifier texture){
  if(!Boolean.getBoolean("valen.customization.audit"))return;
  Minecraft client=Minecraft.getInstance();if(!client.gameDirectory.toPath().toAbsolutePath().normalize().toString().equals(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/client"))return;
  ItemStack stack=state.getOrDefaultGeckolibData(AnimatedGunRenderer.ITEM_STACK,ItemStack.EMPTY);if(stack.isEmpty())return;
  String gun=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();String paint=GunAttachments.id(stack,AttachmentType.PAINT_JOB).map(Identifier::toString).orElse("");
  JsonObject row=new JsonObject();row.addProperty("gun",gun);row.addProperty("paint_item",paint);row.addProperty("actual_selected_texture",texture.toString());row.addProperty("last_seen_nano",System.nanoTime());observed.put(gun+"|"+paint,row);
 }
 static void action(Minecraft client,JsonObject request,JsonObject result)throws Exception{
  if(!Boolean.getBoolean("valen.customization.audit"))throw new IllegalStateException("Customization profile not enabled");
  if(request.get("action").getAsString().equals("customization_open")){
   ttv.migami.jeg.network.ClientNetworkHandler.sendOpenAttachments();result.addProperty("action_result","Normal open-attachment payload sent");
  }
  JsonArray rows=new JsonArray();observed.values().forEach(v->rows.add(v.deepCopy()));result.add("actual_texture_selections",rows);
  if(client.player!=null){
   var stack=client.player.getMainHandItem();result.addProperty("held_item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());result.addProperty("magnified_optic",ttv.migami.jeg.gun.GunScopeSupport.hasMagnifiedOptic(stack));result.addProperty("ads_progress",ttv.migami.jeg.client.handler.AimingHandler.get().getRenderAdsProgress());
  }
  JsonArray models=new JsonArray();for(String optic:new String[]{"reflex_sight","monocle_sight","holographic_sight","telescopic_sight","combat_scope","spyglass"}){
   JsonObject row=new JsonObject();row.addProperty("optic",optic);
   try{var baked=new GunAttachmentGeoModel().getBakedModel(Identifier.parse("jeg:item/attachment/"+optic));row.addProperty("native_baked",baked!=null);row.addProperty("baked_class",baked.getClass().getName());row.addProperty("glow_bone",baked.getBone("glow").isPresent());}catch(Throwable error){row.addProperty("error",error.toString());}
   models.add(row);
  }
  result.add("native_optic_models",models);
  Files.writeString(client.gameDirectory.toPath().resolve("valen-customization-client-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
 }
}
