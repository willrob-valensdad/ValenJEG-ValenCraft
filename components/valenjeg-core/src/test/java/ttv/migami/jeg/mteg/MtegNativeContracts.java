package ttv.migami.jeg.mteg;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import ttv.migami.jeg.gun.GunDefinitions;
import ttv.migami.jeg.station.StationRecipe;
/** Registered native IDs/components/source transactions. Actual world/network evidence is separate. */
public final class MtegNativeContracts {
 private static void check(boolean b,String text){if(!b)throw new AssertionError(text);}
 public static void run(HolderLookup.Provider lookup)throws Exception{
  check(MtegTrumpetItem.SOUND_ID.equals(MtegContent.id("item.trumpet.doot")),"Original trumpet callsite sound ID changed");
  check(BuiltInRegistries.SOUND_EVENT.containsKey(MtegTrumpetItem.SOUND_ID)&&BuiltInRegistries.SOUND_EVENT.getValue(MtegTrumpetItem.SOUND_ID)!=null,"Actual trumpet callsite sound is unregistered");
  var originalSound=MtegContent.resource("/assets/mteg/sounds.json").getAsJsonObject("item.trumpet.doot");
  check(originalSound.getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString().equals("mteg:item/doot"),"Original authored trumpet audio binding changed");
  check(Files.exists(Path.of("src/main/resources/assets/mteg/sounds/item/doot.ogg")),"Original trumpet OGG missing");
  check(MtegContent.DEFINITIONS.size()==35,"Expected35 original definitions");int auto=0,manual=0,single=0;
  for(var d:MtegContent.DEFINITIONS.values()){
   for(var sound:d.source().getAsJsonObject("sounds").entrySet()){
    Identifier id=Identifier.parse(sound.getValue().getAsString());check(BuiltInRegistries.SOUND_EVENT.containsKey(id)&&BuiltInRegistries.SOUND_EVENT.getValue(id)!=null,"Unregistered original source sound "+id+" for "+d.id());
   }
   Item item=BuiltInRegistries.ITEM.getValue(d.id());check(item instanceof MtegGunItem,"Missing actual MTEG item "+d.id());
   ItemStack stack=new ItemStack(item);check(stack.getMaxDamage()==d.durability(),"Original durability "+d.id());
   check(item.getDefaultInstance().getOrDefault(ttv.migami.jeg.init.ModDataComponents.GUN_AMMO.get(),0)==0,"MTEG native default ammo component not empty");
   check(GunDefinitions.ALL.containsKey(d.id()),"MTEG missing shared registry definition");
   check(!((MtegGunItem)item).usesMagazineSwapReload(),"Original MTEG loose-source reload changed");
   check(d.nextReloadTicks(0)==(d.manual()?10:d.reloadTimer()+d.emptyMagTimer()+1),"Original strict empty reload boundary");
   if(d.automatic())auto++;if(d.manual())manual++;if(d.singleItem()){single++;check(d.roundsToLoad(0,1)==d.magazineSize(),"Single item did not fill original capacity");}
  }
  check(auto==17&&manual==7&&single==2,"Original fire/reload distribution");
  check(MtegContent.DEFINITIONS.get(MtegContent.id("famas")).burstAmount()==3,"OriginalFAMASburst");
  check(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:flare_gun"))!=BuiltInRegistries.ITEM.getValue(MtegContent.id("flare_gun")),"Namespace collision");
  check(MtegContent.FLARE.getDefaultMaxStackSize()==8&&new ItemStack(MtegContent.TRUMPET).getMaxDamage()==64,"Original ammo/trumpet properties");
  check(MtegContent.MO_WORKBENCH.newBlockEntity(net.minecraft.core.BlockPos.ZERO,MtegContent.MO_WORKBENCH.defaultBlockState()).getType().isValid(MtegContent.MO_WORKBENCH.defaultBlockState()),"Mo block missing station BE valid set");
  int count=0,guns=0;try(var files=Files.list(Path.of("src/main/resources/data/mteg/valen_station"))){for(Path p:files.toList()){
   var recipe=StationRecipe.parse(MtegContent.id(p.getFileName().toString().replace(".json","")),JsonParser.parseString(Files.readString(p)).getAsJsonObject());
   check(recipe.station().equals("mo_workbench"),"Mo recipe routing");check(!recipe.output().isEmpty(),"Missing native result "+recipe.id());
   for(var m:recipe.materials())check(!m.tag()&&BuiltInRegistries.ITEM.containsKey(m.id()),"Original Mo material missing "+m.id());
   if(recipe.output().getItem() instanceof MtegGunItem)guns++;count++;
  }}
  check(count==37&&guns==34,"Original37 Mo transactions /34 crafted guns (HogBonker remains earned)");
  var ops=net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,lookup);
  try(var files=Files.list(Path.of("src/main/resources/data/mteg/recipe"))){for(Path file:files.toList())net.minecraft.world.item.crafting.Recipe.CODEC.parse(ops,JsonParser.parseString(Files.readString(file))).getOrThrow();}
  net.minecraft.world.level.storage.loot.LootTable.DIRECT_CODEC.parse(ops,JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/mteg/loot_table/blocks/mo_workbench.json")))).getOrThrow();
  System.out.println("MTEG native original trumpet/all35 gun sound references PASS; IDs35+4, exact original stats/durability/reload branches, Mo37/34 material IDs and station BE PASS; world/network/model bake pending");
 }
}
