package valen.createprobe;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.google.gson.GsonBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.resources.*;
import net.minecraft.util.Util;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.storage.*;
import net.minecraft.nbt.*;
public final class SavedCreateReadback {
 static final class Worker extends IOWorker {Worker(RegionStorageInfo i,Path p){super(i,p,false);}}
 public static void main(String[] args)throws Exception {
  SharedConstants.tryDetectVersion();
  Identifier id=Identifier.parse("minecraft:overworld");
  ResourceKey<Level> key=ResourceKey.create(ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dimension")),id);
  Map<String,CompoundTag> entities=new TreeMap<>();
  try(var worker=new Worker(new RegionStorageInfo("create-probe",key,"chunk"),Path.of(args[0],"dimensions","minecraft","overworld","region"))){
   for(ChunkPos pos:List.of(new ChunkPos(8,7),new ChunkPos(8,8))){
    var tag=worker.loadAsync(pos).get(10,TimeUnit.SECONDS).orElseThrow();
    for(var value:tag.getListOrEmpty("block_entities")){
     var be=(CompoundTag)value;
     entities.put(be.getIntOr("x",0)+","+be.getIntOr("y",0)+","+be.getIntOr("z",0),be);
    }
   }
  }finally{Util.shutdownExecutors();}
  List<Object> rows=new ArrayList<>();
  for(String pos:List.of("131,222,120","128,222,128","131,222,128")){
   var be=Objects.requireNonNull(entities.get(pos),pos);
   var fluid=be.getCompoundOrEmpty("Fluid");var components=fluid.getCompoundOrEmpty("components");
   if(fluid.getLongOr("amount",-1)!=40500)throw new AssertionError(pos+" amount "+fluid);
   if(!(components.get("create:fluid_max_capacity") instanceof IntTag))throw new AssertionError(pos+" capacity type "+components);
   if(pos.endsWith(",128")&&!components.getCompoundOrEmpty("minecraft:potion_contents").getStringOr("potion","").equals("minecraft:awkward"))throw new AssertionError(pos+" potion "+components);
   rows.add(Map.of("position",pos,"saved_nbt",be.toString(),"pass",true));
  }
  var vault=Objects.requireNonNull(entities.get("140,222,120"));int iron=0;
  for(var value:vault.getListOrEmpty("Inventory")){var item=(CompoundTag)value;if(item.getStringOr("id","").equals("minecraft:iron_ingot"))iron+=item.getIntOr("count",0);}
  if(iron!=7)throw new AssertionError("vault="+vault);
  rows.add(Map.of("position","140,222,120","saved_nbt",vault.toString(),"iron_ingots",iron,"pass",true));
  Files.writeString(Path.of(args[1]),new GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
  System.out.println("PASS native saved Create tanks/potion/vault readback: "+rows.size());
 }
}
