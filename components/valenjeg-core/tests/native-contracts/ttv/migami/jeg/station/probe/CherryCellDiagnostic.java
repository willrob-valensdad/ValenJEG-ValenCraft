package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
/** Read-only cherry cell record. Does not place, generate, ticket chunks, call processors, or unpack loot. */
final class CherryCellDiagnostic {
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final long SEED=-4056840922791553232L;
 private static final String STRUCTURE="jeg:valen_hangars/cherry_hangar";
 private static final BlockPos CELL=new BlockPos(450623,120,782500);
 private static final int START_X=28165,START_Z=48907;
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private CherryCellDiagnostic(){}
 static boolean consumeIfRequested(MinecraftServer server,Path inbox,Path game){
  try{
   if(!Files.exists(inbox))return false;
   JsonObject request=JsonParser.parseString(Files.readString(inbox)).getAsJsonObject();
   if(!request.has("action")||!request.get("action").getAsString().equals("cherry_diagnostic"))return false;
   Files.delete(inbox);write(server,request,game);return true;
  }catch(Exception error){return false;}
 }
 static void write(MinecraftServer server,JsonObject request,Path game){
  JsonObject out=new JsonObject();out.add("request",request);
  try{read(server,out);out.addProperty("status","read");}
  catch(Throwable error){out.addProperty("status","fail");out.addProperty("error",error.toString());}
  try{String text=GSON.toJson(out)+"\n";Files.writeString(game.resolve("valen-cherry-diagnostic.json"),text);Files.writeString(game.resolve("valen-station-server-state.json"),text);Files.writeString(game.resolve("valen-station-server-actions.jsonl"),out+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
  catch(Exception error){error.printStackTrace();}
 }
 private static void read(MinecraftServer server,JsonObject out)throws Exception{
  out.addProperty("read_only",true);out.addProperty("processors_invoked",false);out.addProperty("historical_writes_recovered",false);
  out.addProperty("assertion_unchanged",true);out.addProperty("world_modified",false);out.addProperty("chunks_ticketed_or_generated",false);
  ServerLevel level=server.overworld();
  if(!server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())||!Files.isRegularFile(FIXTURE.resolve("../preparation.json"))||server.getPort()!=25589||level.getSeed()!=SEED||!level.dimension().identifier().toString().equals("minecraft:overworld"))
   throw new IllegalStateException("cherry_diagnostic requires the copied fixture, port 25589, overworld, and seed "+SEED);
  RegistryAccess access=level.registryAccess();
  Identifier iron=Identifier.parse("create:industrial_iron_block");if(!BuiltInRegistries.BLOCK.containsKey(iron))throw new IllegalStateException("Expected Create id absent; fallback prohibited");
  BlockState registry=BuiltInRegistries.BLOCK.getValue(iron).defaultBlockState();
  JsonObject block=new JsonObject();block.addProperty("id",BuiltInRegistries.BLOCK.getKey(registry.getBlock()).toString());
  block.addProperty("registry_class",registry.getBlock().getClass().getName());block.addProperty("default_state",registry.toString());
  block.addProperty("is_air",registry.isAir());block.addProperty("can_be_replaced",registry.canBeReplaced());block.addProperty("in_minecraft_air_tag",registry.is(BlockTags.AIR));
  out.add("create_industrial_iron_block",block);
  Structure structure=access.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.parse(STRUCTURE))).value();
  ChunkAccess startChunk=level.getChunkSource().getChunk(START_X,START_Z,ChunkStatus.FULL,false);
  ChunkAccess cellChunk=level.getChunkSource().getChunk(CELL.getX()>>4,CELL.getZ()>>4,ChunkStatus.FULL,false);
  out.addProperty("start_chunk_visible",startChunk!=null);out.addProperty("cell_chunk_visible",cellChunk!=null);
  if(startChunk==null){out.addProperty("limitation","start chunk 28165,48907 is not already visible; this action does not ticket or generate it, and a FULL chunk does not retain historical setBlock calls");return;}
  out.addProperty("start_chunk_status",startChunk.getPersistedStatus().toString());
  if(cellChunk!=null)out.addProperty("cell_chunk_status",cellChunk.getPersistedStatus().toString());
  StructureStart start=startChunk.getStartForStructure(structure);
  if(start==null||!start.isValid())throw new IllegalStateException("Visible start chunk has no valid cherry_hangar start");
  if(!start.getBoundingBox().isInside(CELL))throw new IllegalStateException("Focused cell is outside the saved cherry start box");
  JsonArray pieces=new JsonArray(),floor=new JsonArray(),column=new JsonArray();
  int order=0;
  for(var actual:start.getPieces()){
   JsonObject row=new JsonObject();row.addProperty("order",order++);pieces.add(row);
   if(!(actual instanceof PoolElementStructurePiece piece)||!(piece.getElement() instanceof SinglePoolElement single)){row.addProperty("kind","not_single_pool");continue;}
   String templateId=single.getTemplateLocation().toString();row.addProperty("template",templateId);row.addProperty("rotation",piece.getRotation().toString());
   row.add("origin",xyz(piece.getPosition()));row.addProperty("inside_piece_box",piece.getBoundingBox().isInside(CELL));
   row.addProperty("projection",piece.getElement().getProjection().getSerializedName());
   JsonArray projectionProcessors=new JsonArray();
   for(var processor:piece.getElement().getProjection().getProcessors())projectionProcessors.add(processor.getClass().getName());
   row.add("projection_processor_classes",projectionProcessors);
   var encoded=StructurePoolElement.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,access),piece.getElement()).getOrThrow().getAsJsonObject();
   String processorId=encoded.get("processors").getAsString();row.addProperty("processor_list",processorId);
   if(encoded.has("liquid_settings"))row.addProperty("liquid_settings",encoded.get("liquid_settings").getAsString());
   StructureProcessorList processors=access.lookupOrThrow(Registries.PROCESSOR_LIST).getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST,Identifier.parse(processorId))).value();
   JsonArray classes=new JsonArray();for(var processor:processors.list())classes.add(processor.getClass().getName());
   row.add("processor_classes",classes);row.addProperty("processor_processBlock_called",false);
   JsonArray engine=new JsonArray();engine.add("net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor");engine.add("net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor");
   row.add("engine_getSettings_processors_not_invoked",engine);
   Map<Integer,BlockState> palette=palette(server,templateId);
   for(Tag raw:root(server,templateId).getListOrEmpty("blocks")){
    CompoundTag tag=(CompoundTag)raw;ListTag pos=tag.getListOrEmpty("pos");
    BlockPos local=new BlockPos(pos.getIntOr(0,0),pos.getIntOr(1,0),pos.getIntOr(2,0));
    BlockState source=palette.get(tag.getIntOr("state",-1));if(source==null||source.is(Blocks.STRUCTURE_VOID))continue;
    BlockPos world=StructureTemplate.transform(local,Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());
    if(world.getX()!=CELL.getX()||(world.getY()!=CELL.getY()&&world.getZ()!=CELL.getZ()))continue;
    JsonObject cell=cell(local,source.rotate(piece.getRotation()),world,cellChunk);
    cell.addProperty("template",templateId);cell.addProperty("piece_order",order-1);
    if(world.equals(CELL)){row.add("focus_source",cell);out.add("focus_saved",cell);}
    if(world.getY()==CELL.getY())floor.add(cell);
    if(world.getZ()==CELL.getZ())column.add(cell);
   }
  }
  out.add("pieces",pieces);out.add("same_x_floor",floor);out.add("same_x_column",column);
  out.addProperty("limitation",cellChunk==null?"cell chunk is not already visible; saved states were omitted and this action did not ticket or generate it":"saved states are the current FULL chunk only; historical setBlock order was not recovered");
 }
 private static JsonObject cell(BlockPos local,BlockState state,BlockPos world,ChunkAccess chunk){
  JsonObject row=new JsonObject();row.add("local",xyz(local));row.add("world",xyz(world));
  row.addProperty("source_state",state.toString());row.addProperty("source_id",BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
  row.addProperty("source_class",state.getBlock().getClass().getName());row.addProperty("is_air",state.isAir());
  row.addProperty("can_be_replaced",state.canBeReplaced());row.addProperty("in_minecraft_air_tag",state.is(BlockTags.AIR));
  if(chunk instanceof LevelChunk levelChunk&&world.getX()>>4==levelChunk.getPos().x()&&world.getZ()>>4==levelChunk.getPos().z())row.addProperty("saved_state",levelChunk.getBlockState(world).toString());
  else row.addProperty("saved_state_omitted",true);
  return row;
 }
 private static Map<Integer,BlockState> palette(MinecraftServer server,String templateId)throws Exception{
  ListTag palette=root(server,templateId).getListOrEmpty("palette");Map<Integer,BlockState> states=new HashMap<>();
  for(int i=0;i<palette.size();i++)states.put(i,state((CompoundTag)palette.get(i)));
  return states;
 }
 private static CompoundTag root(MinecraftServer server,String templateId)throws Exception{
  Identifier template=Identifier.parse(templateId);
  Identifier resource=Identifier.fromNamespaceAndPath(template.getNamespace(),"structure/"+template.getPath()+".nbt");
  byte[] raw;try(var in=server.getResourceManager().getResource(resource).orElseThrow().open()){raw=in.readAllBytes();}
  try(var in=new ByteArrayInputStream(raw)){return NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
 }
 private static BlockState state(CompoundTag nbt){return BunkerWorldProbe.state(nbt);}
 private static JsonArray xyz(BlockPos pos){JsonArray array=new JsonArray();array.add(pos.getX());array.add(pos.getY());array.add(pos.getZ());return array;}
}
