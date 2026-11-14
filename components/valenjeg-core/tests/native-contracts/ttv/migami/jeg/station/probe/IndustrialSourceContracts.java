package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.LoggerFactory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
/** Fixture-only SERVER_STARTING observer. No StructureTemplate load and no world placement.
 * Native natural search stays on the existing structure probe; this class does not generate. */
public final class IndustrialSourceContracts {
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Set<String> SPAWNER_ENTITIES=Set.of("minecraft:zombie","minecraft:zombie_villager");
 private record Spec(String id,String sha,int[] size,int cells,int palette,Map<String,Integer> spawners,Map<String,Integer> loot){}
 private static boolean passed;
 private static final Map<String,JsonObject> markers=new LinkedHashMap<>();
 public static void requirePassed(){check(passed,"Industrial SERVER_STARTING preflight must pass before native search");}
 public static JsonObject expectedMarkers(String id){requirePassed();JsonObject row=markers.get(id);check(row!=null,"No passing industrial marker row "+id);return row.deepCopy();}
 private static final List<Spec> SPECS=List.of(
  new Spec("jeg:valen_industrial/warehouse","76fbfa91f53059ced9581f49d7da16408319bddb5da5fa7beb27436d47d2965a",new int[]{20,28,18},10080,102,Map.of("minecraft:zombie",1,"minecraft:zombie_villager",1),Map.of("jeg:chests/valen_industrial/copper_storage",1)),
  new Spec("jeg:valen_industrial/train_station","5ad98bcf4eae732ec0e936bfee34d956d0b3e122487efc6b6b2d8850740fa3d6",new int[]{17,10,19},1347,56,Map.of(),Map.of("minecraft:chests/village/village_taiga_house",1,"minecraft:chests/abandoned_mineshaft",2)));
 private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 private static String sha(byte[] data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private static void guard(MinecraftServer server)throws IOException{
  check(Boolean.getBoolean("valen.industrial.probe")&&Boolean.getBoolean("valen.station.probe")&&server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())&&Files.exists(FIXTURE.resolve("../preparation.json")),"Refusing industrial preflight outside root-staged copied fixture");
 }
 public static void register(){
  if(!Boolean.getBoolean("valen.industrial.probe"))return;
  ServerLifecycleEvents.SERVER_STARTING.register(IndustrialSourceContracts::preflight);
 }
 private static JsonObject note(String subject,String problem){JsonObject row=new JsonObject();row.addProperty("subject",subject);row.addProperty("problem",problem);row.addProperty("state","null");row.addProperty("air_substitution",false);return row;}
 @SuppressWarnings({"unchecked","rawtypes"})
 private static BlockState apply(BlockState state,Property property,String value){var parsed=property.getValue(value);return parsed.isPresent()?state.setValue(property,(Comparable)parsed.get()):null;}
 private static <T extends Comparable<T>> JsonArray propertyValues(Property<T> property){JsonArray values=new JsonArray();for(T value:property.getPossibleValues())values.add(property.getName(value));return values;}
 private static BlockState resolve(CompoundTag raw,JsonObject row,JsonArray diagnostics){
  String text=raw.getStringOr("id","");row.addProperty("id",text);JsonArray sourceProps=new JsonArray();var props=raw.getCompoundOrEmpty("properties");
  for(String key:props.keySet()){JsonObject prop=new JsonObject();prop.addProperty("name",key);prop.addProperty("value",props.getStringOr(key,""));sourceProps.add(prop);}row.add("source_properties",sourceProps);
  if(text.isEmpty()||raw.contains("Name")||raw.contains("Properties")){diagnostics.add(note(text,"missing id or legacy Name/Properties; quarantined, not air"));row.addProperty("state","null");return null;}
  Identifier id;try{id=Identifier.parse(text);}catch(RuntimeException error){diagnostics.add(note(text,error.toString()));row.addProperty("state","null");return null;}
  if(!BuiltInRegistries.BLOCK.containsKey(id)){diagnostics.add(note(text,"block id absent from the live registry; quarantined, not air"));row.addProperty("state","null");return null;}
  var block=BuiltInRegistries.BLOCK.getValue(id);row.addProperty("block_class",block.getClass().getName());JsonArray schema=new JsonArray();
  for(Property<?> property:block.getStateDefinition().getProperties()){JsonObject prop=new JsonObject();prop.addProperty("name",property.getName());prop.addProperty("class",property.getClass().getName());JsonArray values=propertyValues(property);prop.add("values",values);schema.add(prop);}
  row.add("property_schema",schema);BlockState state=block.defaultBlockState();boolean resolved=true;
  for(String key:props.keySet()){String value=props.getStringOr(key,"");var property=block.getStateDefinition().getProperty(key);BlockState next=property==null?null:apply(state,property,value);if(next==null){resolved=false;diagnostics.add(note(text+"."+key,"property absent or value "+value+" is not in the live schema; quarantined, not air"));}else state=next;}
  row.addProperty("state",resolved?"resolved":"null");return resolved?state:null;
 }
 private static boolean legacyItem(CompoundTag n){
  if(n.contains("Count")||n.contains("tag")||n.contains("components"))return true;
  for(String key:List.of("Item","HeldItem")){var child=n.getCompoundOrEmpty(key);if(child.contains("id")||child.contains("Count")||child.contains("tag")||child.contains("components"))return true;}
  for(Tag tag:n.getListOrEmpty("Items"))if(tag instanceof CompoundTag item&&(item.contains("Count")||item.contains("tag")||item.contains("components")))return true;
  for(Tag tag:n.getCompoundOrEmpty("Inventory").getListOrEmpty("Items"))if(tag instanceof CompoundTag item&&(item.contains("Count")||item.contains("tag")||item.contains("components")))return true;
  return !n.getCompoundOrEmpty("Material").isEmpty();
 }
 private static boolean stackMatches(CompoundTag source,CompoundTag saved,HolderLookup.Provider lookup,JsonArray problems,String label){
  for(String key:source.keySet())if(!Set.of("id","Count","count","Slot","components").contains(key)){problems.add(label+" unhandled item field "+key);return false;}
  String id=source.getStringOr("id","");int count=source.contains("Count")?source.getIntOr("Count",-1):source.getIntOr("count",-1);String savedId=saved.getStringOr("id","");int savedCount=saved.getIntOr("count",saved.getIntOr("Count",savedId.isEmpty()?-1:1));
  if(id.isEmpty()||count<1||!id.equals(savedId)||count!=savedCount){problems.add(label+" id/count "+id+" x"+count+" saved "+savedId+" x"+savedCount);return false;}
  if(source.contains("components")||saved.contains("components")){var ops=RegistryOps.create(NbtOps.INSTANCE,lookup);var parsedSource=ItemStack.CODEC.parse(ops,source);var parsedSaved=ItemStack.CODEC.parse(ops,saved);if(parsedSource.result().isEmpty()||parsedSaved.result().isEmpty()||parsedSource.result().get().getCount()!=count||!parsedSource.result().get().getComponents().equals(parsedSaved.result().get().getComponents())){problems.add(label+" components did not match the saved stack");return false;}}
  return true;
 }
 private static boolean listMatches(ListTag source,ListTag saved,HolderLookup.Provider lookup,JsonArray problems,String label){
  if(source.size()!=saved.size()){problems.add(label+" length "+source.size()+" saved "+saved.size());return false;}
  boolean matched=true;for(int i=0;i<source.size();i++){if(!(source.get(i) instanceof CompoundTag from)||!(saved.get(i) instanceof CompoundTag to)||!stackMatches(from,to,lookup,problems,label+"["+i+"]"))matched=false;}return matched;
 }
 private static boolean materialMatches(CompoundTag source,CompoundTag savedRoot,HolderLookup.Provider lookup,JsonArray problems){
  var ops=RegistryOps.create(NbtOps.INSTANCE,lookup);var parsed=BlockState.CODEC.parse(ops,source);
  if(parsed.result().isEmpty()){problems.add("source Material did not parse, including its properties");return false;}
  BlockState expected=parsed.result().get();CompoundTag savedCompound=savedRoot.getCompoundOrEmpty("Material");String savedString=savedRoot.getStringOr("Material","");BlockState actual;
  if(!savedCompound.isEmpty()){var savedParsed=BlockState.CODEC.parse(ops,savedCompound);if(savedParsed.result().isEmpty()){problems.add("saved Material compound did not parse");return false;}actual=savedParsed.result().get();}
  else if(!savedString.isEmpty()){try{Identifier id=Identifier.parse(savedString);if(!BuiltInRegistries.BLOCK.containsKey(id)){problems.add("saved Material id absent "+savedString);return false;}actual=BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();}catch(RuntimeException error){problems.add(error.toString());return false;}}
  else{problems.add("saved Material missing");return false;}
  if(!expected.equals(actual)){problems.add("Material state differs; properties were not dropped");return false;}
  return true;
 }
 private static JsonObject legacyCompare(CompoundTag source,BlockEntity loaded,HolderLookup.Provider lookup){
  JsonObject row=new JsonObject();row.addProperty("attempted_loadStatic",true);JsonArray problems=new JsonArray();int examined=0;boolean accepted=true;
  CompoundTag saved;try{saved=loaded.saveWithFullMetadata(lookup);}catch(Throwable error){row.addProperty("accepted",false);row.addProperty("save_error",error.toString());return row;}
  for(String field:List.of("Item","HeldItem","Material")){if(source.contains(field))row.addProperty("source_"+field,source.get(field).toString());if(saved.contains(field))row.addProperty("saved_"+field,saved.get(field).toString());}
  if(source.contains("Count")||source.contains("tag")||source.contains("components")){examined++;accepted=false;problems.add("top-level legacy item fields are not a validated item stack");}
  for(String key:List.of("Item","HeldItem")){CompoundTag item=source.getCompoundOrEmpty(key);if(item.isEmpty()||!(item.contains("Count")||item.contains("tag")||item.contains("id")||item.contains("components")))continue;examined++;if(!stackMatches(item,saved.getCompoundOrEmpty(key),lookup,problems,key))accepted=false;}
  ListTag items=source.getListOrEmpty("Items");if(!items.isEmpty()){examined++;if(!listMatches(items,saved.getListOrEmpty("Items"),lookup,problems,"Items"))accepted=false;}
  ListTag inventory=source.getCompoundOrEmpty("Inventory").getListOrEmpty("Items");if(!inventory.isEmpty()){examined++;if(!listMatches(inventory,saved.getCompoundOrEmpty("Inventory").getListOrEmpty("Items"),lookup,problems,"Inventory.Items"))accepted=false;}
  CompoundTag material=source.getCompoundOrEmpty("Material");if(!material.isEmpty()){examined++;if(!materialMatches(material,saved,lookup,problems))accepted=false;}
  if(examined==0){accepted=false;problems.add("legacy payload was detected but no id, count, components, or material was validated");}
  row.add("problems",problems);row.addProperty("accepted",accepted&&problems.isEmpty());return row;
 }
 private static void loot(MinecraftServer server,String id,JsonArray diagnostics){
  try{
   Identifier parsed=Identifier.parse(id);Identifier resource=Identifier.fromNamespaceAndPath(parsed.getNamespace(),"loot_table/"+parsed.getPath()+".json");
   var selected=server.getResourceManager().getResource(resource).orElseThrow(()->new AssertionError("Loot resource missing "+resource));
   String json;try(var in=selected.open()){json=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
   LootTable.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,server.registryAccess()),JsonParser.parseString(json)).getOrThrow();
   if(server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,parsed))==LootTable.EMPTY)throw new AssertionError("Native loot registry missed "+id);
  }catch(Throwable error){diagnostics.add(note(id,"loot resource or native parse failed: "+error));}
 }
 private static void preflight(MinecraftServer server){
  JsonObject report=new JsonObject();JsonArray templates=new JsonArray(),pending=new JsonArray(),diagnostics=new JsonArray();report.add("templates",templates);report.add("pending",pending);report.add("diagnostics",diagnostics);
  boolean fixtureGuardPassed=false;
  try{
   guard(server);fixtureGuardPassed=true;var lookup=server.registryAccess();Set<String> lootSeen=new LinkedHashSet<>();Map<String,JsonObject> built=new LinkedHashMap<>();boolean contract=true;
   for(Spec spec:SPECS){
    JsonObject row=new JsonObject();templates.add(row);row.addProperty("id",spec.id());JsonArray paletteRows=new JsonArray(),pairs=new JsonArray();row.add("palette",paletteRows);row.add("block_entity_pairs",pairs);
    Identifier template=Identifier.parse(spec.id());Identifier resource=Identifier.fromNamespaceAndPath(template.getNamespace(),"structure/"+template.getPath()+".nbt");
    var found=server.getResourceManager().getResource(resource);
    if(found.isEmpty()){contract=false;diagnostics.add(note(spec.id(),"staged structure resource missing"));continue;}
    byte[] bytes;try(var in=found.get().open()){bytes=in.readAllBytes();}String actual=sha(bytes);
    row.addProperty("selected_pack",found.get().sourcePackId());row.addProperty("raw_sha256",actual);if(!actual.equals(spec.sha())){contract=false;diagnostics.add(note(spec.id(),"staged resource hash differs"));}
    CompoundTag tag;try(var in=new ByteArrayInputStream(bytes)){tag=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
    var size=tag.getListOrEmpty("size");if(size.size()!=3){contract=false;diagnostics.add(note(spec.id(),"missing raw size"));}else for(int i=0;i<3;i++)if(((NumericTag)size.get(i)).intValue()!=spec.size()[i]){contract=false;diagnostics.add(note(spec.id(),"raw dimensions differ"));}
    if(!tag.getListOrEmpty("entities").isEmpty()){contract=false;diagnostics.add(note(spec.id(),"withheld template entities reappeared"));}
    var paletteTag=tag.getListOrEmpty("palette");if(paletteTag.size()!=spec.palette()){contract=false;diagnostics.add(note(spec.id(),"palette count differs"));}
    List<BlockState> palette=new ArrayList<>();int quarantined=0;
    for(Tag entry:paletteTag){JsonObject paletteRow=new JsonObject();paletteRows.add(paletteRow);if(!(entry instanceof CompoundTag raw)){quarantined++;palette.add(null);diagnostics.add(note(spec.id(),"malformed palette entry"));paletteRow.addProperty("state","null");continue;}BlockState resolved=resolve(raw,paletteRow,diagnostics);palette.add(resolved);if(resolved==null)quarantined++;}
    int cells=0,decoded=0,failedCells=0,skipped=0,legacyRejected=0,emptyCreate=0,emptyVanilla=0,createEntities=0;Map<String,Integer> spawners=new LinkedHashMap<>(),lootCounts=new LinkedHashMap<>();
    for(Tag entry:tag.getListOrEmpty("blocks")){
     if(!(entry instanceof CompoundTag block)){contract=false;diagnostics.add(note(spec.id(),"malformed source block"));continue;}
     int index=block.getIntOr("state",-1);cells++;
     if(index<0||index>=palette.size()){contract=false;failedCells++;diagnostics.add(note(spec.id(),"palette index outside palette"));continue;}
     BlockState blockState=palette.get(index);CompoundTag nbt=block.getCompoundOrEmpty("nbt");
     if(blockState==null){failedCells++;if(!nbt.isEmpty())skipped++;continue;}
     if(nbt.isEmpty())continue;
     String rawId=nbt.getStringOr("id","");
     if(rawId.isEmpty()){skipped++;diagnostics.add(note(spec.id(),"block entity missing id"));continue;}
     Identifier beId;try{beId=Identifier.parse(rawId);}catch(RuntimeException error){skipped++;diagnostics.add(note(rawId,error.toString()));continue;}
     if(!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(beId)){skipped++;pending.add(spec.id()+" "+rawId+" absent from BLOCK_ENTITY_TYPE; machine retained, no substitute id");continue;}
     var type=BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(beId);boolean valid=type.isValid(blockState);JsonObject pair=new JsonObject();pair.addProperty("block",BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());pair.addProperty("block_class",blockState.getBlock().getClass().getName());pair.addProperty("block_entity",rawId);pair.addProperty("valid",valid);pairs.add(pair);
     if(!valid){skipped++;diagnostics.add(note(rawId,"block entity is not valid for "+pair.get("block").getAsString()));continue;}
     if(rawId.equals("minecraft:mob_spawner")){String entity=nbt.getCompoundOrEmpty("SpawnData").getCompoundOrEmpty("entity").getStringOr("id","");if(!SPAWNER_ENTITIES.contains(entity)||!BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(entity))){contract=false;diagnostics.add(note(entity,"spawner entity outside the retained source set"));}else spawners.merge(entity,1,Integer::sum);}
     if(nbt.contains("LootTable")){String table=nbt.getStringOr("LootTable","");lootCounts.merge(table,1,Integer::sum);lootSeen.add(table);}
     BlockEntity loaded;String fieldLoss=null;
     try{loaded=type.create(BlockPos.ZERO,blockState);}catch(Throwable error){loaded=null;pending.add(spec.id()+" "+rawId+" type.create threw "+error);}
     if(loaded==null){skipped++;pending.add(spec.id()+" "+rawId+" native type.create returned null; source NBT retained");continue;}
     try(var reporter=new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(rawId),LoggerFactory.getLogger(IndustrialSourceContracts.class))){
      loaded.loadWithComponents(TagValueInput.create(reporter,lookup,nbt));
      if(!reporter.isEmpty())fieldLoss=reporter.getReport();
     }catch(Throwable error){skipped++;pending.add(spec.id()+" "+rawId+" loadWithComponents threw "+error);continue;}
     if(fieldLoss!=null){skipped++;pending.add(spec.id()+" "+rawId+" native field decode loss; nonnull load is not lossless: "+fieldLoss);continue;}
     if(legacyItem(nbt)){JsonObject compared=legacyCompare(nbt,loaded,lookup);compared.addProperty("block_entity",rawId);if(!row.has("legacy_payloads"))row.add("legacy_payloads",new JsonArray());row.getAsJsonArray("legacy_payloads").add(compared);if(!compared.get("accepted").getAsBoolean()){legacyRejected++;skipped++;pending.add(spec.id()+" "+rawId+" legacy Count/tag or material did not match saved native item/material");continue;}}
     decoded++;if(rawId.startsWith("create:"))createEntities++;
     if(loaded instanceof RandomizableContainerBlockEntity container&&nbt.contains("LootTable")){var key=container.getLootTable();if(key==null||!key.identifier().toString().equals(nbt.getStringOr("LootTable",""))){contract=false;diagnostics.add(note(rawId,"native loot id differs"));}}
     if(loaded instanceof Container container&&!nbt.contains("LootTable")){boolean empty=true;for(int slot=0;slot<container.getContainerSize();slot++)if(!container.getItem(slot).isEmpty()){empty=false;contract=false;diagnostics.add(note(rawId,"native container invented items for an empty source"));break;}if(empty){if(rawId.startsWith("create:"))emptyCreate++;else if(rawId.startsWith("minecraft:"))emptyVanilla++;else{contract=false;diagnostics.add(note(rawId,"native container namespace was not counted"));}}}
    }
    if(cells!=spec.cells()||!spawners.equals(spec.spawners())||!lootCounts.equals(spec.loot())){contract=false;diagnostics.add(note(spec.id(),"cell, spawner, or loot counts differ from the staged contract"));}
    row.addProperty("cells",cells);row.addProperty("palette_entries",palette.size());row.addProperty("quarantined_palette_states",quarantined);row.addProperty("failed_state_cells",failedCells);row.addProperty("skipped_block_entity_decodes",skipped);row.addProperty("legacy_payloads_not_accepted",legacyRejected);row.addProperty("native_be_decoded",decoded);
    JsonObject marker=new JsonObject();marker.addProperty("sha256",actual);marker.addProperty("empty_create_native_containers",emptyCreate);marker.addProperty("empty_minecraft_native_containers",emptyVanilla);marker.addProperty("create_block_entities",createEntities);marker.add("spawners",GSON.toJsonTree(spawners));marker.add("loot",GSON.toJsonTree(lootCounts));built.put(spec.id(),marker);
   }
   for(String id:lootSeen)loot(server,id,diagnostics);
   report.addProperty("phase","SERVER_STARTING actual Fabric registry; one pass over both templates; StructureTemplate and world placement not invoked");
   report.addProperty("unknown_state_policy","null quarantine; never air and never accepted");report.addProperty("unknown_id_air_coercion",false);report.addProperty("registry_unfreeze_or_fake_registration",false);
   boolean unresolved=false;for(JsonElement element:diagnostics)if("null".equals(element.getAsJsonObject().get("state").getAsString()))unresolved=true;
   if(!contract||unresolved)report.addProperty("status","fail");
   else if(!pending.isEmpty())report.addProperty("status","pending");
   else{for(JsonElement element:templates){JsonObject row=element.getAsJsonObject();row.add("native_container_markers",built.get(row.get("id").getAsString()));}markers.clear();markers.putAll(built);passed=true;report.addProperty("status","pass");report.addProperty("marker_counts_exposed_only_after_pass",true);}
   if(!report.get("status").getAsString().equals("pass"))throw new IllegalStateException("Industrial raw preflight "+report.get("status").getAsString()+"; no structure placement");
  }catch(Throwable failure){
   if(!report.has("status"))report.addProperty("status","fail");report.addProperty("error",failure.toString());failure.printStackTrace();
   if(fixtureGuardPassed)throw new IllegalStateException("Industrial raw native preflight failed",failure);throw failure instanceof RuntimeException runtime?runtime:new IllegalStateException(failure);
  }finally{if(fixtureGuardPassed)try{Files.writeString(server.getServerDirectory().resolve("valen-industrial-raw-preflight.json"),GSON.toJson(report)+"\n");}catch(Exception e){throw new IllegalStateException(e);}}
 }
 /** Read-only placed comparison. Counts come only from a passing raw preflight. This does not place or strip machines. */
 public static JsonObject readPlaced(ServerLevel level,StructureStart start,String id){
  JsonObject expected=expectedMarkers(id);
  check(start.getPieces().size()==1&&start.getPieces().getFirst() instanceof PoolElementStructurePiece,"Industrial start is not the one authored piece");
  PoolElementStructurePiece piece=(PoolElementStructurePiece)start.getPieces().getFirst();
  check(piece.getElement() instanceof SinglePoolElement single&&single.getTemplateLocation().toString().equals(id),"Placed template is not the authored industrial source");
  Identifier parsed=Identifier.parse(id);Identifier resource=Identifier.fromNamespaceAndPath(parsed.getNamespace(),"structure/"+parsed.getPath()+".nbt");
  var selected=level.getServer().getResourceManager().getResource(resource).orElseThrow(()->new AssertionError("Missing placed source "+resource));
  byte[] bytes;try(var in=selected.open()){bytes=in.readAllBytes();}catch(IOException error){throw new IllegalStateException(error);}
  check(sha(bytes).equals(expected.get("sha256").getAsString()),"Placed source bytes differ from the preflight");
  CompoundTag tag;try(var in=new ByteArrayInputStream(bytes)){tag=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}catch(IOException error){throw new IllegalStateException(error);}
  List<BlockState> palette=new ArrayList<>();JsonArray ignored=new JsonArray();
  for(Tag entry:tag.getListOrEmpty("palette")){JsonObject paletteRow=new JsonObject();check(entry instanceof CompoundTag,"Malformed placed palette");BlockState resolved=resolve((CompoundTag)entry,paletteRow,ignored);check(resolved!=null,"Placed source palette state is quarantined, not air");palette.add(resolved);}
  int emptyCreate=0,emptyVanilla=0,createEntities=0,ignoredEditor=0;Map<String,Integer> spawners=new LinkedHashMap<>(),lootCounts=new LinkedHashMap<>();JsonArray lootRows=new JsonArray(),ignoredEditors=new JsonArray();
  for(Tag entry:tag.getListOrEmpty("blocks")){
   CompoundTag block=(CompoundTag)entry;BlockState expectedState=palette.get(block.getIntOr("state",-1)).rotate(piece.getRotation());
   var local=block.getListOrEmpty("pos");int sourceX=((NumericTag)local.get(0)).intValue(),sourceY=((NumericTag)local.get(1)).intValue(),sourceZ=((NumericTag)local.get(2)).intValue();
   BlockPos world=StructureTemplate.transform(new BlockPos(sourceX,sourceY,sourceZ),Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());
   CompoundTag nbt=block.getCompoundOrEmpty("nbt");if(nbt.isEmpty())continue;
   if(demonstratedEditor(id,expectedState,nbt,sourceX,sourceY,sourceZ)){ignoredEditor++;JsonObject skipped=new JsonObject();skipped.addProperty("source_x",sourceX);skipped.addProperty("source_y",sourceY);skipped.addProperty("source_z",sourceZ);skipped.addProperty("world",world.toString());skipped.addProperty("source_state",expectedState.toString());skipped.addProperty("source_block_entity",nbt.getStringOr("id",""));skipped.addProperty("processor","SinglePoolElement.getSettings adds BlockIgnoreProcessor.STRUCTURE_BLOCK; processBlock returns null");skipped.addProperty("source_nbt_retained",true);skipped.addProperty("air_substitution",false);ignoredEditors.add(skipped);continue;}
   BlockState actualState=level.getBlockState(world);check(actualState.equals(expectedState),"Placed source block at world "+world+" source ["+sourceX+","+sourceY+","+sourceZ+"] expected "+expectedState+" actual "+actualState);
   String rawId=nbt.getStringOr("id","");var be=level.getBlockEntity(world);String actualBe=be==null?"absent":BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString();check(be!=null&&actualBe.equals(rawId),"Placed source block entity at world "+world+" source ["+sourceX+","+sourceY+","+sourceZ+"] expected "+rawId+" actual "+actualBe);
   if(rawId.startsWith("create:"))createEntities++;
   if(rawId.equals("minecraft:mob_spawner")){String entity=be.saveWithFullMetadata(level.registryAccess()).getCompoundOrEmpty("SpawnData").getCompoundOrEmpty("entity").getStringOr("id","");check(SPAWNER_ENTITIES.contains(entity),"Placed spawner entity left the retained source set");spawners.merge(entity,1,Integer::sum);}
   if(be instanceof RandomizableContainerBlockEntity container&&nbt.contains("LootTable")){var key=container.getLootTable();check(key!=null&&key.identifier().toString().equals(nbt.getStringOr("LootTable","")),"Placed loot id differs");lootCounts.merge(key.identifier().toString(),1,Integer::sum);JsonObject row=new JsonObject();row.addProperty("table",key.identifier().toString());lootRows.add(row);}
   if(be instanceof Container container&&!nbt.contains("LootTable")){for(int slot=0;slot<container.getContainerSize();slot++)check(container.getItem(slot).isEmpty(),"Placed empty source container gained items");if(rawId.startsWith("create:"))emptyCreate++;else if(rawId.startsWith("minecraft:"))emptyVanilla++;}
  }
  check(ignoredEditor==(id.equals("jeg:valen_industrial/warehouse")?1:0),"Ignored editor structure blocks "+ignoredEditor+" for "+id+" expected "+(id.equals("jeg:valen_industrial/warehouse")?1:0));
  check(emptyCreate==expected.get("empty_create_native_containers").getAsInt()&&emptyVanilla==expected.get("empty_minecraft_native_containers").getAsInt()&&createEntities==expected.get("create_block_entities").getAsInt(),"Placed native container or Create machine counts differ");
  check(GSON.toJsonTree(spawners).equals(expected.get("spawners"))&&GSON.toJsonTree(lootCounts).equals(expected.get("loot")),"Placed spawner or loot counts differ");
  JsonObject evidence=expected.deepCopy();evidence.addProperty("placed_empty_create_native_containers",emptyCreate);evidence.addProperty("placed_empty_minecraft_native_containers",emptyVanilla);evidence.addProperty("placed_create_block_entities",createEntities);evidence.addProperty("placed_loot",lootCounts.values().stream().mapToInt(Integer::intValue).sum());evidence.add("placed_loot_rows",lootRows);evidence.addProperty("ignored_editor_structure_blocks",ignoredEditor);evidence.add("ignored_editor_structure_blocks_detail",ignoredEditors);evidence.addProperty("machines_removed",false);evidence.addProperty("unknown_ids_coerced",false);return evidence;
 }
 /** Only the warehouse SAVE editor at local [0,1,0]. SinglePoolElement.getSettings always adds BlockIgnoreProcessor.STRUCTURE_BLOCK, whose processBlock returns null for that block. Raw preflight still decodes this NBT. */
 private static boolean demonstratedEditor(String id,BlockState state,CompoundTag nbt,int x,int y,int z){
  return id.equals("jeg:valen_industrial/warehouse")&&x==0&&y==1&&z==0&&state.is(Blocks.STRUCTURE_BLOCK)&&state.getValue(StructureBlock.MODE)==StructureMode.SAVE&&nbt.getStringOr("id","").equals("minecraft:structure_block")&&nbt.getStringOr("mode","").equals("SAVE")&&nbt.getStringOr("name","").equals(id)&&nbt.getIntOr("sizeX",-1)==20&&nbt.getIntOr("sizeY",-1)==28&&nbt.getIntOr("sizeZ",-1)==18;
 }
}
