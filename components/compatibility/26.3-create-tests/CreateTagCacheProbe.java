package valen.createprobe;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.impl.contraption.dispenser.DispenserBehaviorConverter;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
public final class CreateTagCacheProbe {
 private static final RegistryHolder HOLDER=new RegistryHolder();
 private static int stage;
 private static final class RegistryHolder {
  final SimpleRegistry<Item,String> single=SimpleRegistry.create();
  final SimpleRegistry.Multi<Item,String> multi=SimpleRegistry.Multi.create();
  final SimpleRegistry<Item,MountedDispenseBehavior> dispenser=SimpleRegistry.create();
  RegistryHolder(){
   var tag=TagKey.create(Registries.ITEM,Identifier.parse("valen_create_probe:cache_probe"));
   single.registerProvider(SimpleRegistry.Provider.forItemTag(tag,"present"));
   multi.addProvider(SimpleRegistry.Provider.forItemTag(tag,"present"));
   dispenser.registerProvider(DispenserBehaviorConverter.INSTANCE);
  }
 }
 public static Map<String,Object> run(MinecraftServer server){
  boolean flipped=stage==1;
  Item positive=flipped?Items.STONE:Items.DIRT,negative=flipped?Items.DIRT:Items.STONE;
  boolean one="present".equals(HOLDER.single.get(positive))&&HOLDER.single.get(negative)==null;
  boolean many=HOLDER.multi.get(positive).equals(List.of("present"))&&HOLDER.multi.get(negative).isEmpty();
  boolean arrow=(HOLDER.dispenser.get(Items.ARROW,server.overworld())!=null)==!flipped;
  if(!one||!many||!arrow)throw new AssertionError("tag stage="+stage+" single="+one+" multi="+many+" dispenser="+arrow);
  return Map.of("test","actual tag reload invalidates positive and negative caches stage"+stage++,"pass",true,"single",one,"multi",many,"dispenser_blacklist",arrow);
 }
}
