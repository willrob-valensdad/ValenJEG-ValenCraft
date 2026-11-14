package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
/** Read-only recheck of the two R35 industrial sites. Does not search, place, ticket, generate, or unpack loot. */
final class IndustrialRecheck {
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final Path REPORT=Path.of(System.getProperty("valenjeg.evidence.a93c1a6a4c54", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-industrial.json"));
 private static final String REPORT_SHA="e342418f93b9638ab40e7c90c5cde2c18858709782ed132d76d920dc1e5c4bdd";
 private static final long SEED=-4056840922791553232L;
 private static final String TRAIN="jeg:valen_industrial/train_station";
 private static final String WAREHOUSE="jeg:valen_industrial/warehouse";
 private static final String TRAIN_SHA="8f3f9c68d5e63bd0fff17eccac20a8d931557b5d52c942ed08c00460ea97abb4";
 private static final String WAREHOUSE_SHA="b4f008ddfcdaf5305d382130a10d425b0ba1c243098abc34c5aeb33d7404fc32";
 private static final String WAREHOUSE_ERROR="java.lang.AssertionError: Placed source block changed or became air at BlockPos{x=319360, y=64, z=471056}";
 private static final int RAW_WAREHOUSE_BLOCK_ENTITIES=116,RAW_TRAIN_BLOCK_ENTITIES=79;
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private IndustrialRecheck(){}
 static boolean consumeIfRequested(MinecraftServer server,Path inbox,Path game){
  try{
   if(!Files.exists(inbox))return false;
   JsonObject request=JsonParser.parseString(Files.readString(inbox)).getAsJsonObject();
   if(!request.has("action")||!request.get("action").getAsString().equals("industrial_recheck"))return false;
   Files.delete(inbox);write(server,request,game);return true;
  }catch(Exception error){return false;}
 }
 static void write(MinecraftServer server,JsonObject request,Path game){
  JsonObject out=new JsonObject();out.add("request",request);
  try{read(server,out);if(!out.has("status"))out.addProperty("status","read");}
  catch(Throwable error){out.addProperty("status","fail");out.addProperty("error",error.toString());}
  try{String text=GSON.toJson(out)+"\n";Files.writeString(game.resolve("valen-industrial-recheck.json"),text);Files.writeString(game.resolve("valen-station-server-state.json"),text);Files.writeString(game.resolve("valen-station-server-actions.jsonl"),out+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
  catch(Exception error){error.printStackTrace();}
 }
 private static void read(MinecraftServer server,JsonObject out)throws Exception{
  out.addProperty("read_only",true);out.addProperty("world_modified",false);out.addProperty("chunks_ticketed_or_generated",false);out.addProperty("loot_unpacked",false);
  out.addProperty("structure_probe_job_rerun",false);out.addProperty("prior_thirty_four_sites_replayed",false);out.addProperty("frozen_report_rewritten",false);
  ServerLevel level=server.overworld();
  if(!Boolean.getBoolean("valen.industrial.probe")||!Boolean.getBoolean("valen.station.probe")||!server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())||!Files.isRegularFile(FIXTURE.resolve("../preparation.json"))||server.getPort()!=25589||level.getSeed()!=SEED||!level.dimension().identifier().toString().equals("minecraft:overworld"))
   throw new IllegalStateException("industrial_recheck requires the copied fixture, industrial and station probes, port 25589, overworld, and seed "+SEED);
  byte[] frozen=Files.readAllBytes(REPORT);String actual=sha(frozen);out.addProperty("frozen_report",REPORT.toString());out.addProperty("frozen_report_sha256",actual);
  if(!actual.equals(REPORT_SHA))throw new IllegalStateException("Frozen R35 industrial report hash "+actual);
  JsonObject report=JsonParser.parseString(new String(frozen,StandardCharsets.UTF_8)).getAsJsonObject();
  if(report.get("world_seed").getAsLong()!=SEED)throw new IllegalStateException("Frozen report seed differs");
  if(report.get("generated_structure_count").getAsInt()!=2||report.get("searched_structure_count_this_run").getAsInt()!=2)throw new IllegalStateException("Frozen report is not the two-structure R35 run");
  JsonArray sites=report.getAsJsonArray("sites");if(sites==null||sites.size()!=2)throw new IllegalStateException("Frozen report does not contain the two existing industrial sites");
  JsonObject train=sites.get(0).getAsJsonObject(),warehouse=sites.get(1).getAsJsonObject();
  checkSite(train,TRAIN,20485,30718,new int[]{327748,50,491476,327788,83,491518},"actual native start and full-chunk placement read back");
  checkSite(warehouse,WAREHOUSE,19960,29441,new int[]{319329,51,471027,319372,102,471068},"actual native start placed; source marker count mismatch");
  if(!train.getAsJsonObject("authored_source_contract").get("sha256").getAsString().equals(TRAIN_SHA))throw new IllegalStateException("Frozen train station source hash differs");
  if(!warehouse.get("authored_source_contract_error").getAsString().equals(WAREHOUSE_ERROR))throw new IllegalStateException("Frozen warehouse failure proof differs");
  out.addProperty("r35_status",report.get("status").getAsString());out.addProperty("r35_partial_failure_preserved",true);
  out.addProperty("r35_train_station_status",train.get("status").getAsString());out.addProperty("r35_warehouse_error",WAREHOUSE_ERROR);
  out.addProperty("r35_warehouse_editor_world_x",319360);out.addProperty("r35_warehouse_editor_world_y",64);out.addProperty("r35_warehouse_editor_world_z",471056);
  out.addProperty("raw_warehouse_block_entities",RAW_WAREHOUSE_BLOCK_ENTITIES);out.addProperty("raw_train_station_block_entities",RAW_TRAIN_BLOCK_ENTITIES);
  out.addProperty("raw_block_entities",RAW_WAREHOUSE_BLOCK_ENTITIES+RAW_TRAIN_BLOCK_ENTITIES);out.addProperty("raw_block_entity_total_196_is_not_the_raw_count",true);
  JsonArray rows=new JsonArray();boolean pending=false;
  pending|=recheck(level,train,rows);pending|=recheck(level,warehouse,rows);out.add("sites",rows);
  if(pending){out.addProperty("status","pending");out.addProperty("reason","A frozen site chunk is not already FULL. Required ordinary client visit coordinates are listed; this action does not load chunks.");}
 }
 private static void checkSite(JsonObject site,String id,int chunkX,int chunkZ,int[] box,String status){
  if(!site.get("id").getAsString().equals(id)||site.get("start_chunk_x").getAsInt()!=chunkX||site.get("start_chunk_z").getAsInt()!=chunkZ||!site.get("status").getAsString().equals(status))
   throw new IllegalStateException("Frozen site identity differs for "+id);
  JsonArray bbox=site.getAsJsonArray("bounding_box");
  for(int i=0;i<box.length;i++)if(bbox.get(i).getAsInt()!=box[i])throw new IllegalStateException("Frozen bounding box differs for "+id);
 }
 private static boolean recheck(ServerLevel level,JsonObject frozen,JsonArray rows){
  JsonObject row=new JsonObject();rows.add(row);row.addProperty("id",frozen.get("id").getAsString());row.add("frozen_status",frozen.get("status"));
  if(frozen.has("authored_source_contract_error"))row.add("frozen_error",frozen.get("authored_source_contract_error"));
  int chunkX=frozen.get("start_chunk_x").getAsInt(),chunkZ=frozen.get("start_chunk_z").getAsInt();
  JsonArray box=frozen.getAsJsonArray("bounding_box");int y=frozen.get("unadapted_center_surface_y").getAsInt();
  JsonArray missing=new JsonArray();ChunkAccess startChunk=null;
  for(int x=box.get(0).getAsInt()>>4;x<=box.get(3).getAsInt()>>4;x++)for(int z=box.get(2).getAsInt()>>4;z<=box.get(5).getAsInt()>>4;z++){
   ChunkAccess chunk=level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);
   if(x==chunkX&&z==chunkZ)startChunk=chunk;
   if(chunk==null||!chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)){JsonObject visit=new JsonObject();visit.addProperty("chunk_x",x);visit.addProperty("chunk_z",z);visit.addProperty("x",x*16+8);visit.addProperty("y",y);visit.addProperty("z",z*16+8);missing.add(visit);}
  }
  row.addProperty("queried_with_getChunk_FULL_false",true);row.addProperty("chunk_load_or_generation_requested",false);
  if(!missing.isEmpty()){row.addProperty("status","pending");row.add("required_ordinary_client_visit",missing);row.addProperty("readPlaced",false);return true;}
  IndustrialSourceContracts.requirePassed();
  String liveSha=IndustrialSourceContracts.expectedMarkers(frozen.get("id").getAsString()).get("sha256").getAsString();
  String frozenSha=frozen.get("id").getAsString().equals(WAREHOUSE)?WAREHOUSE_SHA:TRAIN_SHA;
  if(!liveSha.equals(frozenSha)){row.addProperty("status","pending");row.addProperty("live_sha256",liveSha);row.addProperty("frozen_r35_sha256",frozenSha);row.addProperty("readPlaced",false);row.addProperty("reason","Live source bytes differ from the frozen R35 template. The old placement is not new-source proof and is not relabeled compatible.");return true;}
  Structure structure=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.parse(frozen.get("id").getAsString()))).value();
  StructureStart start=startChunk.getStartForStructure(structure);
  if(start==null||!start.isValid())throw new IllegalStateException("Visible start chunk has no valid existing start for "+frozen.get("id").getAsString());
  row.add("readPlaced",IndustrialSourceContracts.readPlaced(level,start,frozen.get("id").getAsString()));row.addProperty("status","read");return false;
 }
 private static String sha(byte[] data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}catch(NoSuchAlgorithmException error){throw new IllegalStateException(error);}}
}
