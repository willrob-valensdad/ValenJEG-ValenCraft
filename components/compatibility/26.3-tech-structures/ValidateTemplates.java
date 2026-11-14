import java.nio.file.*;
import java.util.zip.ZipFile;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.*;
public final class ValidateTemplates {
 public static void main(String[] args)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  int templates=0,states=0,properties=0;
  try(var zip=new ZipFile(args[0])){
   for(var e:zip.stream().filter(e->e.getName().endsWith(".nbt")).toList()){
    CompoundTag root;
    try(var stream=zip.getInputStream(e)){root=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());}
    if(root.getIntOr("DataVersion",0)!=5023)throw new AssertionError("Unexpected data version");
    for(var raw:root.getListOrEmpty("palette")){
     var state=(CompoundTag)raw;var id=Identifier.parse(state.getStringOr("id",""));
     if(!BuiltInRegistries.BLOCK.containsKey(id))throw new AssertionError("Missing block "+id);
     var block=BuiltInRegistries.BLOCK.getValue(id);
     for(var key:state.getCompoundOrEmpty("properties").keySet()){
      var prop=block.getStateDefinition().getProperty(key);
      var value=state.getCompoundOrEmpty("properties").getStringOr(key,"");
      if(prop==null||prop.getValue(value).isEmpty())throw new AssertionError("Invalid "+id+" "+key+"="+value);
      properties++;
     }
     states++;
    }
    templates++;
   }
  }
  System.out.println("VALIDATED templates="+templates+" states="+states+" properties="+properties);
 }
}
