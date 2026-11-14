package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import ttv.migami.jeg.structure.mesh.HbmQuarterTurn;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.LoggerFactory;
/** Fixture-only raw preflight for the one Desert Atom template. It does not place blocks or roll loot. */
public final class DesertComplexContracts {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final String ID="jeg:valen/desert_atom_complex";
 private static final String SHA="5e70747d59e5bbe97defb3d1844f696cb5b9b24cf5d51d9436eae41234fb0580";
 private static final int[] SIZE={41,25,34};
 private static final int CELLS=5168,PALETTE=89,UNIQUE_IDS=48,AIR=3,CHESTS=5,BARRELS=1,BEDROCK=0,EDITORS=0,PROPERTY_VARIANTS=296;
 private static final String[] BLOCKSTATES={"hbm_barbed_wire","hbm_deco_steel","hbm_metal_fence","hbm_pole_top","hbm_reinforced_glass","hbm_reinforced_lamp","hbm_satellite_receiver","hbm_steel_pole","hbm_tape_recorder","hbm_yellow_barrel"};
 private static final Map<String,Integer> PROPS=Map.of(
  "jeg:hbm_deco_steel",536,"jeg:hbm_metal_fence",456,"jeg:hbm_barbed_wire",134,"jeg:hbm_yellow_barrel",105,"jeg:hbm_steel_pole",60,
  "jeg:hbm_reinforced_glass",55,"jeg:hbm_tape_recorder",15,"jeg:hbm_satellite_receiver",5,"jeg:hbm_reinforced_lamp",5,"jeg:hbm_pole_top",2);
 private static final Map<String,Integer> META=Map.of(
  "jeg:hbm_barbed_wire/0",134,"jeg:hbm_satellite_receiver/3",1,"jeg:hbm_satellite_receiver/4",1,"jeg:hbm_satellite_receiver/5",3,
  "jeg:hbm_steel_pole/2",18,"jeg:hbm_steel_pole/3",18,"jeg:hbm_steel_pole/4",9,"jeg:hbm_steel_pole/5",15,"jeg:hbm_tape_recorder/4",8,"jeg:hbm_tape_recorder/5",7);
 private static final String[] CHEST_KEYS={"9,1,14 jeg:chests/valen/field_supplies","22,5,13 jeg:chests/valen/field_supplies","24,5,26 jeg:chests/valen/field_supplies","36,5,12 jeg:chests/valen/field_supplies","36,9,9 jeg:chests/valen/research_cache"};
 private static final Set<String> BUILTIN_MESH_KINDS=Set.of("fence","pole_top","satellite");
 private static boolean passed;private static JsonObject report;
 private DesertComplexContracts(){}
 private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public static void register(){if(Boolean.getBoolean("valen.desertComplex.probe"))ServerLifecycleEvents.SERVER_STARTING.register(DesertComplexContracts::preflight);}
 public static void requirePassed(){check(passed,"Desert complex raw preflight has not passed");}
 public static JsonObject readPlaced(ServerLevel level,StructureStart start,String id){
  requirePassed();check(id.equals(ID),"Desert readback received another structure");
  check(start.getPieces().size()==1&&start.getPieces().getFirst() instanceof PoolElementStructurePiece,"Desert start is not the one authored piece");
  PoolElementStructurePiece piece=(PoolElementStructurePiece)start.getPieces().getFirst();
  check(piece.getElement() instanceof SinglePoolElement single&&single.getTemplateLocation().toString().equals(ID),"Placed template is not the desert complex");
  byte[] bytes=resourceBytes(level.getServer(),Identifier.parse("jeg:structure/valen/desert_atom_complex.nbt"));
  check(sha(bytes).equals(SHA),"Placed desert template hash differs");
  CompoundTag tag=read(bytes);List<BlockState> palette=new ArrayList<>();
  for(Tag entry:tag.getListOrEmpty("palette"))palette.add(resolve((CompoundTag)entry,new JsonObject(),new JsonArray()));
  int loot=0,ignoredEditor=0,compared=0;Map<String,Integer> tables=new LinkedHashMap<>();JsonArray rows=new JsonArray(),stateErrors=new JsonArray();
  for(Tag entry:tag.getListOrEmpty("blocks")){
   CompoundTag block=(CompoundTag)entry;   BlockState expected=palette.get(block.getIntOr("state",-1));check(expected!=null,"Placed desert cell has no resolved state");
   BlockState rotated=expected.rotate(piece.getRotation());turn(expected,rotated,piece.getRotation());
   var local=block.getListOrEmpty("pos");int x=((NumericTag)local.get(0)).intValue(),y=((NumericTag)local.get(1)).intValue(),z=((NumericTag)local.get(2)).intValue();
   BlockPos world=StructureTemplate.transform(new BlockPos(x,y,z),Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());
   CompoundTag nbt=block.getCompoundOrEmpty("nbt");
   if(expected.is(Blocks.STRUCTURE_BLOCK)||nbt.getStringOr("id","").equals("minecraft:structure_block")){ignoredEditor++;continue;}
   BlockState actual=level.getBlockState(world);compared++;
   if(!actual.equals(rotated)){if(stateErrors.size()<64){JsonObject error=new JsonObject();error.addProperty("world",world.toString());error.addProperty("source",x+","+y+","+z);error.addProperty("expected",rotated.toString());error.addProperty("actual",actual.toString());stateErrors.add(error);}continue;}
   if(nbt.isEmpty())continue;
   String rawId=nbt.getStringOr("id","");var be=level.getBlockEntity(world);String actualBe=be==null?"absent":BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString();
   check(be!=null&&actualBe.equals(rawId),"Placed desert block entity at world "+world+" source ["+x+","+y+","+z+"] expected "+rawId+" actual "+actualBe);
   if(be instanceof RandomizableContainerBlockEntity container&&nbt.contains("LootTable")){var key=container.getLootTable();check(key!=null&&key.identifier().toString().equals(nbt.getStringOr("LootTable","")),"Placed desert loot id differs at "+world);check(packed(container,level.getServer()),"Placed desert loot was unpacked before read-only NBT inspection");loot++;tables.merge(key.identifier().toString(),1,Integer::sum);JsonObject row=new JsonObject();row.addProperty("source",x+","+y+","+z);row.addProperty("table",key.identifier().toString());rows.add(row);}
  }
  check(stateErrors.isEmpty(),"Placed desert source-state mismatches: "+stateErrors);
  check(ignoredEditor==EDITORS&&compared==CELLS&&loot==CHESTS&&tables.getOrDefault("jeg:chests/valen/field_supplies",0)==4&&tables.getOrDefault("jeg:chests/valen/research_cache",0)==1,"Placed desert cell, editor, or loot counts differ");
  JsonObject evidence=new JsonObject();evidence.addProperty("sha256",SHA);evidence.addProperty("compared_source_cells",compared);evidence.addProperty("ignored_editor_structure_blocks",ignoredEditor);evidence.addProperty("source_bedrock_cells",BEDROCK);evidence.addProperty("placed_loot",loot);evidence.add("placed_loot_rows",rows);evidence.addProperty("placed_spawner_count",0);evidence.addProperty("cache_item_pool_rolls",0);evidence.addProperty("ghoul_activation",false);evidence.addProperty("loot_unpacked",false);
  evidence.addProperty("piece_position",piece.getPosition().toString());evidence.addProperty("piece_rotation",piece.getRotation().toString());
  BlockPos ground=StructureTemplate.transform(new BlockPos(0,4,0),Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());evidence.addProperty("template_y4_world",ground.toString());evidence.addProperty("source_y_minus_1_is_template_y",4);evidence.addProperty("heightmap_proposal_absolute",-4);evidence.addProperty("placement_equivalence_claimed",false);
  evidence.addProperty("processor","jeg:valen/preserve_bedrock protects existing features_cannot_replace blocks. This template has no bedrock cells, so none were skipped.");return evidence;
 }
 private static void preflight(MinecraftServer server){
  report=new JsonObject();JsonArray diagnostics=new JsonArray(),pending=new JsonArray();boolean contract=true;
  report.addProperty("template",ID);report.addProperty("sha256",SHA);report.addProperty("phase","SERVER_STARTING raw desert template; no placement and no loot roll");
  byte[] bytes;try{bytes=resourceBytes(server,Identifier.parse("jeg:structure/valen/desert_atom_complex.nbt"));}catch(RuntimeException error){pending.add("staged desert template missing");finish(server,contract,pending,diagnostics);return;}
  if(!sha(bytes).equals(SHA)){contract=false;diagnostics.add(note("hash","staged desert template hash differs"));}
  CompoundTag tag=read(bytes);if(tag.getIntOr("DataVersion",-1)!=5023){contract=false;diagnostics.add(note("DataVersion","expected 5023"));}
  var size=tag.getListOrEmpty("size");if(size.size()!=3){contract=false;diagnostics.add(note("size","missing"));}else for(int i=0;i<3;i++)if(((NumericTag)size.get(i)).intValue()!=SIZE[i]){contract=false;diagnostics.add(note("size","dimensions differ"));}
  if(!tag.getListOrEmpty("entities").isEmpty()){contract=false;diagnostics.add(note("entities","template entities reappeared"));}
  List<BlockState> palette=new ArrayList<>();JsonArray paletteStates=new JsonArray();Set<String> ids=new LinkedHashSet<>();int quarantined=0;
  for(Tag entry:tag.getListOrEmpty("palette")){if(!(entry instanceof CompoundTag raw)){quarantined++;palette.add(null);JsonObject row=new JsonObject();row.addProperty("error","palette entry is not a compound");paletteStates.add(row);continue;}JsonObject row=new JsonObject();BlockState state=resolve(raw,row,diagnostics);palette.add(state);paletteStates.add(row);if(state==null)quarantined++;else ids.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());}
  report.add("palette_states",paletteStates);
  if(palette.size()!=PALETTE||ids.size()!=UNIQUE_IDS||quarantined!=0){contract=false;diagnostics.add(note("palette","count, unique ids, or quarantine differs"));}
  Map<String,Integer> props=new LinkedHashMap<>(),meta=new LinkedHashMap<>(),loot=new LinkedHashMap<>();Set<String> chests=new LinkedHashSet<>();int cells=0,air=0,bedrock=0,editors=0,barrels=0,decoded=0;
  for(Tag entry:tag.getListOrEmpty("blocks")){
   CompoundTag block=(CompoundTag)entry;cells++;int index=block.getIntOr("state",-1);if(index<0||index>=palette.size()||palette.get(index)==null){contract=false;diagnostics.add(note("cell","unresolved palette index"));continue;}
   BlockState state=palette.get(index);String blockId=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();if(state.is(Blocks.AIR))air++;if(state.is(Blocks.BEDROCK))bedrock++;if(state.is(Blocks.STRUCTURE_BLOCK))editors++;
   if(PROPS.containsKey(blockId))props.merge(blockId,1,Integer::sum);var properties=tagPalette(tag,index);String metaValue=properties.get("meta");if(metaValue!=null)meta.merge(blockId+"/"+metaValue,1,Integer::sum);
   if(blockId.equals("jeg:hbm_metal_fence")&&(metaValue!=null||!"false".equals(properties.get("waterlogged")))){contract=false;diagnostics.add(note(blockId,"fence meta or waterlogged differs"));}
   if(blockId.equals("jeg:hbm_pole_top")&&metaValue!=null){contract=false;diagnostics.add(note(blockId,"pole top gained a meta key"));}
   CompoundTag nbt=block.getCompoundOrEmpty("nbt");if(nbt.isEmpty()){if(blockId.equals("minecraft:barrel"))barrels++;continue;}
   String rawId=nbt.getStringOr("id","");if(rawId.equals("minecraft:structure_block"))editors++;
   if(!rawId.equals("minecraft:chest")){contract=false;diagnostics.add(note(rawId,"unexpected desert block entity"));continue;}
   if(nbt.contains("Items")||nbt.contains("LootTableSeed")){contract=false;diagnostics.add(note(rawId,"chest carries items or a seed; loot stays packed"));continue;}
   var local=block.getListOrEmpty("pos");String key=((NumericTag)local.get(0)).intValue()+","+((NumericTag)local.get(1)).intValue()+","+((NumericTag)local.get(2)).intValue()+" "+nbt.getStringOr("LootTable","");chests.add(key);loot.merge(nbt.getStringOr("LootTable",""),1,Integer::sum);
   Identifier beId=Identifier.parse(rawId);if(!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(beId)){pending.add(rawId+" absent");continue;}
   var type=BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(beId);if(!type.isValid(state)){pending.add(rawId+" invalid for "+blockId);continue;}
   BlockEntity loaded;try{loaded=type.create(BlockPos.ZERO,state);}catch(Throwable error){pending.add(rawId+" type.create threw "+error);continue;}
   if(loaded==null){pending.add(rawId+" type.create returned null");continue;}
   String fieldLoss=null;try(var reporter=new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(rawId),LoggerFactory.getLogger(DesertComplexContracts.class))){loaded.loadWithComponents(TagValueInput.create(reporter,server.registryAccess(),nbt));if(!reporter.isEmpty())fieldLoss=reporter.getReport();}catch(Throwable error){pending.add(rawId+" loadWithComponents threw "+error);continue;}
   if(fieldLoss!=null){pending.add(rawId+" field loss: "+fieldLoss);continue;}
   if(!(loaded instanceof RandomizableContainerBlockEntity container)||container.getLootTable()==null||!container.getLootTable().identifier().toString().equals(nbt.getStringOr("LootTable",""))||!packed(container,server)){contract=false;diagnostics.add(note(rawId,"loot id changed or items were unpacked"));continue;}
   decoded++;
  }
  if(cells!=CELLS||air!=AIR||bedrock!=BEDROCK||editors!=EDITORS||barrels!=BARRELS||decoded!=CHESTS||!props.equals(PROPS)||!meta.equals(META)||!chests.equals(Set.of(CHEST_KEYS))||loot.getOrDefault("jeg:chests/valen/field_supplies",0)!=4||loot.getOrDefault("jeg:chests/valen/research_cache",0)!=1){contract=false;diagnostics.add(note("counts","cells, props, meta, chests, barrel, bedrock, or editor differ"));}
  report.add("prop_cells",GSON.toJsonTree(props));report.add("meta_cells",GSON.toJsonTree(meta));report.add("loot",GSON.toJsonTree(loot));report.addProperty("decoded_chests",decoded);report.addProperty("empty_barrels",barrels);report.addProperty("source_bedrock_cells",bedrock);report.addProperty("source_editor_cells",editors);
  JsonArray missing=new JsonArray(),builtin=new JsonArray(),rotationFailures=new JsonArray();
  int variants=propertyVariants(server,rotationFailures,missing,builtin);
  report.addProperty("property_variants_checked",variants);report.addProperty("property_variants_expected",PROPERTY_VARIANTS);report.add("rotation_failures",rotationFailures);report.add("builtin_mesh_pseudopaths",builtin);report.addProperty("pseudopaths_required_as_files",false);
  report.add("missing_model_or_texture",missing);report.addProperty("unreferenced_textures","hbm_reinforced_glass_ct.png and hbm_reinforced_lamp_on.png are not referenced by the staged blockstates");
  if(variants!=PROPERTY_VARIANTS||!rotationFailures.isEmpty()){contract=false;if(!rotationFailures.isEmpty())diagnostics.add(rotationFailures.get(0).getAsJsonObject());}
  report.addProperty("steel_pole_particle_texture","jeg:block/hbm_steel_beam");report.addProperty("asset_inventory_source","Loaded JEG mod container client assets; server datapack manager for world data. Inventory only, not native client decode/bake.");report.addProperty("meshes_qualified",false);report.addProperty("worldgen_qualified",false);report.addProperty("main_mod_blocks_are_inputs",true);
  if(!missing.isEmpty())pending.add("missing staged model, mesh, or texture resources");
  if(!proposal(server,pending,diagnostics))contract=false;passed=contract&&pending.isEmpty();
  finish(server,contract,pending,diagnostics);
 }
 private static boolean proposal(MinecraftServer server,JsonArray pending,JsonArray diagnostics){
  String structure=text(server,"jeg:worldgen/structure/valen/desert_atom_complex.json"),set=text(server,"jeg:worldgen/structure_set/valen/desert_atom_complex.json"),pool=text(server,"jeg:worldgen/template_pool/valen/desert_atom_complex.json"),biomes=text(server,"jeg:tags/worldgen/biome/has_structure/valen/desert_atom_complex.json"),processor=text(server,"jeg:worldgen/processor_list/valen/preserve_bedrock.json");
  if(structure==null||set==null||pool==null||biomes==null||processor==null){pending.add("desert structure, set, pool, biome tag, or preserve_bedrock resource missing");return true;}
  boolean fields=JsonParser.parseString(structure).getAsJsonObject().has("liquid_settings")&&JsonParser.parseString(structure).getAsJsonObject().get("liquid_settings").getAsString().equals("ignore_waterlogging")&&(structure.contains("\"absolute\": -4")||structure.contains("\"absolute\":-4"))&&structure.contains("WORLD_SURFACE_WG")&&set.contains("1728102405")&&pool.contains("jeg:valen/preserve_bedrock")&&biomes.contains("minecraft:desert")&&processor.contains("features_cannot_replace");
  if(!fields)diagnostics.add(note("worldgen","staged proposal fields differ"));
  report.addProperty("heightmap_proposal_absolute",-4);report.addProperty("template_ground_y",4);report.addProperty("source_ground_y",-1);report.addProperty("placement_equivalence_claimed",false);
  report.addProperty("placement_note","Source y -1 is template y 4. Absolute -4 with WORLD_SURFACE_WG is the staged proposal. No placed origin has been shown to equal that rule.");
  return fields;
 }
 private static void finish(MinecraftServer server,boolean contract,JsonArray pending,JsonArray diagnostics){
  report.add("diagnostics",diagnostics);report.add("pending",pending);report.addProperty("unknown_state_policy","null quarantine; never air");report.addProperty("loot_unpacked",false);report.addProperty("cache_item_pool_rolls",0);report.addProperty("ghoul_activation",false);
  if(!contract)report.addProperty("status","fail");else if(!pending.isEmpty())report.addProperty("status","pending");else report.addProperty("status","pass");
  try{java.nio.file.Files.writeString(server.getServerDirectory().resolve("valen-desert-complex-raw-preflight.json"),GSON.toJson(report)+"\n");}catch(IOException error){throw new IllegalStateException(error);}
  if(!report.get("status").getAsString().equals("pass")){String detail=report.has("rotation_failures")&&!report.getAsJsonArray("rotation_failures").isEmpty()?report.getAsJsonArray("rotation_failures").get(0).toString():"";throw new IllegalStateException("Desert complex raw preflight "+report.get("status").getAsString()+" "+detail);}
 }
 private static Map<String,String> tagPalette(CompoundTag tag,int index){Map<String,String> values=new LinkedHashMap<>();CompoundTag raw=(CompoundTag)tag.getListOrEmpty("palette").get(index);CompoundTag props=raw.getCompoundOrEmpty("properties");for(String key:props.keySet())values.put(key,props.getStringOr(key,""));return values;}
 private static boolean packed(RandomizableContainerBlockEntity container,MinecraftServer server){
  // getItem/isEmpty can unpack a live loot table. Metadata save does not open the inventory.
  CompoundTag saved=container.saveWithFullMetadata(server.registryAccess());
  return container.getLootTable()!=null&&saved.contains("LootTable")&&!saved.contains("Items");
 }
 private static BlockState resolve(CompoundTag raw,JsonObject row,JsonArray diagnostics){
  String text=raw.getStringOr("id","");row.addProperty("id",text);if(text.isEmpty()||raw.contains("Name")||raw.contains("Properties")){String error="missing id or legacy Name/Properties";row.addProperty("state","unresolved");row.addProperty("error",error);diagnostics.add(note(text,error));return null;}
  Identifier id;try{id=Identifier.parse(text);}catch(RuntimeException error){row.addProperty("state","unresolved");row.addProperty("error",error.toString());diagnostics.add(note(text,error.toString()));return null;}
  if(!BuiltInRegistries.BLOCK.containsKey(id)){String error="block id absent from the live registry";row.addProperty("state","unresolved");row.addProperty("error",error);diagnostics.add(note(text,error));return null;}
  var block=BuiltInRegistries.BLOCK.getValue(id);BlockState state=block.defaultBlockState();CompoundTag props=raw.getCompoundOrEmpty("properties");
  for(String key:props.keySet()){String value=props.getStringOr(key,"");var property=block.getStateDefinition().getProperty(key);BlockState next=property==null?null:apply(state,property,value);if(next==null){String error="property "+key+" value "+value+" is outside the live schema for "+state;row.addProperty("state",state.toString());row.addProperty("error",error);diagnostics.add(note(text+"."+key,error));return null;}state=next;}
  row.addProperty("state",state.toString());return state;
 }
 private static int propertyVariants(MinecraftServer server,JsonArray failures,JsonArray missing,JsonArray builtin){
  int checked=0;Set<String> required=new LinkedHashSet<>(),pseudo=new LinkedHashSet<>();
  for(String name:BLOCKSTATES){
   String body=text(server,"jeg:blockstates/"+name+".json");if(body==null){failures.add(failure(name,"","blockstate resource missing",null));continue;}
   JsonObject variants;try{variants=JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("variants");}catch(RuntimeException error){failures.add(failure(name,"",error.toString(),null));continue;}
   if(variants==null){failures.add(failure(name,"","blockstate has no variants object",null));continue;}
   for(var entry:variants.entrySet()){
    checked++;String key=entry.getKey();JsonObject spec=entry.getValue().getAsJsonObject();BlockState state=null;
    try{state=variantState(name,key);for(Rotation rotation:Rotation.values()){BlockState turned=state.rotate(rotation);turn(state,turned,rotation);if(state.getBlock().getStateDefinition().getProperty("north")!=null){BlockState fence=fenceTurn(state,rotation);if(!turned.equals(fence))throw new IllegalStateException("native fence rotate "+rotation+" expected "+fence+" actual "+turned);}}resources(server,spec,required,pseudo);}
    catch(Throwable error){failures.add(failure(name+"["+key+"]",state==null?"":state.toString(),error.toString(),error));}
   }
  }
  for(String path:pseudo)builtin.add(path);
  for(String path:required)if(!pseudo.contains(path)&&textOrBinaryMissing(server,path))missing.add(path);
  return checked;
 }
 private static void resources(MinecraftServer server,JsonObject spec,Set<String> required,Set<String> pseudo){
  if(spec.has("model")){String model=spec.get("model").getAsString();required.add(modelFile(model));String modelText=text(server,modelFile(model));if(modelText!=null)textures(modelText,required);return;}
  if(!spec.has("fabric:type"))return;
  String kind=spec.get("kind").getAsString(),mesh=spec.get("mesh").getAsString(),particle=spec.get("particle").getAsString();
  required.add(modelFile(particle));String particleText=text(server,modelFile(particle));if(particleText!=null)textures(particleText,required);
  if(BUILTIN_MESH_KINDS.contains(kind))pseudo.add(mesh);
  else if(kind.equals("wavefront")){required.add(mesh);required.add(textureFile(particle.replace("_particle","")));}
  else throw new IllegalStateException("unknown mesh kind "+kind+" mesh "+mesh);
  if(kind.equals("pole_top"))required.add(textureFile("jeg:block/hbm_pole_top"));
  if(kind.equals("satellite"))required.add(textureFile("jeg:block/hbm_satellite_receiver"));
  if(kind.equals("fence")){required.add(textureFile("jeg:block/hbm_metal_fence"));required.add(textureFile("jeg:block/hbm_metal_fence_post"));}
 }
 private static void textures(String modelText,Set<String> required){JsonObject textures=JsonParser.parseString(modelText).getAsJsonObject().getAsJsonObject("textures");if(textures==null)return;for(var entry:textures.entrySet())required.add(textureFile(entry.getValue().getAsString()));}
 private static BlockState variantState(String name,String key){
  Identifier id=Identifier.fromNamespaceAndPath("jeg",name);if(!BuiltInRegistries.BLOCK.containsKey(id))throw new IllegalStateException("block id absent "+id);
  var block=BuiltInRegistries.BLOCK.getValue(id);BlockState state=block.defaultBlockState();if(key.isEmpty())return state;
  for(String part:key.split(",")){String[] pair=part.split("=",2);if(pair.length!=2)throw new IllegalStateException("malformed variant "+key);var property=block.getStateDefinition().getProperty(pair[0]);if(property==null)throw new IllegalStateException("property "+pair[0]+" absent on "+state);BlockState next=apply(state,property,pair[1]);if(next==null)throw new IllegalStateException("value "+pair[1]+" outside schema of "+state);state=next;}
  return state;
 }
 private static void turn(BlockState source,BlockState turned,Rotation rotation){
  Identifier id=BuiltInRegistries.BLOCK.getKey(source.getBlock());
  if(!id.getNamespace().equals("jeg")||!id.getPath().startsWith("hbm_"))return; // This preservation contract covers HBM metadata; vanilla directional states use native rotation.
  Integer meta=integer(source,"meta"),turnedMeta=integer(turned,"meta"),rot=integer(source,"rotation"),turnedRot=integer(turned,"rotation");
  if(meta!=null&&!meta.equals(turnedMeta))throw new IllegalStateException("meta changed under "+rotation+" source "+source+" turned "+turned);
  if(rot!=null){int expected=HbmQuarterTurn.compose(rot,rotation);if(turnedRot==null||turnedRot!=expected)throw new IllegalStateException("rotation quarter turn under "+rotation+" expected "+expected+" source "+source+" turned "+turned);}
  if(meta==null&&rot==null&&source.getBlock().getStateDefinition().getProperty("north")==null&&!source.equals(turned))throw new IllegalStateException("unoriented state changed under "+rotation+" source "+source+" turned "+turned);
 }
 private static Integer integer(BlockState state,String name){var property=state.getBlock().getStateDefinition().getProperty(name);if(!(property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProperty))return null;return state.getValue(intProperty);}
 private static boolean flag(BlockState state,String name){var property=state.getBlock().getStateDefinition().getProperty(name);if(!(property instanceof BooleanProperty boolProperty))throw new IllegalStateException(state+" missing "+name);return state.getValue(boolProperty);}
 private static BlockState fenceTurn(BlockState state,Rotation rotation){
  boolean north=flag(state,"north"),south=flag(state,"south"),east=flag(state,"east"),west=flag(state,"west");
  return switch(rotation){
   case CLOCKWISE_90->put(put(put(put(state,"north",west),"east",north),"south",east),"west",south);
   case CLOCKWISE_180->put(put(put(put(state,"north",south),"east",west),"south",north),"west",east);
   case COUNTERCLOCKWISE_90->put(put(put(put(state,"north",east),"east",south),"south",west),"west",north);
   default->state;
  };
 }
 private static BlockState put(BlockState state,String name,boolean value){var property=state.getBlock().getStateDefinition().getProperty(name);if(!(property instanceof BooleanProperty boolProperty))throw new IllegalStateException(state+" missing "+name);return state.setValue(boolProperty,value);}
 private static String modelFile(String model){Identifier id=Identifier.parse(model);return id.getNamespace()+":models/"+id.getPath()+".json";}
 private static String textureFile(String texture){Identifier id=Identifier.parse(texture);return id.getNamespace()+":textures/"+id.getPath()+".png";}
 private static JsonObject failure(String variant,String state,String error,Throwable thrown){JsonObject row=new JsonObject();row.addProperty("variant",variant);row.addProperty("state",state);row.addProperty("error",thrown==null?error:thrown.toString());return row;}
 private static <T extends Comparable<T>> BlockState apply(BlockState state,Property<T> property,String value){var parsed=property.getValue(value);return parsed.isEmpty()?null:state.setValue(property,parsed.get());}
 private static byte[] resourceBytes(MinecraftServer server,Identifier id){
  // Dedicated-server data packs do not expose client assets. Inventory the actual loaded mod container instead.
  String path=id.getPath();
  if(path.startsWith("blockstates/")||path.startsWith("models/")||path.startsWith("textures/")){
   var container=net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(id.getNamespace()).orElseThrow();
   var asset=container.findPath("assets/"+id.getNamespace()+"/"+path).orElseThrow(()->new IllegalStateException("Missing loaded mod asset "+id));
   try{return java.nio.file.Files.readAllBytes(asset);}catch(IOException error){throw new IllegalStateException(error);}
  }
  var found=server.getResourceManager().getResource(id).orElseThrow(()->new IllegalStateException("Missing "+id));
  try(var in=found.open()){return in.readAllBytes();}catch(IOException error){throw new IllegalStateException(error);}
 }
 private static boolean textOrBinaryMissing(MinecraftServer server,String path){try{resourceBytes(server,resource(path));return false;}catch(RuntimeException error){return true;}}
 private static String text(MinecraftServer server,String id){try{return new String(resourceBytes(server,resource(id)),StandardCharsets.UTF_8);}catch(RuntimeException error){return null;}}
 private static Identifier resource(String text){String[] parts=text.split(":",2);return Identifier.fromNamespaceAndPath(parts[0],parts[1]);}
 private static CompoundTag read(byte[] bytes){try(var in=new ByteArrayInputStream(bytes)){return NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}catch(IOException error){throw new IllegalStateException(error);}}
 private static String sha(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(NoSuchAlgorithmException error){throw new IllegalStateException(error);}}
 private static JsonObject note(String state,String detail){JsonObject row=new JsonObject();row.addProperty("state",state);row.addProperty("detail",detail);return row;}
}
