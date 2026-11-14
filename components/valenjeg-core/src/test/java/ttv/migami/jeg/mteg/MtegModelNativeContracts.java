package ttv.migami.jeg.mteg;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.client.renderer.item.*;
/** Native model/definition decode, exact used part graph and atlas ownership; real bake/GPU is separate. */
public final class MtegModelNativeContracts {
 private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  Path root=Path.of("src/main/resources/assets/mteg");
  JsonObject descriptors=JsonParser.parseString(Files.readString(root.resolve("valen/item-model-descriptors.json"))).getAsJsonObject();
  int matrices=verifySingleSourceCenter(descriptors);
  check(descriptors.size()==35,"Original35 render programs");Set<String> models=new TreeSet<>();
  for(var e:descriptors.entrySet())for(var part:e.getValue().getAsJsonObject().getAsJsonArray("models"))models.add(part.getAsString().split(":")[1]);
  models.add("block/mo_workbench");models.add("item/trumpet");int elements=0,faces=0;
  for(String id:models){
   Path file=root.resolve("models/"+id+".json");JsonObject json=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
   CuboidModel model;try(var reader=Files.newBufferedReader(file)){model=CuboidModel.fromStream(reader);}
   check(model.geometry() instanceof UnbakedCuboidGeometry,"No native geometry "+id);
   UnbakedCuboidGeometry geometry=(UnbakedCuboidGeometry)model.geometry();elements+=geometry.elements().size();
   var textures=json.getAsJsonObject("textures");
   for(var element:geometry.elements())for(var face:element.faces().values()){
    String ref=face.texture();Set<String> seen=new HashSet<>();
    while(ref.startsWith("#")){check(seen.add(ref)&&textures.has(ref.substring(1)),"Unassigned face "+id+":"+ref);ref=textures.get(ref.substring(1)).getAsString();}
    check(!id.startsWith("block/")||!ref.startsWith("mteg:")||ref.startsWith("mteg:block/"),"Native block cross-atlas "+id+":"+ref);
    check(id.startsWith("block/")||!ref.startsWith("mteg:")||ref.startsWith("mteg:item/"),"Native item cross-atlas "+id+":"+ref);
    if(ref.startsWith("mteg:"))check(Files.exists(root.resolve("textures/"+ref.substring(5)+".png")),"Missing original PNG "+ref);faces++;
   }
  }
  for(String name:List.of("akm","akm_custom","awp","blue_heat","hellfire","m16a1","mp5","uzi","wrapped_rifle")){
   String id=List.of("m16a1","mp5","uzi").contains(name)?"special/gun/"+name:"special/"+name+"/main";
   JsonObject json=JsonParser.parseString(Files.readString(root.resolve("models/"+id+".json"))).getAsJsonObject();
   check(json.getAsJsonObject("textures").get("particle").getAsString().equals("mteg:item/"+name),"Actual primary particle "+id);
   check(Files.exists(root.resolve("textures/item/"+name+".png")),"Authored primary sprite "+name);
  }
  for(String color:List.of("green","blue","red","yellow"))for(String suffix:List.of(".png",".png.mcmeta","_e.png","_e.png.mcmeta")){
   Path original=root.resolve("textures/xmas/"+color+"_light"+suffix),alias=root.resolve("textures/item/xmas/"+color+"_light"+suffix);
   check(Files.mismatch(original,alias)==-1,"Byte-identical original animated/emissive item alias "+alias);
  }
  ItemModels.bootstrap();int definitions=0;
  try(var files=Files.list(root.resolve("items"))){for(Path file:files.toList()){var json=JsonParser.parseString(Files.readString(file));ClientItem.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();definitions++;}}
  check(definitions==39,"Native35guns+2ammo+trumpet+Mo definitions");
  System.out.println("MTEG_MODEL_NATIVE_PASS: "+models.size()+" used models/"+elements+" cuboids/"+faces+" face refs/39 native definitions; actual bake/GPU pending; "+matrices+" native nested item matrices match original single-center path");
 }
 private static Vector3f vector(JsonObject value,String key,float fallback){
  if(!value.has(key))return new Vector3f(fallback);
  JsonArray a=value.getAsJsonArray(key);return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());
 }
 /** Independent original affine sequence versus actual26.3 native ItemTransform calls.
  * Covers both hands, all contexts, nonidentity parent roots and moving part matrices. */
 private static int verifySingleSourceCenter(JsonObject descriptors){
  int count=0;
  for(var entry:descriptors.entrySet())for(var context:ItemDisplayContext.values()){
   JsonObject displays=entry.getValue().getAsJsonObject().getAsJsonObject("display");
   String key=context.getSerializedName();if(!displays.has(key)&&context.leftHand())key=key.replace("lefthand","righthand");
   JsonObject display=displays.has(key)?displays.getAsJsonObject(key):new JsonObject();
   Vector3f rotation=vector(display,"rotation",0),translation=vector(display,"translation",0).div(16F),scale=vector(display,"scale",1);
   ItemTransform nativeDisplay=displays.has(key)?new ItemTransform(rotation,translation,scale):ItemTransform.NO_TRANSFORM;
   for(Matrix4f root:List.of(new Matrix4f(),new Matrix4f().translation(.12F,-.08F,.16F).rotateXYZ(.14F,-.21F,.08F)))
    for(Matrix4f part:List.of(new Matrix4f(),new Matrix4f().translation(0,0,.125F).rotateZ(-.22F))){
     PoseStack.Pose actual=new PoseStack.Pose();nativeDisplay.apply(context.leftHand(),actual);
     actual.mulPose(MtegItemModels.sourceRootTransform(root));actual.mulPose(part);ItemTransform.NO_TRANSFORM.apply(false,actual);
     float hand=context.leftHand()?-1F:1F;
     Matrix4f expected=new Matrix4f().translation(translation.x*hand,translation.y,translation.z)
      .rotateXYZ((float)Math.toRadians(rotation.x),(float)Math.toRadians(rotation.y*hand),(float)Math.toRadians(rotation.z*hand))
      .scale(scale).mul(root).mul(part).translate(-.5F,-.5F,-.5F);
     float[] a=actual.pose().get(new float[16]),b=expected.get(new float[16]);
     for(int n=0;n<16;n++)check(Math.abs(a[n]-b[n])<.00001F,"Duplicate native center or wrong matrix order "+entry.getKey()+"/"+context+" element"+n+": "+a[n]+" vs "+b[n]);
     count++;
    }
  }
  check(count>1000,"Expected all35/context/hand/root/animation cases");return count;
 }

}
