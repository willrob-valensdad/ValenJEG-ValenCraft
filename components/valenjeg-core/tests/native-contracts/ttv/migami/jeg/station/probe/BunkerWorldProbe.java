package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.worldgen.structure.MinipurgeSourceHeight;

/** Copied-fixture only. Actual Fabric raw/state/BE/item checks precede native generation.
 * Existing configured placement/context/weighted arbitration; never forced starts. */
public final class BunkerWorldProbe {
 private static final String ID="jeg:valen_minipurge/underground_shelter",SHA="e205876f5ae33f3ae9a04652fbb1c2661e754568e7a7758d2c0305c59025dfdd";
 private static final String PRIOR_SHA="7f43e9040877bccb7161623e5ce74ccf8165f2a6121cb32165cd1eddb2661002";
 private static final Path PRIOR=Path.of(System.getProperty("valenjeg.evidence.f7162c8e2bee", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-bunker-probe.json"));
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Set<String> DECOR=Set.of("minecraft:armor_stand","minecraft:painting","minecraft:item_frame","minecraft:glow_item_frame");
 private static CompoundTag source;private static List<BlockState> palette;private static JsonObject rawReceipt;private static boolean rawPassed;
 private record ExpectedInventory(int stacks,int items){}
 private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 private static JsonArray xyz(BlockPos p){JsonArray a=new JsonArray();a.add(p.getX());a.add(p.getY());a.add(p.getZ());return a;}
 private static JsonArray stand(BlockPos p){JsonArray a=new JsonArray();a.add(p.getX()+0.5);a.add(p.getY()+1.0);a.add(p.getZ()+0.5);return a;}
 private static JsonArray sortRoute(JsonArray stairs){var rows=new ArrayList<JsonObject>();stairs.forEach(e->rows.add(e.getAsJsonObject()));rows.sort((a,b)->Integer.compare(b.getAsJsonArray("world").get(1).getAsInt(),a.getAsJsonArray("world").get(1).getAsInt()));JsonArray out=new JsonArray();int i=0;for(var row:rows){row.addProperty("down_route_index",i++);out.add(row);}return out;}
 private static JsonArray box(BoundingBox b){JsonArray a=new JsonArray();for(int v:new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()})a.add(v);return a;}
 private static void save(Path p,JsonObject o){try{Files.writeString(p,GSON.toJson(o)+"\n");}catch(Exception e){throw new IllegalStateException(e);}}
 private static void guard(MinecraftServer server)throws IOException{
  check(server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())&&Files.exists(FIXTURE.resolve("../preparation.json"))&&Boolean.getBoolean("valen.station.probe"),"Refusing bunker probe outside root-staged copied fixture");
 }
 public static void register(){
  boolean probe=Boolean.getBoolean("valen.bunker.probe"),readbackOnly=Boolean.getBoolean("valen.bunker.readbackOnly");
  if(!probe&&!readbackOnly)return;
  ServerLifecycleEvents.SERVER_STARTING.register(BunkerWorldProbe::preflight);
  if(readbackOnly){ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->noteReadbackOnly(server)));return;}
  ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->{
   boolean fixtureGuardPassed=false;
   try{guard(server);fixtureGuardPassed=true;check(rawPassed,"Actual Fabric raw preflight must pass");var job=new Job(server.overworld());ServerTickEvents.END_SERVER_TICK.register(s->{if(!job.done)job.step();});}
   catch(Throwable error){JsonObject r=new JsonObject();r.addProperty("status","fail");r.addProperty("error",error.toString());if(fixtureGuardPassed)save(FIXTURE.resolve("valen-bunker-probe.json"),r);error.printStackTrace();}
  }));
 }
 private static void noteReadbackOnly(MinecraftServer server){
  JsonObject r=new JsonObject();boolean fixtureGuardPassed=false;
  try{guard(server);fixtureGuardPassed=true;check(rawPassed,"Actual Fabric raw preflight must pass");r.addProperty("status","readback_only");r.addProperty("bunker_world_job_started",false);r.addProperty("resume_prior_or_fresh_search_started",false);r.addProperty("generation",false);r.addProperty("inventory_replay",false);r.addProperty("probe_receipt_rewritten",false);}
  catch(Throwable error){r.addProperty("status","fail");r.addProperty("error",error.toString());error.printStackTrace();}
  finally{if(fixtureGuardPassed)save(FIXTURE.resolve("valen-bunker-readback-only.json"),r);}
 }
 private static <T extends Comparable<T>> BlockState property(BlockState s,Property<T> p,String v){var value=p.getValue(v);check(value.isPresent(),"Invalid native property "+p.getName()+"="+v);return s.setValue(p,value.get());}
 static BlockState state(CompoundTag raw){
  Identifier id=Identifier.parse(raw.getStringOr("id",""));check(BuiltInRegistries.BLOCK.containsKey(id),"Unknown raw block "+id);BlockState s=BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
  var props=raw.getCompoundOrEmpty("properties");for(String key:props.keySet()){var p=s.getBlock().getStateDefinition().getProperty(key);check(p!=null,"Unknown raw property "+id+"."+key);s=property(s,p,props.getStringOr(key,""));}return s;
 }
 private static ItemStack item(CompoundTag n,HolderLookup.Provider lookup){var parsed=ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,lookup),n).getOrThrow();check(!parsed.isEmpty()&&parsed.getCount()==n.getIntOr("count",0),"Native source item/count lost");return parsed;}
 private static ExpectedInventory inventory(CompoundTag n,BlockEntity be,HolderLookup.Provider lookup){
  int stacks=0,items=0;var list=n.getListOrEmpty("Items");
  if(n.contains("Items")&&be instanceof Container){var c=(Container)be;Set<Integer> slots=new HashSet<>();
   for(Tag tag:list){var raw=(CompoundTag)tag;int slot=raw.getIntOr("Slot",-1);check(slot>=0&&slot<c.getContainerSize()&&slots.add(slot),"Invalid authored inventory slot");var expected=item(raw,lookup);check(ItemStack.matches(expected,c.getItem(slot)),"Native inventory slot/item/components/count differ");stacks++;items+=expected.getCount();}
   for(int slot=0;slot<c.getContainerSize();slot++)check(slots.contains(slot)||c.getItem(slot).isEmpty(),"Native container added source items");
  }
  if(!(be instanceof Container))check(list.isEmpty(),"Nonempty source inventory without native Container");
  if(n.contains("item")){var expected=item(n.getCompoundOrEmpty("item"),lookup);var actual=item(be.saveWithFullMetadata(lookup).getCompoundOrEmpty("item"),lookup);check(ItemStack.matches(expected,actual),"Native decorated pot item differs");stacks++;items+=expected.getCount();}
  return new ExpectedInventory(stacks,items);
 }
 private static BlockPos local(CompoundTag b){var p=b.getListOrEmpty("pos");return new BlockPos(((NumericTag)p.get(0)).intValue(),((NumericTag)p.get(1)).intValue(),((NumericTag)p.get(2)).intValue());}
 private static void preflight(MinecraftServer server){
  JsonObject r=new JsonObject();rawReceipt=r;Path output=server.getServerDirectory().resolve("valen-bunker-raw-preflight.json");boolean fixtureGuardPassed=false;
  try{
   guard(server);fixtureGuardPassed=true;var resource=server.getResourceManager().getResource(Identifier.parse("jeg:structure/valen_minipurge/underground_shelter.nbt")).orElseThrow();byte[] bytes;try(var in=resource.open()){bytes=in.readAllBytes();}
   String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));check(sha.equals(SHA),"Chosen bunker resource differs from frozen authored template");r.addProperty("selected_pack",resource.sourcePackId());r.addProperty("raw_sha256",sha);
   try(var in=new ByteArrayInputStream(bytes)){source=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}palette=new ArrayList<>();for(Tag t:source.getListOrEmpty("palette"))palette.add(state((CompoundTag)t));check(palette.size()==277,"Source277palette entries");
   int bes=0,stacks=0,items=0,cells=0,storage=0,air=0;var lookup=server.registryAccess();
   for(Tag t:source.getListOrEmpty("blocks")){var b=(CompoundTag)t;BlockState s=palette.get(b.getIntOr("state",-1));cells++;if(s.isAir())air++;check(!s.is(Blocks.SPAWNER),"Source skeletonspawner must remain inert");var n=b.getCompoundOrEmpty("nbt");if(n.isEmpty())continue;
    Identifier beId=Identifier.parse(n.getStringOr("id",""));check(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(beId)&&BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(beId).isValid(s),"Source native BE ID/state incompatible");var be=BlockEntity.loadStatic(BlockPos.ZERO,s,n,lookup);check(be!=null,"Actual Fabric source BE decode failed");bes++;if(be instanceof ChestBlockEntity||be instanceof BarrelBlockEntity)storage++;
    var count=inventory(n,be,lookup);stacks+=count.stacks();items+=count.items();
   }
   int decor=0,equipment=0;for(Tag t:source.getListOrEmpty("entities")){var e=(CompoundTag)t;var n=e.getCompoundOrEmpty("nbt");String id=n.getStringOr("id","");check(DECOR.contains(id)&&BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(id)),"Only source static decor permitted");decor++;if(n.contains("Item")){item(n.getCompoundOrEmpty("Item"),lookup);equipment++;}var eq=n.getCompoundOrEmpty("equipment");for(String slot:eq.keySet()){item(eq.getCompoundOrEmpty(slot),lookup);equipment++;}}
   check(cells==18837&&air==8911&&bes==163&&storage==113&&stacks==876&&items==19917&&decor==27&&equipment==20,"Actual Fabric authored payload counts differ");
   r.addProperty("status","pass");r.addProperty("proper_fabric_registry_phase","SERVER_STARTING before probe StructureTemplate/native starts");r.addProperty("source_cells",cells);r.addProperty("explicit_air",air);r.addProperty("native_block_entity_payloads",bes);r.addProperty("native_authored_inventory_stacks",stacks);r.addProperty("native_authored_inventory_items",items);r.addProperty("native_decor_type_payloads",decor);r.addProperty("native_decor_item_equipment_codecs",equipment);r.addProperty("source_creatures_spawned",false);rawPassed=true;
  }catch(Throwable failure){r.addProperty("status","fail");r.addProperty("error",failure.toString());throw new IllegalStateException("Bunker raw native preflight failed",failure);}finally{if(fixtureGuardPassed)save(output,r);}
 }
 private static final class Job{
  final ServerLevel level;final Holder.Reference<Structure> holder;final Structure structure;final RandomSpreadStructurePlacement placement;
  final JsonObject r=new JsonObject();final JsonArray trials=new JsonArray();final Set<Long> full=new HashSet<>();List<ChunkPos> chunks=List.of();int attempt,eligible,index,settle,ticks;boolean done,resume,priorBox;int priorMinX,priorMinY,priorMinZ,priorMaxX,priorMaxY,priorMaxZ;String phase="candidate";StructureStart start;PoolElementStructurePiece piece;
  Job(ServerLevel level){
   this.level=level;holder=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.parse(ID)));structure=holder.value();var p=level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder);check(p.size()==1&&p.getFirst() instanceof RandomSpreadStructurePlacement,"Native existing shelter placement required");placement=(RandomSpreadStructurePlacement)p.getFirst();
   r.addProperty("status","running");r.addProperty("id",ID);r.addProperty("world_seed",level.getSeed());r.addProperty("forced_starts",false);r.addProperty("forced_template",false);r.addProperty("old_structure_rows_or_loot_replayed",false);r.addProperty("type_category","Existing civil-defense-shelter variety; no new85type");r.addProperty("source_template_sha256",SHA);r.add("actual_raw_preflight",rawReceipt.deepCopy());r.add("candidate_diagnostics",trials);r.addProperty("max_grid_trials",384);r.addProperty("max_eligible_candidates",64);r.addProperty("max_NEW_FULL",64);r.addProperty("configured_structure_set_arbitration","civil_defense_shelter weight 1 and underground_shelter weight 1; native placement decides");r.addProperty("source_access_walk_visual_acceptance","Pending root-controlled actual native QA");if(Boolean.getBoolean("valen.bunker.resumePrior"))bindPrior();save(FIXTURE.resolve("valen-bunker-probe.json"),r);
  }
  void bindPrior(){
   try{
    byte[] bytes=Files.readAllBytes(PRIOR);String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));check(sha.equals(PRIOR_SHA),"Frozen R32 bunker receipt SHA differs");
    JsonObject prior=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
    check(prior.get("id").getAsString().equals(ID)&&prior.get("source_template_sha256").getAsString().equals(SHA)&&prior.getAsJsonObject("actual_raw_preflight").get("raw_sha256").getAsString().equals(SHA)&&"pass".equals(prior.getAsJsonObject("actual_raw_preflight").get("status").getAsString()),"Prior source e205 preflight differs");
    check(prior.get("world_seed").getAsLong()==level.getSeed(),"Prior world seed differs");
    check(!prior.get("forced_starts").getAsBoolean()&&prior.get("piece_count").getAsInt()==1,"Prior start was forced or not a single piece");
    check(prior.get("max_grid_trials").getAsInt()==384&&prior.get("max_eligible_candidates").getAsInt()==64&&prior.get("max_NEW_FULL").getAsInt()==64,"Prior search bounds differ");
    int checked=prior.get("candidates_checked").getAsInt();JsonArray diags=prior.getAsJsonArray("candidate_diagnostics");check(diags.size()==checked&&checked==19,"Prior grid cursor differs");
    JsonObject last=diags.get(checked-1).getAsJsonObject();check("actual_native_start_valid".equals(last.get("result").getAsString())&&last.get("grid_attempt").getAsInt()==checked&&last.get("chunk_x").getAsInt()==prior.get("start_chunk_x").getAsInt()&&last.get("chunk_z").getAsInt()==prior.get("start_chunk_z").getAsInt(),"Prior grid did not end on its actual start");
    check(prior.get("placement_eligible_candidates").getAsInt()==18&&prior.get("new_FULL_chunks").getAsInt()==9&&prior.get("placed_native_BE_payloads").getAsInt()==163&&prior.get("placed_native_inventory_stacks").getAsInt()==876&&prior.get("placed_native_inventory_items").getAsInt()==19917&&prior.get("source_spiral_top_opening_air_cells").getAsInt()==16,"Prior actual payload counts differ");
    JsonArray bounds=prior.getAsJsonArray("native_bounds");check(bounds.size()==6,"Prior bounds missing");priorMinX=bounds.get(0).getAsInt();priorMinY=bounds.get(1).getAsInt();priorMinZ=bounds.get(2).getAsInt();priorMaxX=bounds.get(3).getAsInt();priorMaxY=bounds.get(4).getAsInt();priorMaxZ=bounds.get(5).getAsInt();priorBox=true;attempt=checked;resume=true;
    r.addProperty("prior_receipt",PRIOR.toString());r.addProperty("prior_receipt_sha256",sha);r.addProperty("resumed_after_candidates_checked",checked);r.addProperty("prior_start_chunk_x",prior.get("start_chunk_x").getAsInt());r.addProperty("prior_start_chunk_z",prior.get("start_chunk_z").getAsInt());r.add("prior_native_bounds",bounds.deepCopy());r.add("prior_native_template_origin",prior.getAsJsonArray("native_template_origin").deepCopy());r.addProperty("prior_rotation",prior.get("rotation").getAsString());r.addProperty("prior_inventories_replayed",false);r.addProperty("prior_world_rewritten",false);
   }catch(AssertionError failure){throw failure;}catch(Exception failure){throw new IllegalStateException("Frozen R32 receipt resume refused",failure);}
  }
  boolean hitsPrior(ChunkPos cp){if(!priorBox)return false;int x0=cp.x()<<4,z0=cp.z()<<4;return x0<=priorMaxX&&x0+15>=priorMinX&&z0<=priorMaxZ&&z0+15>=priorMinZ;}
  void step(){try{
   check(++ticks<=18000,"Bunker fixture tick cap");if(phase.equals("candidate"))candidate();else if(phase.equals("full")){if(index<chunks.size()){var cp=chunks.get(index++);level.getChunk(cp.x(),cp.z());full.add(ChunkPos.pack(cp.x(),cp.z()));}else phase="settle";}else if(++settle>=40)readback();
  }catch(Throwable failure){done=true;r.addProperty("status","fail");r.addProperty("error",failure.toString());failure.printStackTrace();}finally{r.addProperty("new_FULL_chunks",full.size());save(FIXTURE.resolve("valen-bunker-probe.json"),r);}}
  void candidate(){
   if(attempt>=384||eligible>=64){done=true;r.addProperty("status","pending: no actual configured bunker start within bounded search");return;}
   int n=++attempt;ChunkPos cp=placement.getPotentialStructureChunk(level.getSeed(),1024+35*512+(n-1)*256,1024+35*768+(n-1)*512);var trial=new JsonObject();trials.add(trial);trial.addProperty("grid_attempt",n);trial.addProperty("chunk_x",cp.x());trial.addProperty("chunk_z",cp.z());r.addProperty("candidates_checked",attempt);
   if(hitsPrior(cp)){trial.addProperty("result","prior_r32_site_not_replayed");return;}
   var source=level.getChunkSource();var gs=source.getGeneratorState();if(!placement.isStructureChunk(gs,cp.x(),cp.z())){trial.addProperty("result","native_frequency_exclusion_reject");return;}eligible++;r.addProperty("placement_eligible_candidates",eligible);
   var generator=source.getGenerator();var context=new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),source.randomState().createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build()),source.randomState(),level.getStructureTemplateManager(),level.getSeed(),cp,level,structure.biomes()::contains);
   if(structure.findValidGenerationPoint(context).isEmpty()){trial.addProperty("result","native_source_origin_biome_or_generation_reject");return;}
   var c=source.getChunk(cp.x(),cp.z(),ChunkStatus.STRUCTURE_STARTS,true);check(c!=null,"Native STARTS missing");start=c.getStartForStructure(structure);if(start==null||!start.isValid()){trial.addProperty("result","native_weighted_set_arbitration_selected_other_or_absent");return;}
   check(start.getPieces().size()==1&&start.getPieces().getFirst() instanceof PoolElementStructurePiece,"Exact one native template piece");piece=(PoolElementStructurePiece)start.getPieces().getFirst();check(piece.getElement() instanceof SinglePoolElement&&((SinglePoolElement)piece.getElement()).getTemplateLocation().toString().equals(ID),"Wrong actual source template");
   trial.addProperty("result","actual_native_start_valid");r.addProperty("start_chunk_x",cp.x());r.addProperty("start_chunk_z",cp.z());r.add("native_bounds",box(start.getBoundingBox()));r.add("native_template_origin",xyz(piece.getPosition()));r.addProperty("rotation",piece.getRotation().toString());r.addProperty("mirror","NONE explicit source parity limit");r.addProperty("piece_count",1);
   BoundingBox b=start.getBoundingBox();var list=new ArrayList<ChunkPos>();for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)list.add(new ChunkPos(x,z));check(list.size()<=64,"Bunker FULL bound");
   if(resume){for(ChunkPos extra:list)if(hitsPrior(extra)){trial.addProperty("result","prior_r32_bounds_not_loaded");start=null;piece=null;return;}r.addProperty("fresh_bunker",true);r.addProperty("prior_site_rewritten",false);}
   chunks=List.copyOf(list);phase="full";
  }
  BlockPos world(BlockPos p){return StructureTemplate.transform(p,Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());}
  void readback(){
   var origin=piece.getPosition();var cs=level.getChunkSource();int surface=cs.getGenerator().getFirstFreeHeight(origin.getX(),origin.getZ(),Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,level,cs.randomState());check(origin.getY()==MinipurgeSourceHeight.baseY(surface,24)||origin.getY()==MinipurgeSourceHeight.baseY(surface,25),"Actual original-origin/depth/min63 rule failed");
   r.addProperty("native_origin_surface_air_y",surface);r.addProperty("actual_origin_depth",surface-origin.getY());r.addProperty("source_height_rule_match",true);
   int bes=0,stacks=0,items=0,topAir=0,stairs=0;JsonArray inventoryRows=new JsonArray(),walk=new JsonArray();JsonObject entry=null;
   for(Tag t:source.getListOrEmpty("blocks")){var b=(CompoundTag)t;var p=local(b);var expected=palette.get(b.getIntOr("state",-1)).rotate(piece.getRotation());var wp=world(p);var actual=level.getBlockState(wp);
    if(p.getY()==25&&p.getX()>=1&&p.getX()<=4&&p.getZ()>=1&&p.getZ()<=4){check(expected.isAir()&&actual.isAir(),"Original16explicit top shaft opening blocked");topAir++;}
    if(p.getX()==2&&p.getY()==25&&p.getZ()==1){entry=new JsonObject();entry.add("opening",xyz(wp));JsonArray feet=new JsonArray();feet.add(wp.getX()+0.5);feet.add((double)wp.getY());feet.add(wp.getZ()+0.5);entry.add("stand",feet);entry.addProperty("opening_air",expected.isAir()&&actual.isAir());}
    if(p.getX()>=0&&p.getX()<=5&&p.getZ()>=0&&p.getZ()<=5&&expected.getBlock() instanceof StairBlock){var row=new JsonObject();row.add("world",xyz(wp));row.add("stand",stand(wp));row.addProperty("facing",expected.getValue(StairBlock.FACING).getSerializedName());walk.add(row);if(p.getY()>=15){check(actual.equals(expected),"Original upper spiral stair changed or blocked");stairs++;}}
    var n=b.getCompoundOrEmpty("nbt");if(n.isEmpty())continue;var be=level.getBlockEntity(wp);check(be!=null&&BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString().equals(n.getStringOr("id","")),"Actual placed source BE missing/type mismatch at"+wp);bes++;
    var count=inventory(n,be,level.registryAccess());stacks+=count.stacks();items+=count.items();if(count.stacks()>0){var row=new JsonObject();row.add("pos",xyz(wp));row.addProperty("native_source_stacks",count.stacks());row.addProperty("native_source_items",count.items());inventoryRows.add(row);}
   }
   check(bes==163&&stacks==876&&items==19917&&topAir==16&&stairs>=8,"Actual source BE/inventory/shaft counts differ");r.addProperty("placed_native_BE_payloads",bes);r.addProperty("placed_native_inventory_stacks",stacks);r.addProperty("placed_native_inventory_items",items);r.add("placed_native_inventory_rows",inventoryRows);r.addProperty("source_spiral_top_opening_air_cells",topAir);r.addProperty("source_upper_spiral_stairs",stairs);r.add("source_walk_stairs_high_to_low",sortRoute(walk));if(entry!=null)r.add("source_walk_entry",entry);r.addProperty("source_walk_route","Ordinary Survival walk uses source_walk_entry then source_walk_stairs_high_to_low stands. No teleport.");
   var decor=new TreeMap<String,Integer>();for(Entity e:level.getEntitiesOfClass(Entity.class,AABB.of(start.getBoundingBox()).inflate(1),e->DECOR.contains(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString())))decor.merge(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(),1,Integer::sum);
   r.add("observed_native_decor_entities",GSON.toJsonTree(decor));r.addProperty("native_decor_placement_status",decor.values().stream().mapToInt(Integer::intValue).sum()==27?"27actualnative decor observed; rotation/visual QA pending":"Entity visibility/placement/support pending root QA; raw27typed payloads proved separately");
   r.addProperty("status","native raw/actual configured start/original origin/placed inventory/spiral readback passed; access/decor visual acceptance pending");done=true;
  }
 }
}
