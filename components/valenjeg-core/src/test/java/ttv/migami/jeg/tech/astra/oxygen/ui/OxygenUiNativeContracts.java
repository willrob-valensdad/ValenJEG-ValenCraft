package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
public final class OxygenUiNativeContracts {
 private static int checks;
 private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  var sides=new OxygenSideConfiguration();
  for(Direction direction:Direction.values()){
   check(sides.allows(0,direction)==(direction!=Direction.DOWN),"Preserve original native hopper input defaults");
   check(sides.allows(1,direction)==(direction==Direction.DOWN),"Preserve original native output defaults");
  }
  for(int lane=0;lane<6;lane++)for(Direction direction:Direction.values()){
   int original=sides.mask(lane),id=OxygenSideConfiguration.button(lane,direction);
   check(OxygenSideConfiguration.validButton(id)&&id-100==lane*6+direction.ordinal(),"Unique bounded native button");
   sides.toggle(lane,direction);check(sides.mask(lane)==(original^(1<<direction.ordinal())),"Only selected channel and native face changes");
   sides.toggle(lane,direction);check(sides.mask(lane)==original,"Native face toggle reverses");
  }
  check(!OxygenSideConfiguration.validButton(99)&&!OxygenSideConfiguration.validButton(136),"Reject unscoped menu actions");
  for(long value:new long[]{0,1,80,81,32767,32768,65535,65536,81000,243000,4294967295L}){
   check(OxygenMenuData.join((short)OxygenMenuData.low(value),(short)OxygenMenuData.high(value))==value,"Signed16 menu properties retain exact physical droplets "+value);
  }
  sides.toggle(4,Direction.NORTH);sides.toggle(3,Direction.DOWN);
  var out=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);sides.save(out);
  var lookup=net.minecraft.data.registries.VanillaRegistries.createWorldLookup();var restored=new OxygenSideConfiguration();
  restored.load(TagValueInput.create(ProblemReporter.DISCARDING,lookup,out.buildResult()));
  for(int i=0;i<6;i++)check(restored.mask(i)==sides.mask(i),"Native side value persistence");
  var malformed=new CompoundTag();malformed.putInt("Side4",-1);
  restored.load(TagValueInput.create(ProblemReporter.DISCARDING,lookup,malformed));check(restored.mask(4)==63,"Native persisted bitmasks are bounded");
  for(var pending:net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup))pending.apply();
  var inventory=new Inventory(null,new EntityEquipment());var machine=new SimpleContainer(3);
  var menu=new OxygenLoaderMenu(1,inventory,machine,new SimpleContainerData(OxygenMenuData.COUNT),null);
  check(menu.slots.size()==39,"Exactly3 real machine slots plus36 visible player slots; no fake item-fill pair");
  check(!menu.getSlot(2).mayPlace(new ItemStack(Items.STONE)),"Native remainder output rejects manual insertion");
  machine.setItem(2,new ItemStack(Items.BUCKET,5));
  for(int i=0;i<36;i++)inventory.setItem(i,new ItemStack(Items.STONE,64));
  int limit=new ItemStack(Items.BUCKET).getMaxStackSize();inventory.setItem(0,new ItemStack(Items.BUCKET,limit-1));
  var moved=menu.quickMoveStack(null,2);
  check(!moved.isEmpty()&&inventory.getItem(0).getCount()==limit&&machine.getItem(2).getCount()==4,"Actual native menu shift-click inserts one and retains four when player inventory nearly full");
  check(menu.quickMoveStack(null,2).isEmpty()&&machine.getItem(2).getCount()==4,"Repeated blocked shift-click neither loses nor duplicates output");
  check(menu.quickMoveStack(null,-1).isEmpty()&&menu.quickMoveStack(null,39).isEmpty(),"Native menu bounds checked");
  var process=new ttv.migami.jeg.tech.astra.oxygen.OxygenLoaderProcess(()->{});
  var source=new za.co.neroland.nerospace.gas.GasTank(3000,()->{});
  var oxygen=za.co.neroland.nerospace.gas.GasResource.OXYGEN;
  check(source.fill(oxygen,100,false)==100,"Actual installed native gas tank seed");
  var intake=new OxygenGasIntake(process);
  try(var tx=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){
   check(process.energyInput().insert(150,tx)==150,"Passthrough uses finite native power");tx.commit();
  }
  check(intake.pullFrom(source)==100&&source.getAmount()==0&&process.inputGas()==100,"Actual native tank debit exactly credits unprocessed input gas");
  check(intake.space().drain(25,false)==0,"Unprocessed gas input is not a free output or suit refill port");
  var recipe=ttv.migami.jeg.tech.astra.oxygen.OxygenLoadingRecipe.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/data/jeg/valen_oxygen_passthrough/oxygen.json")))).getOrThrow();
  check(recipe.energy()==30&&recipe.cookingTime()==1&&recipe.input().amount()==25&&recipe.result().amount()==25,"Exact pinned original native oxygen passthrough recipe");
  var before=process.state();
  try(var tx=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){check(process.stepPassthrough(recipe,tx),"Actual source gas batch admitted");}
  check(process.state().equals(before),"Aborted passthrough restores finite energy/inputgas/outputgas/progress");
  try(var tx=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){process.stepPassthrough(recipe,tx);tx.commit();}
  check(process.energy()==120&&process.inputGas()==75&&process.oxygen()==25,"Passthrough conserves gas and costs exactly30E");
  try(var tx=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){check(process.insertWater(81,tx)==0,"One native input tank never mixes gas and water");}
  before=process.state();source.fill(oxygen,25,false);boolean refused=false;
  try(var parent=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){try{intake.pullFrom(source);}catch(IllegalStateException expected){refused=true;}}
  check(refused&&source.getAmount()==25&&process.state().equals(before),"Foreign native gas debit refuses unsupported parent rollback without mutation");
  before=process.state();
  try(var parent=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){check(intake.space().fill(oxygen,10,false)==10,"Native gas inlet accepts existing oxygen through own transactional buffer");}
  check(process.state().equals(before),"Native own gas inlet rolls back with Fabric parent");
  var saved=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);process.save(saved);
  var reloaded=new ttv.migami.jeg.tech.astra.oxygen.OxygenLoaderProcess(()->{});reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING,lookup,saved.buildResult()));
  check(reloaded.state().equals(process.state()),"Native value persistence retains both original input/output tanks and finite energy");
  System.out.println("OXYGEN_UI_NATIVE_CONTRACTS_PASS "+checks+"; real input-provider routing/world identity/REI synchronization/connected automatic oxygen remain proper-Fabric proof.");
 }
}
