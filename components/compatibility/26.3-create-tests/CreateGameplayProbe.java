package valen.createprobe;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import io.netty.buffer.Unpooled;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public final class CreateGameplayProbe implements ModInitializer {
 private static final BlockPos MOTOR=new BlockPos(120,222,120), SHAFT=MOTOR.east(), GEAR=MOTOR.east(2), PRESS=MOTOR.east(3), DRIVEN_GEAR=GEAR.south();
 private static final List<Map<String,Object>> checks=new ArrayList<>(), recipeSnapshots=new ArrayList<>();
 private static int ticks;
 private static boolean setup,finished;
 private static float pressPeak;
 private static boolean pressActive;
 private static ItemEntity pressInput;
 private static final Map<String,BlockPos> stores=new LinkedHashMap<>();

 public void onInitialize() {
  if(!Boolean.getBoolean("valen.create.probe"))throw new IllegalStateException("Disposable fixture requires -Dvalen.create.probe=true");
  ServerTickEvents.END_SERVER_TICK.register(server -> {
   if(finished)return;
   try {
    ticks++;
    if(Boolean.getBoolean("valen.create.recipesOnly")) {
     if(ticks==180){recipes(server,"initial");if(Boolean.getBoolean("valen.create.blockDump"))dumpBlockMaterials();if(Boolean.getBoolean("valen.create.cacheProbe"))checks.add(CreateTagCacheProbe.run(server));if(Boolean.getBoolean("valen.create.portalProbe"))checks.add(CreatePortalTrackProbe.run(server));if(Boolean.getBoolean("valen.promenade.probe"))checks.add(PromenadeBoundaryProbe.run(server));finished=true;write(server);System.out.println("CREATE_RECIPE_PROBE_FINISHED");}
     return;
    }
    if(ticks==140){prepare(server.overworld());setup=true;write(server);}
    if(ticks==180){seedAndTransfer(server);recipes(server,"initial");ore(server);write(server);}
    if(setup&&server.overworld().getBlockEntity(PRESS) instanceof MechanicalPressBlockEntity press) {
     pressPeak=Math.max(pressPeak,Math.abs(press.getSpeed()));pressActive|=press.pressingBehaviour.running;
    }
    if(ticks==440){verifyMachines(server);finished=true;write(server);System.out.println("CREATE_GAMEPLAY_PROBE_FINISHED "+checks);}
   }catch(Throwable error){checks.add(Map.of("test","fixture exception at tick"+ticks,"pass",false,"error",error.toString()));error.printStackTrace();finished=true;write(server);}
  });
  ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server,resources,success)->{
   if(ticks<180)return;
   try{check("reload succeeds",success,Map.of());recipes(server,"reload");if(Boolean.getBoolean("valen.create.cacheProbe"))checks.add(CreateTagCacheProbe.run(server));write(server);}
   catch(Throwable error){checks.add(Map.of("test","reload fixture","pass",false,"error",error.toString()));error.printStackTrace();write(server);}
  });
 }
 private static void check(String name,boolean pass,Map<String,Object> detail){
  var row=new LinkedHashMap<String,Object>();row.put("test",name);row.put("pass",pass);row.putAll(detail);checks.add(row);
 }
 private static BlockState state(String id){var b=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));if(b==null||b==Blocks.AIR)throw new IllegalArgumentException(id);return b.defaultBlockState();}
 private static void place(ServerLevel level,BlockPos pos,BlockState state){level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,state);}
 private static void prepare(ServerLevel level){
  for(var e:level.getEntitiesOfClass(ItemEntity.class,new AABB(PRESS).inflate(4)))e.discard();
  for(int x=7;x<=9;x++)for(int z=7;z<=9;z++){level.setChunkForced(x,z,true);level.getChunk(x,z);}
  for(BlockPos p:BlockPos.betweenClosed(118,220,118,126,224,124))level.setBlock(p,Blocks.AIR.defaultBlockState(),2);
  for(BlockPos p:BlockPos.betweenClosed(PRESS.below(2).offset(-1,0,-1),PRESS.below(2).offset(1,0,1)))level.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());
  place(level,PRESS,state("create:mechanical_press").setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST));
  place(level,DRIVEN_GEAR,state("create:cogwheel").setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  place(level,GEAR,state("create:cogwheel").setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  place(level,SHAFT,state("create:shaft").setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  place(level,MOTOR,state("create:creative_motor").setValue(BlockStateProperties.FACING,Direction.EAST));
  String[] ids={"nerolandcore:fluid_tank","create:fluid_tank","buildcraftfactory:tank","nerolandcore:item_store","create:item_vault"};
  for(int i=0;i<ids.length;i++){BlockPos p=new BlockPos(128+i*3,222,120);place(level,p,state(ids[i]));stores.put(ids[i],p);}
  place(level,new BlockPos(128,222,128),state("create:fluid_tank"));place(level,new BlockPos(131,222,128),state("create:fluid_tank"));
 }
 private static float speed(ServerLevel level,BlockPos pos){return ((KineticBlockEntity)level.getBlockEntity(pos)).getSpeed();}
 private static void seedAndTransfer(MinecraftServer server) {
  var level=server.overworld();
  ((CreativeMotorBlockEntity)level.getBlockEntity(MOTOR)).generatedSpeed.setValue(128);
  var input=new ItemEntity(level,PRESS.getX()+0.5,PRESS.getY()-0.75,PRESS.getZ()+0.5,new ItemStack(Items.IRON_INGOT));
  input.addTag("valen_create_press");input.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);input.setNoPickUpDelay();pressInput=input;check("press item accepted by native entity storage",level.addFreshEntity(input),Map.of("uuid",input.getUUID().toString()));
  var core=FluidStorage.SIDED.find(level,stores.get("nerolandcore:fluid_tank"),Direction.UP);
  var create=FluidStorage.SIDED.find(level,stores.get("create:fluid_tank"),Direction.UP);
  var bc=FluidStorage.SIDED.find(level,stores.get("buildcraftfactory:tank"),Direction.UP);
  transfer("Core water to Create tank",core,create,FluidVariant.of(Fluids.WATER),FluidConstants.BUCKET*2,FluidConstants.BUCKET);
  boolean exportedPlain=true;long raw=0;for(var v:create){if(v.getAmount()>0){raw+=v.getAmount();exportedPlain&=v.getResource().equals(FluidVariant.of(Fluids.WATER));}}
  check("Create exports native plain water without capacity component",exportedPlain&&raw==FluidConstants.BUCKET,Map.of("raw_amount",raw,"native_identity",exportedPlain));
  long prefilled;try(var tx=Transaction.openOuter()){prefilled=bc.insert(FluidVariant.of(Fluids.WATER),FluidConstants.BUCKET/4,tx);tx.commit();}
  check("external water tank prefilled before merge",prefilled==FluidConstants.BUCKET/4,Map.of("amount",prefilled));
  transfer("Create water merges into nonempty BuildCraft tank",create,bc,FluidVariant.of(Fluids.WATER),0,FluidConstants.BUCKET/2);
  transfer("Core items to Create vault",ItemStorage.SIDED.find(level,stores.get("nerolandcore:item_store"),Direction.UP),
   ItemStorage.SIDED.find(level,stores.get("create:item_vault"),Direction.UP),ItemVariant.of(Items.IRON_INGOT),32,7);
  BlockPos potionSource=new BlockPos(128,222,128),potionTarget=new BlockPos(131,222,128);
  var patch=net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
    new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.AWKWARD)).build();
  var potion=FluidVariant.of(com.zurrtum.create.AllFluids.POTION,patch);
  long rejected;try(var tx=Transaction.openOuter()){rejected=core.insert(potion,FluidConstants.BUCKET,tx);}
  check("Core intentionally refuses component-bearing fluid",rejected==0,Map.of("inserted",rejected,"author_contract","NeroToStandard.insert rejects hasComponents"));
  transfer("Create component-bearing potion preserves matching/export ("+potion.getComponents().getClass().getName()+")",
   FluidStorage.SIDED.find(level,potionSource,Direction.UP),FluidStorage.SIDED.find(level,potionTarget,Direction.UP),potion,FluidConstants.BUCKET,FluidConstants.BUCKET/2);
 }
 private static <T> long amount(Storage<T> store,T resource){long total=0;for(var v:store)if(v.getResource().equals(resource))total+=v.getAmount();return total;}
 private static long capacity(Storage<?> store){long total=0;for(var v:store)total+=v.getCapacity();return total;}
 private static <T> void transfer(String name,Storage<T> source,Storage<T> target,T resource,long seed,long wanted){
  if(source==null||target==null){check(name,false,Map.of("reason","missing Fabric endpoint"));return;}
  long seeded=0;if(seed>0)try(var tx=Transaction.openOuter()){seeded=source.insert(resource,seed,tx);tx.commit();}
  long a=amount(source,resource),b=amount(target,resource),sourceCap=capacity(source),targetCap=capacity(target),preview,moved;
  try(var tx=Transaction.openOuter()){preview=StorageUtil.move(source,target,resource::equals,wanted,tx);}
  boolean aborted=amount(source,resource)==a&&amount(target,resource)==b;
  boolean capacityRestored=capacity(source)==sourceCap&&capacity(target)==targetCap;
  try(var tx=Transaction.openOuter()){moved=StorageUtil.move(source,target,resource::equals,wanted,tx);tx.commit();}
  boolean conserved=amount(source,resource)==a-moved&&amount(target,resource)==b+moved;
  boolean bounded=true;for(var v:target)bounded&=v.getAmount()<=v.getCapacity();
  check(name,moved==wanted&&preview==wanted&&aborted&&capacityRestored&&conserved&&bounded,Map.of("seeded",seeded,"moved",moved,"rollback_restored",aborted,"conserved",conserved,"capacity_bound",bounded,"abort_capacity_restored",capacityRestored));
 }
 private static void verifyMachines(MinecraftServer server){
  var level=server.overworld();
  float motor=speed(level,MOTOR),shaft=speed(level,SHAFT),gear=speed(level,GEAR),driven=speed(level,DRIVEN_GEAR),press=speed(level,PRESS);
  check("motor shaft coaxial cog press and meshed cog transmit rotation",motor!=0&&shaft==motor&&gear==motor&&press==motor&&driven==-gear,
   Map.of("motor",motor,"shaft",shaft,"gear",gear,"meshed_cog",driven,"press",press));
  var items=level.getEntitiesOfClass(ItemEntity.class,new AABB(PRESS.below()).inflate(2));
  Map<String,Integer> counts=new TreeMap<>();for(var e:items)counts.merge(BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString(),e.getItem().getCount(),Integer::sum);
  check("real press tick converts dropped iron ingot",pressActive&&pressPeak>0&&counts.getOrDefault("create:iron_sheet",0)==1&&counts.getOrDefault("minecraft:iron_ingot",0)==0,
   Map.of("press_observed_running",pressActive,"peak_speed",pressPeak,"nearby_items",counts,"input_removed",pressInput==null?"null":String.valueOf(pressInput.getRemovalReason()),"input_position",pressInput==null?"null":pressInput.position().toString(),"input_stack",pressInput==null?"null":pressInput.getItem().toString()));
 }
 private static void recipes(MinecraftServer server,String label){
  Map<String,Integer> counts=new TreeMap<>(),roundtrips=new TreeMap<>();
  var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
  for(var holder:server.getRecipeManager().getRecipes()){
   var recipe=holder.value();String type=String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
   if(!type.startsWith("create:"))continue;counts.merge(type,1,Integer::sum);
   if(!Set.of("create:sequenced_assembly","create:potion","create:pressing").contains(type))continue;
   var encoded=Recipe.DIRECT_CODEC.encodeStart(ops,recipe).getOrThrow();
   var decoded=Recipe.DIRECT_CODEC.parse(ops,encoded).getOrThrow();
   if(!encoded.equals(Recipe.DIRECT_CODEC.encodeStart(ops,decoded).getOrThrow()))throw new AssertionError("JSON roundtrip "+holder.id());
   var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),server.registryAccess());
   try{
    Recipe.STREAM_CODEC.encode(buf,recipe);var network=Recipe.STREAM_CODEC.decode(buf);
    if(buf.readableBytes()!=0||!encoded.equals(Recipe.DIRECT_CODEC.encodeStart(ops,network).getOrThrow()))throw new AssertionError("network roundtrip "+holder.id());
   }finally{buf.release();}
   roundtrips.merge(type,1,Integer::sum);
  }
  try {
   var field=net.minecraft.world.item.crafting.RecipeManager.class.getDeclaredField("recipes");field.setAccessible(true);
   var map=(net.minecraft.world.item.crafting.RecipeMap)field.get(server.getRecipeManager());
   var synced=(net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwarePreparedRecipe)(Object)map;
   Map<String,Integer> syncedCounts=new TreeMap<>();
   for(var serializer:net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl.getSyncedSerializers()){
    var expected=new HashSet<net.minecraft.world.item.crafting.RecipeHolder<?>>();
    for(var holder:map.values())if(holder.value().getSerializer()==serializer)expected.add(holder);
    var actual=synced.fabric_getRecipesBySyncedSerializer(serializer);
    if(actual==null||!expected.equals(new HashSet<>(actual))||actual.size()!=expected.size())throw new AssertionError("Fabric synced cache mismatch "+BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer));
    syncedCounts.put(String.valueOf(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer)),actual.size());
   }
   check("Fabric serializer cache matches exact native holders "+label,true,Map.of("serializers",syncedCounts));
  }catch(ReflectiveOperationException error){throw new RuntimeException(error);}
  recipeSnapshots.add(Map.of("stage",label,"counts",counts,"json_and_stream_roundtrips",roundtrips));
  check("Create sequence potion press recipe codecs "+label,roundtrips.getOrDefault("create:sequenced_assembly",0)>0&&roundtrips.getOrDefault("create:potion",0)>0&&roundtrips.getOrDefault("create:pressing",0)>0,Map.of("counts",roundtrips));
  if(recipeSnapshots.size()>1)check("Create recipe counts stable after reload",recipeSnapshots.getFirst().get("counts").equals(counts),Map.of("stage",label));
 }
 private static void ore(MinecraftServer server){
  var level=server.overworld();BlockPos center=new BlockPos(144,210,144);
  for(BlockPos p:BlockPos.betweenClosed(center.offset(-8,-8,-8),center.offset(8,8,8)))level.setBlock(p,Blocks.STONE.defaultBlockState(),2);
  var feature=server.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(ResourceKey.create(Registries.FEATURE,Identifier.parse("create:zinc_ore"))).value();
  boolean placed=feature.place(level,level.getChunkSource().getGenerator(),RandomSource.create(583),center);
  int zinc=0;for(BlockPos p:BlockPos.betweenClosed(center.offset(-8,-8,-8),center.offset(8,8,8)))if(level.getBlockState(p).is(state("create:zinc_ore").getBlock()))zinc++;
  check("registered native Create zinc feature places real ore",placed&&zinc>0,Map.of("placed",placed,"zinc_blocks",zinc,"fixture","stone cube in disposable world; does not claim natural distribution"));
  var placedKey=ResourceKey.create(Registries.PLACED_FEATURE,Identifier.parse("create:zinc_ore"));
  var placedFeature=server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(placedKey).value();
  List<String> attached=new ArrayList<>(),missing=new ArrayList<>();
  for(var biome:level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()){
   String id=biome.unwrapKey().map(k->k.identifier().toString()).orElse("direct");
   (biome.value().getGenerationSettings().hasFeature(placedFeature)?attached:missing).add(id);
  }
  Collections.sort(attached);Collections.sort(missing);
  check("Create zinc feature attached to current Overworld biomes",!attached.isEmpty()&&missing.isEmpty(),Map.of("attached_biomes",attached,"missing_biomes",missing));
 }
 private static void dumpBlockMaterials()throws java.io.IOException {
  List<Object> rows=new ArrayList<>();
  for(var block:BuiltInRegistries.BLOCK){
   var state=block.defaultBlockState();
   rows.add(Map.of("id",BuiltInRegistries.BLOCK.getKey(block).toString(),"class",block.getClass().getName(),
    "default_light_emission",state.getLightEmission(),"tags",block.builtInRegistryHolder().tags().map(t->t.location().toString()).sorted().toList()));
  }
  Files.writeString(Path.of("block-material-registry.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
 }
 private static void write(MinecraftServer server){
  try{Files.writeString(Path.of("create-gameplay-contracts.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("fixture_ticks",ticks,"finished",finished,"checks",checks,"recipe_snapshots",recipeSnapshots,"server_tick",server.getTickCount()))+"\n");}
  catch(Exception e){throw new RuntimeException(e);}
 }
}
