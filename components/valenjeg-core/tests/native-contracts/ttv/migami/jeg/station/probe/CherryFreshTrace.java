package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
/** One fresh cherry site. Memory only until the probe flushes. Does not call processors or alter setBlock. */
public final class CherryFreshTrace {
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final long SEED=-4056840922791553232L;
 private static final BlockPos OLD=new BlockPos(450623,120,782500);
 private static final int TARGETS=64,EVENTS=4096;
 private static final List<JsonObject> events=new ArrayList<>();
 private static final Map<Long,JsonObject> targets=new LinkedHashMap<>();
 private static volatile boolean armed;
 private static volatile Map<Long,JsonObject> watched=Map.of();
 private static final java.util.concurrent.atomic.AtomicLong sequence=new java.util.concurrent.atomic.AtomicLong();
 private static final java.util.concurrent.atomic.AtomicLong observerErrors=new java.util.concurrent.atomic.AtomicLong();
 private CherryFreshTrace(){}
 static boolean armed(){return armed;}
 static JsonObject arm(ServerLevel level,StructureStart start){
  JsonObject out=new JsonObject();out.addProperty("processors_invoked",false);out.addProperty("old_r32_cell_registered",false);
  try{
   if((!Boolean.getBoolean("valen.structure.probe.cherryFreshTrace")&&!Boolean.getBoolean("valen.structure.probe.cherryInner1Trace"))||level.getSeed()!=SEED||!level.getServer().getServerDirectory().toRealPath().equals(FIXTURE.toRealPath()))
    throw new IllegalStateException("Fresh trace requires the copied fixture, its seed, and the opt-in");
   if(armed){out.addProperty("armed",false);out.addProperty("block_full",true);out.addProperty("reason","single_fresh_site_already_armed");return out;}
   if(start.getBoundingBox().isInside(OLD)){out.addProperty("armed",false);out.addProperty("block_full",true);out.addProperty("reason","reserved_r32_box");return out;}
   List<JsonObject> floor=new ArrayList<>(),any=new ArrayList<>();JsonArray floorCreate=new JsonArray();
   for(var piece:start.getPieces()){
    if(!(piece instanceof PoolElementStructurePiece pool)||!(pool.getElement() instanceof SinglePoolElement single))continue;
    String id=single.getTemplateLocation().toString();if(!id.contains("/inner"))continue;
    CompoundTag root=root(level,id);int maxX=root.getListOrEmpty("size").getIntOr(0,0)-1;
    Map<Integer,BlockState> palette=palette(root);Set<String> seenFloor=new LinkedHashSet<>();
    for(Tag raw:root.getListOrEmpty("blocks")){
     CompoundTag tag=(CompoundTag)raw;ListTag pos=tag.getListOrEmpty("pos");
     int lx=pos.getIntOr(0,0),ly=pos.getIntOr(1,0),lz=pos.getIntOr(2,0);
     BlockState source=palette.get(tag.getIntOr("state",-1));if(source==null||source.is(Blocks.STRUCTURE_VOID))continue;
     String blockId=BuiltInRegistries.BLOCK.getKey(source.getBlock()).toString();
     if(ly==0&&blockId.startsWith("create:")&&seenFloor.add(blockId))floorCreate.add(blockId);
     if(!blockId.equals("create:industrial_iron_block"))continue;
     BlockState rotated=source.rotate(pool.getRotation());
     BlockPos world=StructureTemplate.transform(new BlockPos(lx,ly,lz),Mirror.NONE,pool.getRotation(),BlockPos.ZERO).offset(pool.getPosition());
     if(world.equals(OLD))continue;
     JsonObject row=new JsonObject();row.add("world",xyz(world));row.add("local",xyz(lx,ly,lz));row.addProperty("template",id);
     row.addProperty("local_y",ly);row.addProperty("x_boundary",lx==0||lx==maxX);row.addProperty("source_state",rotated.toString());
     row.add("overlap",overlap(level,start,world,id));row.addProperty("chunk_dx",(world.getX()>>4)-start.getChunkPos().x());row.addProperty("chunk_dz",(world.getZ()>>4)-start.getChunkPos().z());
     if(ly==0)floor.add(row);else any.add(row);
    }
   }
   List<JsonObject> found=floor.isEmpty()?any:floor;
   found.sort(Comparator.comparing((JsonObject row)->!row.get("x_boundary").getAsBoolean()).thenComparingInt(row->row.getAsJsonArray("world").get(0).getAsInt()).thenComparingInt(row->row.getAsJsonArray("world").get(2).getAsInt()));
   out.add("floor_create_blocks",floorCreate);out.addProperty("selection",floor.isEmpty()?any.isEmpty()?"none":"all_inner_industrial_iron":"inner_floor_industrial_iron");
   if(found.size()>TARGETS)found=new ArrayList<>(found.subList(0,TARGETS));
   synchronized(CherryFreshTrace.class){
    targets.clear();events.clear();
    for(JsonObject row:found){JsonArray world=row.getAsJsonArray("world");targets.put(BlockPos.asLong(world.get(0).getAsInt(),world.get(1).getAsInt(),world.get(2).getAsInt()),row);}
    watched=Map.copyOf(targets);sequence.set(0);observerErrors.set(0);armed=!targets.isEmpty();
   }
   out.addProperty("armed",armed);out.addProperty("block_full",false);out.addProperty("coverage",armed?"registered_cells":"unavailable");out.addProperty("target_cap",TARGETS);out.addProperty("event_cap",EVENTS);out.addProperty("target_count",found.size());out.add("targets",jsonArray(found));
   if(!armed)out.addProperty("reason","no_source_industrial_iron_in_selected_inner");
  }catch(Throwable error){out.addProperty("armed",false);out.addProperty("block_full",true);out.addProperty("coverage","unavailable");out.addProperty("error",error.toString());}
  return out;
 }
 public static boolean around(WorldGenRegion region,BlockPos pos,BlockState state,int flags,int extra,Operation<Boolean> original){
  if(!armed||!watched.containsKey(pos.asLong()))return original.call(pos,state,flags,extra);
  long invocation=sequence.incrementAndGet(),start=System.nanoTime();boolean readable=false;String before="not_observed";
  try{readable=region.isWithinWriteZone(pos);if(readable)before=region.getBlockState(pos).toString();}catch(Throwable ignored){observerErrors.incrementAndGet();}
  // The original operation executes exactly once, outside observer exception handling.
  boolean result=original.call(pos,state,flags,extra);long ended=System.nanoTime();
  try{
   String after=readable&&region.isWithinWriteZone(pos)?region.getBlockState(pos).toString():"not_observed";
   var center=region.getCenter();
   var centerChunk=region.getChunk(center.x(),center.z(),net.minecraft.world.level.chunk.status.ChunkStatus.EMPTY,false);
   var targetChunk=readable?region.getChunk(pos.getX()>>4,pos.getZ()>>4,net.minecraft.world.level.chunk.status.ChunkStatus.EMPTY,false):null;
   JsonObject event=new JsonObject();event.add("world",xyz(pos));event.addProperty("requested",state.toString());event.addProperty("result",result);
   event.addProperty("before",before);event.addProperty("after",after);event.addProperty("flags",flags);event.addProperty("extra",extra);
   event.addProperty("center_chunk_x",center.x());event.addProperty("center_chunk_z",center.z());event.addProperty("target_chunk_x",pos.getX()>>4);event.addProperty("target_chunk_z",pos.getZ()>>4);
   event.addProperty("center_status",centerChunk==null?"not_loaded":centerChunk.getPersistedStatus().toString());event.addProperty("target_status",targetChunk==null?"not_read":targetChunk.getPersistedStatus().toString());
   event.addProperty("within_write_zone",readable);event.addProperty("thread",Thread.currentThread().getName());event.addProperty("caller",caller());
   event.addProperty("invocation_sequence",invocation);event.addProperty("start_nano",start);event.addProperty("original_return_nano",ended);
   synchronized(CherryFreshTrace.class){if(events.size()<EVENTS)events.add(event);}
  }catch(Throwable ignored){observerErrors.incrementAndGet();}
  return result;
 }
 static JsonObject finish(ServerLevel level){
  synchronized(CherryFreshTrace.class){
   armed=false;watched=Map.of();events.sort(Comparator.comparingLong(row->row.get("invocation_sequence").getAsLong()));
   JsonObject out=new JsonObject();out.addProperty("event_count",events.size());out.addProperty("event_cap",EVENTS);out.addProperty("truncated",events.size()>=EVENTS);out.addProperty("observer_errors",observerErrors.get());
   out.addProperty("coverage",targets.isEmpty()?"unavailable":"registered_cells");
   out.addProperty("observation_scope","Registered cells during this site's bounded FULL phase. Empty capture is coverage unavailable, not evidence a block was never written");
   JsonArray rows=new JsonArray();
   for(var entry:targets.entrySet()){
    JsonObject row=entry.getValue().deepCopy();long key=entry.getKey();boolean saw=false,accepted=false,sequentialReplacement=false,overlap=false;long sourceEnded=Long.MAX_VALUE;
    JsonArray mine=new JsonArray();
    for(JsonObject event:events){
     JsonArray world=event.getAsJsonArray("world");if(BlockPos.asLong(world.get(0).getAsInt(),world.get(1).getAsInt(),world.get(2).getAsInt())!=key)continue;mine.add(event);
     boolean iron=event.get("requested").getAsString().contains("create:industrial_iron_block"),ok=event.get("result").getAsBoolean();
     if(iron){saw=true;if(ok){accepted=true;sourceEnded=Math.min(sourceEnded,event.get("original_return_nano").getAsLong());}}
     else if(accepted&&ok){if(event.get("start_nano").getAsLong()>=sourceEnded)sequentialReplacement=true;else overlap=true;}
    }
    BlockPos pos=BlockPos.of(key);var chunk=level.getChunkSource().getChunk(pos.getX()>>4,pos.getZ()>>4,net.minecraft.world.level.chunk.status.ChunkStatus.FULL,false);
    String actual=chunk==null?"not_already_FULL":chunk.getBlockState(pos).toString();boolean finalSource=actual.contains("create:industrial_iron_block");
    String label=mine.isEmpty()?"not_observed_during_trace":accepted&&finalSource?"source_write_present_at_final_readback":accepted&&sequentialReplacement?"observed_sequential_post_source_replacement":accepted&&overlap?"concurrent_write_order_unresolved":accepted?"different_final_state_without_observed_replacement":saw?"source_write_rejected":"industrial_iron_request_not_captured";
    row.add("writes",mine);row.addProperty("final_native_state",actual);row.addProperty("classification",label);rows.add(row);
   }
   out.add("cells",rows);targets.clear();events.clear();return out;
  }
 }
 private static JsonArray overlap(ServerLevel level,StructureStart start,BlockPos world,String inner)throws Exception{
  JsonArray rows=new JsonArray();
  for(var piece:start.getPieces()){
   if(!(piece instanceof PoolElementStructurePiece pool)||!(pool.getElement() instanceof SinglePoolElement single))continue;
   String id=single.getTemplateLocation().toString();if(id.equals(inner))continue;
   CompoundTag root=root(level,id);Map<Integer,BlockState> palette=palette(root);
   for(Tag raw:root.getListOrEmpty("blocks")){
    CompoundTag tag=(CompoundTag)raw;ListTag pos=tag.getListOrEmpty("pos");
    BlockPos local=new BlockPos(pos.getIntOr(0,0),pos.getIntOr(1,0),pos.getIntOr(2,0));
    if(!StructureTemplate.transform(local,Mirror.NONE,pool.getRotation(),BlockPos.ZERO).offset(pool.getPosition()).equals(world))continue;
    BlockState source=palette.get(tag.getIntOr("state",-1));if(source==null)continue;
    JsonObject row=new JsonObject();row.addProperty("template",id);row.add("local",xyz(local));row.addProperty("source_state",source.rotate(pool.getRotation()).toString());rows.add(row);
   }
  }
  return rows;
 }
 private static String caller(){
  StringBuilder out=new StringBuilder();int kept=0;
  for(StackTraceElement frame:Thread.currentThread().getStackTrace()){
   String name=frame.getClassName();
   if(name.startsWith("java.lang.Thread")||name.startsWith("ttv.migami.jeg.station.probe")||name.contains("Mixin")||name.contains("Operation"))continue;
   if(kept==6)break;if(kept>0)out.append(" | ");out.append(name).append('.').append(frame.getMethodName()).append(':').append(frame.getLineNumber());kept++;
  }
  return out.toString();
 }
 private static Map<Integer,BlockState> palette(CompoundTag root){
  ListTag palette=root.getListOrEmpty("palette");Map<Integer,BlockState> states=new HashMap<>();
  for(int i=0;i<palette.size();i++){
   CompoundTag nbt=(CompoundTag)palette.get(i);BlockState state=BunkerWorldProbe.state(nbt);
   states.put(i,state);
  }
  return states;
 }
 private static <T extends Comparable<T>> BlockState property(BlockState state,Property<T> property,String value){return state.setValue(property,property.getValue(value).orElseThrow());}
 private static CompoundTag root(ServerLevel level,String templateId)throws Exception{
  Identifier template=Identifier.parse(templateId);Identifier resource=Identifier.fromNamespaceAndPath(template.getNamespace(),"structure/"+template.getPath()+".nbt");
  byte[] raw;try(var in=level.getServer().getResourceManager().getResource(resource).orElseThrow().open()){raw=in.readAllBytes();}
  try(var in=new ByteArrayInputStream(raw)){return NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
 }
 private static JsonArray jsonArray(List<JsonObject> rows){JsonArray array=new JsonArray();rows.forEach(array::add);return array;}
 private static JsonArray xyz(BlockPos pos){return xyz(pos.getX(),pos.getY(),pos.getZ());}
 private static JsonArray xyz(int x,int y,int z){JsonArray array=new JsonArray();array.add(x);array.add(y);array.add(z);return array;}
}
