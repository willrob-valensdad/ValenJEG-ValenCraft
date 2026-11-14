package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/** Fixture search only. Native biome location seeds actual unchanged placement regions. */
final class AirfieldNativeSearch {
 record Next(ChunkPos chunk,boolean done,JsonObject diagnostic){}
 private final ServerLevel level;private final Holder<Structure> holder;private final RandomSpreadStructurePlacement spread;
 private final List<BlockPos> recordedOrigins;private final int stableSlot,startAttempt;private final Set<Holder<Biome>> reachable;
 private final JsonArray queries=new JsonArray();private final Set<Long> seen=new HashSet<>();
 private int anchors,regionX,regionZ,maxOrigins=4,emitted,nextConfiguredGrid;private boolean skipRecordedSpirals;private Iterator<? extends BlockPos> offsets=Collections.emptyIterator();
 AirfieldNativeSearch(ServerLevel level,Holder<Structure> holder,int stableSlot,int startAttempt,JsonObject site){this(level,holder,stableSlot,startAttempt,site,List.of());}
 AirfieldNativeSearch(ServerLevel level,Holder<Structure> holder,int stableSlot,int startAttempt,JsonObject site,List<BlockPos> recordedOrigins){this(level,holder,stableSlot,startAttempt,site,recordedOrigins,4);}
 AirfieldNativeSearch(ServerLevel level,Holder<Structure> holder,int stableSlot,int startAttempt,JsonObject site,List<BlockPos> recordedOrigins,int maxOrigins){
  if(maxOrigins<1||maxOrigins>4)throw new AssertionError("Biome locator origin cap stays 1..4");this.maxOrigins=maxOrigins;this.recordedOrigins=List.copyOf(recordedOrigins);if(this.recordedOrigins.size()>4)throw new AssertionError("Recorded origin cap unchanged");this.level=level;this.holder=holder;this.stableSlot=stableSlot;this.startAttempt=startAttempt;
  var placements=level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder);
  if(placements.size()!=1||!(placements.getFirst() instanceof RandomSpreadStructurePlacement p))throw new AssertionError("One actual native random-spread placement required");spread=p;
  var possible=level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();reachable=new HashSet<>(possible);reachable.removeIf(b->!holder.value().biomes().contains(b));
  JsonObject coverage=new JsonObject();coverage.add("actual_source_possible_biomes",ids(possible));coverage.add("actual_allowed_possible_intersection",ids(reachable));coverage.addProperty("actual_source_possible_count",possible.size());coverage.addProperty("intersection_count",reachable.size());coverage.addProperty("height_or_chunk_queries",0);site.add("native_possible_biome_coverage",coverage);
  JsonObject limits=new JsonObject();limits.addProperty("native_biome_calls_max",4);limits.addProperty("native_origins_max",maxOrigins);limits.addProperty("one_biome_call_or_one_configured_candidate_per_tick",true);limits.addProperty("biome_radius_blocks",1024);limits.addProperty("horizontal_step_blocks",64);limits.addProperty("vertical_step_blocks",128);limits.addProperty("max_biome_samples_per_call",1089*((level.getMaxY()-level.getMinY()+127)/128+1));limits.addProperty("native_regions_per_anchor_max",25);limits.addProperty("candidate_max",100);limits.addProperty("original_frequency_exclusion_weighted_start_predicate",true);limits.addProperty("native_surface_height_queries_per_recorded_origin_max",recordedOrigins.isEmpty()?0:1);limits.addProperty("recorded_origin_count",recordedOrigins.size());site.add("native_locator_bounds",limits);site.add("native_biome_locator_queries",queries);site.addProperty("candidate_search_strategy","Native possible-biome intersection, native biome locator, native spiral placement regions; cumulative ordinal does not imply old linear coordinates");
 }
 void continueFromConfiguredGrid(int nextGrid,Set<Long> alreadySeen){if(nextGrid<1||nextGrid>380)throw new AssertionError("Configured-grid continuation stays inside the existing 384 bound");if(alreadySeen==null||alreadySeen.isEmpty())throw new AssertionError("Continuation requires already checked chunks");skipRecordedSpirals=true;nextConfiguredGrid=nextGrid;seen.addAll(alreadySeen);}
 private static JsonArray ids(Collection<Holder<Biome>> biomes){JsonArray a=new JsonArray();biomes.stream().map(b->b.unwrapKey().orElseThrow().identifier().toString()).sorted().forEach(a::add);return a;}
 Next next(){
  JsonObject diagnostic=new JsonObject();
  if(reachable.isEmpty()){diagnostic.addProperty("result","actual_possible_biome_intersection_empty");return new Next(null,true,diagnostic);}
  while(offsets.hasNext()){
   BlockPos offset=offsets.next();ChunkPos cp=spread.getPotentialStructureChunk(level.getSeed(),(regionX+offset.getX())*spread.spacing(),(regionZ+offset.getZ())*spread.spacing());
   if(!seen.add(ChunkPos.pack(cp.x(),cp.z())))continue;
   diagnostic.addProperty("candidate_source","native_biome_assisted_configured_region");diagnostic.addProperty("locator_anchor",anchors);boolean recordedAnchor=!skipRecordedSpirals&&!recordedOrigins.isEmpty()&&anchors<=recordedOrigins.size();if(skipRecordedSpirals){diagnostic.addProperty("source_legacy_grid_attempt",nextConfiguredGrid+anchors-1);diagnostic.addProperty("locator_origin","existing_configured_grid_after_recorded_origins");}else if(recordedOrigins.isEmpty())diagnostic.addProperty("source_anchor_legacy_grid_attempt",startAttempt+anchors);else if(recordedAnchor)diagnostic.addProperty("immutable_recorded_origin_index",anchors);else diagnostic.addProperty("locator_origin","existing_configured_grid_after_recorded_origins");diagnostic.addProperty("region_offset_x",offset.getX());diagnostic.addProperty("region_offset_z",offset.getZ());diagnostic.addProperty("legacy_grid_coordinates_used",false);
   if(!spread.getPotentialStructureChunk(level.getSeed(),cp.x(),cp.z()).equals(cp))throw new AssertionError("Locator changed native configured-placement chunk");
   emitted++;diagnostic.addProperty("configured_regions_emitted",emitted);return new Next(cp,false,diagnostic);
  }
  if(emitted>=100){diagnostic.addProperty("result","bounded_native_configured_regions_exhausted");diagnostic.addProperty("configured_regions_emitted",emitted);return new Next(null,true,diagnostic);}
  if(anchors>=maxOrigins||startAttempt+anchors>=384||!skipRecordedSpirals&&!recordedOrigins.isEmpty()&&anchors==recordedOrigins.size()&&!Boolean.getBoolean("valen.structure.probe.cherryInner1Trace")){diagnostic.addProperty("result","bounded_native_locator_origins_exhausted");return new Next(null,true,diagnostic);}
  int grid=skipRecordedSpirals?nextConfiguredGrid+(++anchors)-1:startAttempt+(++anchors);BlockPos center;JsonObject query=new JsonObject();query.addProperty("anchor",anchors);
  if(skipRecordedSpirals||recordedOrigins.isEmpty()||anchors>recordedOrigins.size()){ChunkPos origin=spread.getPotentialStructureChunk(level.getSeed(),1024+stableSlot*512+(grid-1)*256,1024+stableSlot*768+(grid-1)*512);center=new BlockPos(origin.getMiddleBlockX(),64,origin.getMiddleBlockZ());query.addProperty("source_legacy_grid_attempt",grid);if(skipRecordedSpirals||!recordedOrigins.isEmpty())query.addProperty("locator_origin","existing_configured_grid_after_recorded_origins");}
  else{BlockPos recorded=recordedOrigins.get(anchors-1);var source=level.getChunkSource();int surface=source.getGenerator().getBaseHeight(recorded.getX(),recorded.getZ(),net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,level,source.randomState());center=new BlockPos(recorded.getX(),surface,recorded.getZ());query.addProperty("immutable_recorded_origin_index",anchors);query.addProperty("native_surface_height_locator_seed_only",true);}
  query.addProperty("origin_x",center.getX());query.addProperty("origin_y",center.getY());query.addProperty("origin_z",center.getZ());
  long started=System.nanoTime();var found=level.findClosestBiome3d(reachable::contains,center,1024,64,128);query.addProperty("observed_elapsed_ms",(System.nanoTime()-started)/1_000_000L);
  if(found==null){query.addProperty("result","native_biome_not_found_in_bound");queries.add(query);return new Next(null,false,query);}
  BlockPos pos=found.getFirst();query.addProperty("result","native_biome_found");query.addProperty("biome",found.getSecond().unwrapKey().orElseThrow().identifier().toString());query.addProperty("x",pos.getX());query.addProperty("y",pos.getY());query.addProperty("z",pos.getZ());queries.add(query);
  regionX=Math.floorDiv(pos.getX()>>4,spread.spacing());regionZ=Math.floorDiv(pos.getZ()>>4,spread.spacing());offsets=BlockPos.spiralAround(BlockPos.ZERO,2,Direction.EAST,Direction.SOUTH).iterator();return new Next(null,false,query);
 }
}
