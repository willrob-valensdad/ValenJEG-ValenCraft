package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
/** Fixture only: actual Fabric registries BEFORE StructureTemplate can substitute unknown names.
 * Source-authored positions and true processor range, never a fabricated fixed random-cache count. */
public final class HangarHelipadContracts {
 private static final String SHA="cc177f59949c1c662498419b7d70bbc881bcefe7bbd4f2643376eb6425545e47";
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static final Map<String,TemplateData> TEMPLATES=new LinkedHashMap<>();
 private static JsonObject source;
 private static boolean passed;
 private record Cell(BlockPos local,BlockState state,CompoundTag nbt){}
 private record TemplateData(String id,List<Cell> cells,JsonObject contract){}
 private record Placed(Cell cell,TemplateData template,PoolElementStructurePiece piece){}
 public record Result(int loot,int emptyBarrels,int inlineStacks,JsonArray lootRows,Set<ResourceKey<LootTable>> lootKeys,JsonObject evidence){}
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 private static String sha(byte[] data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
 public static void register(){
  if(Boolean.getBoolean("valen.structure.probe.hangarHelipadAnchors")||Boolean.getBoolean("valen.structure.probe.hangarHelipad")||Boolean.getBoolean("valen.structure.probe.hangarHelipadResume"))ServerLifecycleEvents.SERVER_STARTING.register(HangarHelipadContracts::preflight);
 }
 public static void requirePassed(){check(passed,"Actual Fabric raw palette preflight must pass BEFORE airfield template loading/generation");}
 private static BlockPos pos(JsonArray a){return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
 private static JsonArray xyz(BlockPos p){JsonArray a=new JsonArray();a.add(p.getX());a.add(p.getY());a.add(p.getZ());return a;}
 private static BlockPos world(BlockPos local,PoolElementStructurePiece piece){return StructureTemplate.transform(local,Mirror.NONE,piece.getRotation(),BlockPos.ZERO).offset(piece.getPosition());}
 private static <T extends Comparable<T>> BlockState property(BlockState state,Property<T> property,String value){var parsed=property.getValue(value);check(parsed.isPresent(),"Invalid property value "+property.getName()+"="+value);return state.setValue(property,parsed.get());}
 private static BlockState state(CompoundTag nbt){
  String raw=nbt.getStringOr("id","");Identifier id=Identifier.parse(raw);
  check(BuiltInRegistries.BLOCK.containsKey(id),"Unknown real BLOCK ID "+raw);
  check(!nbt.contains("Name")&&!nbt.contains("Properties"),"Unconverted legacy palette keys for "+raw);
  BlockState state=BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
  CompoundTag props=nbt.getCompoundOrEmpty("properties");
  for(String key:props.keySet()){
   Property<?> property=state.getBlock().getStateDefinition().getProperty(key);check(property!=null,"Unknown real property "+raw+"."+key);
   state=property(state,property,props.getStringOr(key,""));
  }
  return state;
 }
 private static void itemIds(CompoundTag nbt){
  for(String name:List.of("Items","items"))for(Tag entry:nbt.getListOrEmpty(name)){
   check(entry instanceof CompoundTag,"Malformed native item list");var item=(CompoundTag)entry;
   if(item.contains("item"))item=item.getCompoundOrEmpty("item");
   String raw=item.getStringOr("id","");check(!raw.isEmpty()&&BuiltInRegistries.ITEM.containsKey(Identifier.parse(raw)),"Unknown real inline ITEM ID "+raw);
  }
 }
 private static void preflight(MinecraftServer server){
  JsonObject report=new JsonObject();JsonArray rows=new JsonArray(),errors=new JsonArray();report.add("templates",rows);report.add("errors",errors);
  Path output=server.getServerDirectory().resolve("valen-airfield-raw-palette-preflight.json");
  try{
   check(server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())&&Files.exists(FIXTURE.resolve("../preparation.json"))&&Boolean.getBoolean("valen.structure.probe"),"Refusing raw preflight outside explicitly staged copied fixture");
   byte[] contract;try(var in=HangarHelipadContracts.class.getResourceAsStream("/valen/airfield-source-contracts.json")){check(in!=null,"Missing frozen fixture contract");contract=in.readAllBytes();}
   check(sha(contract).equals(SHA),"Frozen source contract SHA differs");source=JsonParser.parseString(new String(contract,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
   check(source.getAsJsonObject("templates").size()==27&&source.getAsJsonArray("structure_ids").size()==10,"Expected exact27templates/10configured roots");
   int states=0,blockEntities=0,items=0,createStates=0;
   for(var entry:source.getAsJsonObject("templates").entrySet()){
    String id=entry.getKey();JsonObject expected=entry.getValue().getAsJsonObject(),row=new JsonObject();rows.add(row);row.addProperty("id",id);
    try{
     Identifier template=Identifier.parse(id),resource=Identifier.fromNamespaceAndPath(template.getNamespace(),"structure/"+template.getPath()+".nbt");
     var selected=server.getResourceManager().getResource(resource).orElseThrow();byte[] raw;try(var in=selected.open()){raw=in.readAllBytes();}
     row.addProperty("selected_pack",selected.sourcePackId());row.addProperty("actual_raw_sha256",sha(raw));check(sha(raw).equals(expected.get("sha256").getAsString()),"Selected raw NBT differs from converted source "+id);
     CompoundTag tag;try(var in=new ByteArrayInputStream(raw)){tag=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
     var sizes=tag.getListOrEmpty("size");check(sizes.size()==3,"Missing raw size");for(int i=0;i<3;i++)check(((NumericTag)sizes.get(i)).intValue()==expected.getAsJsonArray("size").get(i).getAsInt(),"Raw source dimensions differ");
     check(tag.getListOrEmpty("entities").isEmpty(),"Source creature/entity payload not inert");
     var palettes=new ArrayList<ListTag>();if(!tag.getListOrEmpty("palette").isEmpty())palettes.add(tag.getListOrEmpty("palette"));
     for(Tag palette:tag.getListOrEmpty("palettes")){check(palette instanceof ListTag,"Malformed raw palettes");palettes.add((ListTag)palette);}
     check(!palettes.isEmpty(),"No raw palettes");
     List<BlockState> first=new ArrayList<>();
     for(int paletteIndex=0;paletteIndex<palettes.size();paletteIndex++){
      for(Tag paletteEntry:palettes.get(paletteIndex)){
       check(paletteEntry instanceof CompoundTag,"Malformed palette entry");var rawState=(CompoundTag)paletteEntry;
       try{BlockState actual=state(rawState);if(paletteIndex==0)first.add(actual);states++;if(BuiltInRegistries.BLOCK.getKey(actual.getBlock()).getNamespace().equals("create"))createStates++;}
       catch(Throwable error){errors.add(id+" palette"+paletteIndex+": "+error);if(paletteIndex==0)first.add(null);}
      }
     }
     List<Cell> cells=new ArrayList<>();
     for(Tag rawBlock:tag.getListOrEmpty("blocks")){
      check(rawBlock instanceof CompoundTag,"Malformed source block");CompoundTag block=(CompoundTag)rawBlock;int index=block.getIntOr("state",-1);
      check(index>=0&&index<first.size(),"Source palette index outside palette");var local=block.getListOrEmpty("pos");check(local.size()==3,"Missing local source position");
      BlockPos position=new BlockPos(((NumericTag)local.get(0)).intValue(),((NumericTag)local.get(1)).intValue(),((NumericTag)local.get(2)).intValue());BlockState actual=first.get(index);if(actual==null)continue;
      CompoundTag be=block.getCompoundOrEmpty("nbt");
      try{
       check(!actual.is(Blocks.SPAWNER),"No active source spawners are authorized");
       if(!be.isEmpty()){
        String rawId=be.getStringOr("id","");check(!rawId.isEmpty(),"Source BE fields lack an explicit real ID at "+position);
        Identifier beId=Identifier.parse(rawId);check(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(beId),"Unknown real BLOCK_ENTITY_TYPE "+rawId);
        check(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(beId).isValid(actual),"Real BE type incompatible with block "+rawId+" / "+BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
        blockEntities++;itemIds(be);items+=be.getListOrEmpty("Items").size()+be.getListOrEmpty("items").size();
       }
      }catch(Throwable error){errors.add(id+" block"+position+": "+error);}
      cells.add(new Cell(position,actual,be));
     }
     TEMPLATES.put(id,new TemplateData(id,List.copyOf(cells),expected));row.addProperty("raw_states",first.size());row.addProperty("raw_cells",cells.size());
    }catch(Throwable error){errors.add(id+": "+error);}
   }
   report.addProperty("actual_native_palette_entries",states);report.addProperty("actual_Create_palette_entries",createStates);report.addProperty("actual_native_BE_ids_and_state_pairs",blockEntities);report.addProperty("actual_inline_item_ids",items);
   report.addProperty("phase","SERVER_STARTING actual Fabric registry; no StructureTemplate load invoked");report.addProperty("frozen_contract_sha256",SHA);
   check(errors.isEmpty()&&TEMPLATES.size()==27,"Raw real-registry preflight errors; template fallback is prohibited");
   passed=true;report.addProperty("status","pass");
  }catch(Throwable failure){report.addProperty("status","fail");report.addProperty("error",failure.toString());throw new IllegalStateException("Airfield raw registry preflight failed",failure);}
  finally{try{Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}catch(Exception error){throw new IllegalStateException(error);}}
 }
 private static boolean nativeWallChestRemoved(ServerLevel level,Placed placed,BlockPos location,BlockState expected){
  var encoded=StructurePoolElement.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,level.registryAccess()),placed.piece().getElement()).getOrThrow().getAsJsonObject();
  Identifier processorId=Identifier.parse(encoded.get("processors").getAsString());
  check(Set.of("jeg:valen_hangars/hangar/wall1","jeg:valen_hangars/hangar/wall2","jeg:valen_hangars/hangar/savannah2").contains(processorId.toString()),"Unexpected actual source wall processor "+processorId);
  StructureProcessorList processors=level.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST).getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST,processorId)).value();
  var info=new StructureTemplate.StructureBlockInfo(location,expected,placed.cell().nbt().copy());
  var settings=new StructurePlaceSettings().setRotation(placed.piece().getRotation());
  for(var processor:processors.list()){
   info=processor.processBlock(level,placed.piece().getPosition(),BlockPos.ZERO,placed.cell().local(),info,settings);
   check(info!=null,"Source wall processor unexpectedly discarded chest info");
  }
  check(info.state().isAir()||info.state().is(expected.getBlock()),"Unexpected native source chest-rule output "+info.state());
  return info.state().isAir();
 }
 private static boolean ordinaryRegrowth(ServerLevel level,Map<BlockPos,Placed> sourceCells,BlockPos location,BlockState actual){
  if(!(actual.is(Blocks.FERN)||actual.is(Blocks.SHORT_GRASS)||actual.is(Blocks.BUSH))||level.getBlockEntity(location)!=null)return false;
  Placed floor=sourceCells.get(location.below());
  if(floor==null||!floor.cell().state().is(Blocks.COARSE_DIRT))return false;
  var below=level.getBlockState(location.below());return below.is(Blocks.COARSE_DIRT)||below.is(Blocks.GRASS_BLOCK);
 }
 public static JsonObject requireUntouchedJungle(ServerLevel level,StructureStart start,JsonObject prior){
  requirePassed();check(prior.get("id").getAsString().equals("jeg:valen_hangars/jungle_hangar")&&prior.get("status").getAsString().equals("actual native start placed; source marker count mismatch")&&prior.get("start_chunk_x").getAsInt()==22536&&prior.get("start_chunk_z").getAsInt()==36617&&prior.get("candidates_checked").getAsInt()==27,"Wrong bound original jungle natural-start row");
  check(start.isValid()&&start.getPieces().size()==2,"Original natural jungle start missing");Map<BlockPos,Placed> cells=new LinkedHashMap<>();var oldPieces=prior.getAsJsonArray("pieces");
  for(int i=0;i<2;i++){
   check(start.getPieces().get(i) instanceof PoolElementStructurePiece,"Original jungle native pool piece missing");var piece=(PoolElementStructurePiece)start.getPieces().get(i);check(piece.getElement() instanceof SinglePoolElement,"Original jungle native single pool element missing");var single=(SinglePoolElement)piece.getElement();var old=oldPieces.get(i).getAsJsonObject();
   check(single.getTemplateLocation().toString().equals(old.get("template_id").getAsString())&&piece.getRotation().toString().equals(old.get("rotation").getAsString())&&xyz(piece.getPosition()).equals(old.getAsJsonArray("origin")),"Original jungle template/origin/rotation changed");var bounds=piece.getBoundingBox();JsonArray box=new JsonArray();for(int value:new int[]{bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX(),bounds.maxY(),bounds.maxZ()})box.add(value);check(box.equals(old.getAsJsonArray("bounds")),"Original jungle native piece bounds changed");var encoded=StructurePoolElement.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,level.registryAccess()),single).getOrThrow().getAsJsonObject();check(encoded.get("processors").getAsString().equals(i==0?"jeg:valen_hangars/hangar/wall2":"minecraft:empty"),"Original jungle source processor changed");var data=TEMPLATES.get(single.getTemplateLocation().toString());check(data!=null,"Missing bound original jungle source template");
   for(var cell:data.cells())if(!cell.state().is(Blocks.STRUCTURE_VOID)&&!cell.state().is(Blocks.JIGSAW))cells.put(world(cell.local(),piece),new Placed(cell,data,piece));
  }
  JsonArray inspected=new JsonArray();int count=0;
  for(var entry:cells.entrySet()){
   var placed=entry.getValue();var cell=placed.cell();String table=cell.nbt().getStringOr("LootTable","");if(table.isEmpty())continue;BlockPos location=entry.getKey();var expected=cell.state().rotate(placed.piece().getRotation());
   if(!placed.template().id().contains("/inner")&&nativeWallChestRemoved(level,placed,location,expected))continue;
   check(level.getBlockState(location).is(expected.getBlock())&&level.getBlockEntity(location) instanceof RandomizableContainerBlockEntity,"Original jungle surviving container missing at "+location);var cache=(RandomizableContainerBlockEntity)level.getBlockEntity(location);
   check(cache.getLootTable()!=null&&cache.getLootTable().identifier().toString().equals(table),"Original jungle source loot already consumed/changed at "+location);
   JsonObject row=new JsonObject();row.add("pos",xyz(location));row.addProperty("table",table);row.addProperty("untouched_before_any_unpack",true);inspected.add(row);count++;
  }
  check(count==6,"Exactly six original source/native surviving jungle caches required before any unpack");JsonObject proof=new JsonObject();proof.add("all_six_tables_preread_before_any_unpack",inspected);proof.addProperty("original_natural_grid_attempt",27);proof.addProperty("new_structure_generation_or_forced_start",false);return proof;
 }
 public static Result validate(ServerLevel level,StructureStart start,String structureId){
  requirePassed();boolean hangar=structureId.startsWith("jeg:valen_hangars/");check(start.getPieces().size()==(hangar?2:1),"Wrong actual authored piece graph "+structureId);
  List<PoolElementStructurePiece> pieces=new ArrayList<>();Map<BlockPos,Placed> finalCells=new LinkedHashMap<>();JsonArray pieceRows=new JsonArray();PoolElementStructurePiece wall=null,inner=null;
  for(var actual:start.getPieces()){
   check(actual instanceof PoolElementStructurePiece,"Non-source pool piece");var piece=(PoolElementStructurePiece)actual;check(piece.getElement() instanceof SinglePoolElement,"Non-source pool element");var single=(SinglePoolElement)piece.getElement();
   String templateId=single.getTemplateLocation().toString();TemplateData data=TEMPLATES.get(templateId);check(data!=null,"Actual piece not in frozen27 source "+templateId);pieces.add(piece);
   if(hangar){
    String root=structureId+"/frame/";if(templateId.equals(root+"wall1")||templateId.equals(root+"wall2")){check(wall==null,"Duplicate hangar wall");wall=piece;}
    else{check(templateId.equals("jeg:valen_hangars/plains_hangar/frame/inner1")||templateId.equals(root+"inner2"),"Unexpected inner template "+templateId);check(inner==null,"Duplicate hangar inner");inner=piece;}
   }else check(templateId.equals(structureId),"Helipad selected wrong source template");
   var row=new JsonObject();row.addProperty("template",templateId);row.add("origin",xyz(piece.getPosition()));row.addProperty("rotation",piece.getRotation().toString());pieceRows.add(row);
   for(Cell cell:data.cells()){
    if(cell.state().is(Blocks.STRUCTURE_VOID))continue;
    if(cell.state().is(Blocks.JIGSAW)){
     String finish=cell.nbt().getStringOr("final_state","minecraft:air");
     if(finish.equals("minecraft:structure_void"))continue;
     check(finish.equals("minecraft:air"),"Unreviewed source jigsaw final state "+finish);
     finalCells.put(world(cell.local(),piece),new Placed(new Cell(cell.local(),Blocks.AIR.defaultBlockState(),new CompoundTag()),data,piece));continue;
    }
    finalCells.put(world(cell.local(),piece),new Placed(cell,data,piece));
   }
  }
  check(!hangar||wall!=null&&inner!=null,"Missing native wall or mandatory inner");
  if(hangar){
   BlockPos a=null,b=null;for(Cell c:TEMPLATES.get(((SinglePoolElement)wall.getElement()).getTemplateLocation().toString()).cells())if(c.state().is(Blocks.JIGSAW)&&c.nbt().getStringOr("target","").equals("minecraft:inner"))a=world(c.local(),wall);
   for(Cell c:TEMPLATES.get(((SinglePoolElement)inner.getElement()).getTemplateLocation().toString()).cells())if(c.state().is(Blocks.JIGSAW)&&c.nbt().getStringOr("name","").equals("minecraft:inner"))b=world(c.local(),inner);
   check(a!=null&&b!=null&&a.above().equals(b),"Native wall-to-inner jigsaw positions are not source-adjacent");
  }
  int loot=0,empty=0,inline=0,innerLoot=0,createCells=0,hParts=0;JsonArray lootRows=new JsonArray(),deletedWallChests=new JsonArray();Set<ResourceKey<LootTable>> keys=new LinkedHashSet<>();
  for(var entry:finalCells.entrySet()){
   BlockPos location=entry.getKey();Placed placed=entry.getValue();Cell cell=placed.cell();BlockState expected=cell.state().rotate(placed.piece().getRotation()),actual=level.getBlockState(location);
   Identifier id=BuiltInRegistries.BLOCK.getKey(expected.getBlock());
   if(id.getNamespace().equals("create")){check(actual.getBlock()==expected.getBlock(),"Actual source Create block missing at "+location+" expected "+id);createCells++;}
   if(id.toString().equals("jeg:source_helipad_marker")){check(actual.equals(expected),"Actual source9part H marker state/rotation differs at "+location);hParts++;}
   if(!cell.nbt().contains("id")){
    if(expected.is(Blocks.BARREL)||expected.is(Blocks.CHEST)||expected.is(Blocks.TRAPPED_CHEST)){
     check(actual.getBlock()==expected.getBlock()&&level.getBlockEntity(location) instanceof Container,"Authored no-payload container missing at "+location);
     var actualContainer=(Container)level.getBlockEntity(location);
     check(actualContainer.isEmpty(),"Authored no-payload container must be empty at "+location);
     if(expected.is(Blocks.BARREL))empty++;
    }
    continue;
   }
   String table=cell.nbt().getStringOr("LootTable","");
   if(!table.isEmpty()){
    if(hangar&&!placed.template().id().contains("/inner")&&nativeWallChestRemoved(level,placed,location,expected)){
     check(actual.isAir()||ordinaryRegrowth(level,finalCells,location,actual),"Native source-deleted chest has unsupported replacement at "+location+" in "+placed.template().id()+" actual "+actual+" below "+level.getBlockState(location.below()));
     JsonObject deleted=new JsonObject();deleted.add("pos",xyz(location));deleted.addProperty("actual_state",actual.toString());deleted.addProperty("native_source_rule_replayed",true);deletedWallChests.add(deleted);continue;
    }
    check(actual.is(expected.getBlock()),"Authored non-deleted container missing at "+location+" in "+placed.template().id()+" expected "+expected+" actual "+actual);
    check(level.getBlockEntity(location) instanceof RandomizableContainerBlockEntity,"Authored native loot container lacks BE");
    var cache=(RandomizableContainerBlockEntity)level.getBlockEntity(location);check(cache.getLootTable()!=null&&cache.getLootTable().identifier().toString().equals(table),"Authored source loot table differs at "+location);
    var key=cache.getLootTable();keys.add(key);cache.unpackLootTable(null);check(cache.getLootTable()==null,"Actual native container did not unpack");
    var row=new JsonObject();row.add("pos",xyz(location));row.addProperty("table",table);row.addProperty("source_template",placed.template().id());row.addProperty("loot_key_consumed",true);
    JsonArray items=new JsonArray();for(int slot=0;slot<cache.getContainerSize();slot++)if(!cache.getItem(slot).isEmpty()){var stack=cache.getItem(slot);JsonObject item=new JsonObject();item.addProperty("id",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());item.addProperty("count",stack.getCount());items.add(item);}row.add("actual_generated_items",items);lootRows.add(row);loot++;
    if(placed.template().id().contains("/inner"))innerLoot++;
   }else if(expected.is(Blocks.BARREL)||expected.is(Blocks.CHEST)||expected.is(Blocks.TRAPPED_CHEST)){
    check(actual.getBlock()==expected.getBlock()&&level.getBlockEntity(location) instanceof Container,"Authored inline/empty container missing at "+location);
    var actualContainer=(Container)level.getBlockEntity(location);var decoded=BlockEntity.loadStatic(location,expected,cell.nbt(),level.registryAccess());check(decoded instanceof Container,"Actual registry could not decode source inventory");
    var sourceContainer=(Container)decoded;check(sourceContainer.getContainerSize()==actualContainer.getContainerSize(),"Native source inventory size differs");
    int stacks=0;for(int slot=0;slot<sourceContainer.getContainerSize();slot++){check(ItemStack.matches(sourceContainer.getItem(slot),actualContainer.getItem(slot)),"Native inline item/component/slot differs at "+location);if(!actualContainer.getItem(slot).isEmpty())stacks++;}
    inline+=stacks;if(expected.is(Blocks.BARREL)&&sourceContainer.isEmpty())empty++;
   }
  }
  if(hangar)check(loot>=1&&loot<=13&&innerLoot==1&&empty==0,"Source randomprocessor range or mandatory pulley chest failed");
  else{var expected=source.getAsJsonObject("helipad_contract").getAsJsonObject(structureId);check(loot==expected.get("loot").getAsInt()&&empty==expected.get("empty_barrels").getAsInt()&&inline==expected.get("inline_stacks").getAsInt()&&hParts==9,"Authored helipad loot/empty/inline/H source contract failed");}
  JsonObject evidence=new JsonObject();evidence.add("native_source_pieces",pieceRows);evidence.add("native_source_deleted_wall_chests",deletedWallChests);evidence.addProperty("actual_Create_source_cells",createCells);evidence.addProperty("mandatory_inner_pulley_chests",innerLoot);evidence.addProperty("actual_inline_stacks",inline);evidence.addProperty("actual_H_parts",hParts);
  evidence.addProperty("container_contract",hangar?"Source55/60% wall-chest deletion; surviving authored positions/tables plus mandatory inner; native actual range1..13, NOT fixed cache count":"Exact authored fixed containers/inline native items/H parts");evidence.addProperty("native_marker_counts_match_source",true);
  return new Result(loot,empty,inline,lootRows,keys,evidence);
 }
}
