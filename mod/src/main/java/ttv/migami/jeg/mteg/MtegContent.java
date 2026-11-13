package ttv.migami.jeg.mteg;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import ttv.migami.jeg.fabric.compat.neoforge.neoforge.registries.DeferredHolder;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.station.StationBlock;

/** Private native port of MTEG 5d7fb0f. Keeps original IDs, assets, source transactions and acquisition. */
public final class MtegContent {
 private MtegContent(){}
 public static final Map<Identifier,MtegDefinition> DEFINITIONS=MtegDefinitions.load();
 public static StationBlock MO_WORKBENCH;
 public static BlockItem MO_WORKBENCH_ITEM;
 public static Item TAKI,FLARE,TRUMPET;
 public static net.minecraft.core.particles.SimpleParticleType FIREBALL_PARTICLE,FLARE_SMOKE_PARTICLE;
 public static EntityType<MtegProjectile> BULLET_ENTITY,TAKI_ENTITY,FIREBALL_ENTITY,FLARE_ENTITY;
 public static Identifier id(String path){return Identifier.fromNamespaceAndPath("mteg",path);}
 public static Item.Properties properties(String path){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id(path)));}
 public static JsonObject resource(String name){
  try(var in=MtegContent.class.getResourceAsStream(name)){
   if(in==null)throw new IllegalStateException("Missing pinned MTEG resource "+name);
   return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
  }catch(IOException e){throw new UncheckedIOException(e);}
 }
 public static void registerBlocks(){
  MO_WORKBENCH=Registry.register(BuiltInRegistries.BLOCK,id("mo_workbench"),new StationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(ResourceKey.create(Registries.BLOCK,id("mo_workbench"))).strength(3F).noOcclusion()));
 }
 public static void registerItems(){
  FIREBALL_PARTICLE=Registry.register(BuiltInRegistries.PARTICLE_TYPE,id("fireball"),net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple());
  FLARE_SMOKE_PARTICLE=Registry.register(BuiltInRegistries.PARTICLE_TYPE,id("flare_smoke"),net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple());
  for(String name:resource("/assets/mteg/sounds.json").keySet())Registry.register(BuiltInRegistries.SOUND_EVENT,id(name),SoundEvent.createVariableRangeEvent(id(name)));
  BULLET_ENTITY=entity("bullet");TAKI_ENTITY=entity("taki");FIREBALL_ENTITY=entity("magma_cream");FLARE_ENTITY=entity("flare");
  for(MtegDefinition definition:DEFINITIONS.values()){
   MtegGunItem item=Registry.register(BuiltInRegistries.ITEM,definition.id(),new MtegGunItem(properties(definition.id().getPath()).durability(definition.durability()).enchantable(5),definition));
   ModItems.GUNS.put(definition.id(),new DeferredHolder<>(()->item));
  }
  TAKI=Registry.register(BuiltInRegistries.ITEM,id("taki"),new MtegTakiItem(properties("taki")));
  FLARE=Registry.register(BuiltInRegistries.ITEM,id("flare"),new Item(properties("flare").stacksTo(8)));
  TRUMPET=Registry.register(BuiltInRegistries.ITEM,id("trumpet"),new MtegTrumpetItem(properties("trumpet").durability(64)));
  ModItems.AMMO.put(id("taki"),new DeferredHolder<>(()->TAKI));ModItems.AMMO.put(id("flare"),new DeferredHolder<>(()->FLARE));
  MO_WORKBENCH_ITEM=Registry.register(BuiltInRegistries.ITEM,id("mo_workbench"),new BlockItem(MO_WORKBENCH,properties("mo_workbench")));
 }
 private static EntityType<MtegProjectile> entity(String name){
  return Registry.register(BuiltInRegistries.ENTITY_TYPE,id(name),EntityType.Builder.<MtegProjectile>of(MtegProjectile::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(100).updateInterval(1).noSummon().fireImmune().build(ResourceKey.create(Registries.ENTITY_TYPE,id(name))));
 }
 public static void init(){
  MtegConfig.load();MtegReload.register();MtegProjectile.register();
  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(MtegTrailPayload.TYPE,MtegTrailPayload.STREAM_CODEC);
  LootTableEvents.MODIFY.register((key,table,source,lookup)->{
   if(key.identifier().toString().equals("minecraft:entities/piglin_brute"))table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1))).when(Holder.direct(LootItemRandomChanceCondition.randomChance(.1F).build())).add(LootItem.lootTableItem(BuiltInRegistries.ITEM.getValue(id("hog_bonker")))));
  });
 }
}
