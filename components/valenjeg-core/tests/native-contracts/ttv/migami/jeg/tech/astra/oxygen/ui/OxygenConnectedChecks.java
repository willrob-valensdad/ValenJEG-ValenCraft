package ttv.migami.jeg.tech.astra.oxygen.ui;
import com.google.gson.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.reborn.energy.api.EnergyStorage;
import ttv.migami.jeg.tech.astra.oxygen.*;
import za.co.neroland.nerospace.config.NerospaceConfig;
import za.co.neroland.nerospace.gas.*;
import za.co.neroland.nerospace.platform.*;
import za.co.neroland.nerospace.world.OxygenManager;
/** Exact copied-fixture connected player only. Observes NORMAL installed Nerospace server callbacks.
 * No direct hazard callback, metabolism substitution, suit-storage invention or runtime launcher. */
public final class OxygenConnectedChecks {
 private static final Path FIXTURE=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
 private static OxygenConnectedChecks active;
 private final MinecraftServer server;private final ServerPlayer player;private final Path output;
 private final JsonObject report=new JsonObject();private final JsonArray samples=new JsonArray();
 private final List<BlockPos> owned=new ArrayList<>();private final List<BlockState> original=new ArrayList<>();
 private final List<ItemStack> previousInventory=new ArrayList<>();
 private ServerLevel level,previousLevel;private BlockPos machine,tankPos;private Vec3 previousPosition;
 private GameType previousMode;private float previousHealth,previousSaturation,previousYaw,previousPitch;
 private int previousFood,previousAir,previousReserve,seed,negativeConsumption,phase,phaseTick,startTick,positiveStart;
 private long gasAtPositive;private boolean forced,worldOwned,playerOwned,finished,receiptOwned;
 private OxygenLoaderBlockEntity loader;private NerospaceGasStorage tank;
 private OxygenConnectedChecks(MinecraftServer server,ServerPlayer player){
  this.server=server;this.player=player;output=server.getServerDirectory().resolve("valen-oxygen-connected-probe.json");
  report.addProperty("status","running");report.addProperty("started_utc",Instant.now().toString());report.addProperty("profile",player.getGameProfile().name());
  report.addProperty("connected_uuid",player.getUUID().toString());report.addProperty("direct_hazard_callback_invocations",0);
  report.addProperty("gameplay_reserve_writes",0);report.addProperty("cleanup_test_reserve_restores",0);report.addProperty("initial_test_reserve_seeds",0);report.add("samples",samples);
 }
 public static void register(){
  if(!Boolean.getBoolean("valen.astra.oxygen.connected"))return;
  ServerTickEvents.END_SERVER_TICK.register(server->{
   Path request=server.getServerDirectory().resolve("valen-oxygen-connected-request.json");
   try{
    if(active!=null&&!active.finished){active.observe();return;}
    if(!Files.exists(request))return;
    guard(server);var json=JsonParser.parseString(Files.readString(request)).getAsJsonObject();
    if(!json.get("action").getAsString().equals("begin"))throw new IllegalArgumentException("Only explicit begin is supported");
    var player=server.getPlayerList().getPlayerByName("ValenPackProbe");
    if(player==null)return;
    if(!player.getName().getString().equals("ValenPackProbe")||!player.getIpAddress().equals("127.0.0.1"))throw new IllegalStateException("Refusing nonloopback connected profile");
    Files.delete(request);active=new OxygenConnectedChecks(server,player);
    try{active.begin();}catch(Throwable error){active.finish(error);}
   }catch(Throwable error){error.printStackTrace();}
  });
  ServerLifecycleEvents.SERVER_STOPPING.register(server->{if(active!=null&&!active.finished&&active.server==server)active.finish(new IllegalStateException("Fixture stopped before completion"));});
 }
 private static void guard(MinecraftServer server)throws Exception{
  if(!Boolean.getBoolean("valen.astra.oxygen.connected")||server.getPort()!=25589||!server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())||!Files.exists(FIXTURE.resolve("../preparation.json")))
   throw new IllegalStateException("Explicit exact copied fixture guard failed");
 }
 private void require(boolean ok,String label){if(!ok)throw new AssertionError(label);}
 private void write(){try{Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}catch(Exception e){throw new IllegalStateException(e);}}
 private Item item(String id){var key=Identifier.parse(id);require(BuiltInRegistries.ITEM.containsKey(key),"Missing actual item "+id);return BuiltInRegistries.ITEM.getValue(key);}
 private void sample(){
  var row=new JsonObject();row.addProperty("server_tick",server.getTickCount());row.addProperty("player_tick",player.tickCount);
  row.addProperty("phase",phase);row.addProperty("native_reserve",Services.PLATFORM.getOxygen(player));row.addProperty("native_tank_gas_mb",tank==null?0:tank.getAmount());
  row.addProperty("loader_gas_mb",loader==null?0:loader.process().oxygen());row.addProperty("health",player.getHealth());samples.add(row);
 }
 private void phase(int value){phase=value;phaseTick=server.getTickCount();report.addProperty("phase",value);sample();write();}
 private void pose(double x,double y,double z){
  player.teleportTo(level,x,y,z,Set.of(),0,0,false);player.setDeltaMovement(Vec3.ZERO);player.fallDistance=0;
  player.setYHeadRot(0);player.yHeadRotO=0;player.yBodyRot=0;player.yBodyRotO=0;player.setOldPosAndRot();
 }
 private void begin()throws Exception{
  guard(server);require(!Files.exists(output),"Archive previous connected oxygen receipt before retry");receiptOwned=true;
  require(player.isAlive(),"Ordinary Respawn required before test setup");
  require(OxygenUi.MENU!=null&&AstraOxygen.Content.BLOCK!=null,"Oxygen loader/menu not initialized");
  require(OxygenLoadingRecipe.active()!=null&&OxygenLoadingRecipe.active().energy()==30&&OxygenLoadingRecipe.active().input().amount()==100&&OxygenLoadingRecipe.active().result().amount()==4,"Pinned source recipe binding changed");
  level=server.getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse("nerospace:greenxertz")));
  require(level!=null&&OxygenManager.isAirless(level.dimension()),"Native airless dimension required");
  int y=level.getMaxY()-10;machine=new BlockPos(401,y,401);tankPos=machine.north();level.getChunk(25,25);
  for(int x=400;x<=406;x++)for(int z=400;z<=406;z++)owned.add(new BlockPos(x,y-1,z));
  owned.add(machine);owned.add(tankPos);
  for(BlockPos pos:owned){require(level.getBlockState(pos).isAir()&&level.getBlockEntity(pos)==null,"Occupied fixture oxygen position "+pos);original.add(level.getBlockState(pos));}
  require(level.getBlockState(new BlockPos(406,y,406)).isAir()&&level.getBlockState(new BlockPos(406,y+1,406)).isAir(),"Negative actor headspace occupied");
  for(var other:level.players())require(other==player||other.distanceToSqr(401.5,y,401.5)>256,"Other connected consumer in test region");
  forced=level.setChunkForced(25,25,true);worldOwned=true;
  for(int i=0;i<49;i++)level.setBlockAndUpdate(owned.get(i),Blocks.SMOOTH_STONE.defaultBlockState());
  level.setBlockAndUpdate(machine,AstraOxygen.Content.BLOCK.defaultBlockState());
  var gasBlock=BuiltInRegistries.BLOCK.getValue(Identifier.parse("nerolandcore:gas_tank"));require(gasBlock!=Blocks.AIR,"Actual Core gas tank missing");
  level.setBlockAndUpdate(tankPos,gasBlock.defaultBlockState());
  require(level.getBlockEntity(machine) instanceof OxygenLoaderBlockEntity,"Actual loader entity missing");loader=(OxygenLoaderBlockEntity)level.getBlockEntity(machine);
  tank=GasLookup.INSTANCE.find(level,tankPos,Direction.SOUTH);require(tank!=null&&tank.getAmount()==0,"Actual empty Core gas tank missing");
  previousLevel=player.level();previousPosition=player.position();previousMode=player.gameMode.getGameModeForPlayer();previousHealth=player.getHealth();
  previousFood=player.getFoodData().getFoodLevel();previousSaturation=player.getFoodData().getSaturationLevel();previousAir=player.getAirSupply();
  previousYaw=player.getYRot();previousPitch=player.getXRot();previousReserve=Services.PLATFORM.getOxygen(player);
  for(int i=0;i<player.getInventory().getContainerSize();i++)previousInventory.add(player.getInventory().getItem(i).copy());
  playerOwned=true;player.closeContainer();player.setGameMode(GameType.SURVIVAL);player.getInventory().clearContent();
  player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item("nerospace:oxygen_suit_helmet")));
  player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(item("nerospace:oxygen_suit_chestplate")));
  player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(item("nerospace:oxygen_suit_leggings")));
  player.setItemSlot(EquipmentSlot.FEET,new ItemStack(item("nerospace:oxygen_suit_boots")));
  require(OxygenManager.suitTier(player)!=OxygenManager.SuitTier.NONE,"Actual complete Nero suit not recognized");
  pose(406.5,y,406.5);player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);player.getFoodData().setSaturation(5);
  seed=Math.max(10,Math.min(100,OxygenManager.maxOxygenFor(player)/4));Services.PLATFORM.setOxygen(player,seed);report.addProperty("initial_test_reserve_seeds",1);
  report.addProperty("airless_dimension",level.dimension().identifier().toString());report.addProperty("initial_reserve",seed);
  report.addProperty("suit_capacity",OxygenManager.maxOxygenFor(player));report.addProperty("native_recipe","100 mB water +30 E/t ->4 mB nerospace:oxygen");
  startTick=server.getTickCount();phase(0);
 }
 private void energy(long value){
  var input=EnergyStorage.SIDED.find(level,machine,Direction.UP);require(input!=null,"Actual native loader energy input missing");
  long left=value;for(int i=0;left>0&&i<10;i++)try(var tx=Transaction.openOuter()){long amount=input.insert(left,tx);require(amount>0,"Native energy seed rejected");tx.commit();left-=amount;}
  require(left==0,"Actual loader rejected bounded seed");
 }
 private void observe(){
  try{
   require(server.getPlayerList().getPlayer(player.getUUID())==player&&player.isAlive(),"Connected synthetic player disconnected or died");
   require(player.level()==level&&player.gameMode.getGameModeForPlayer()==GameType.SURVIVAL,"Actor left native Survival dimension");
   require(OxygenManager.suitTier(player)!=OxygenManager.SuitTier.NONE,"Actual Nero suit removed");
   require(server.getTickCount()-startTick<=180,"Connected automatic oxygen proof timed out");
   int elapsed=server.getTickCount()-phaseTick;sample();
   if(phase==0&&elapsed>=20){
    require(tank.getAmount()==0&&loader.process().oxygen()==0,"Negative control gas is not empty");
    negativeConsumption=seed-Services.PLATFORM.getOxygen(player);require(negativeConsumption>0,"Automatic installed callback did not consume native oxygen in airless negative control");
    report.addProperty("negative_20tick_native_consumption",negativeConsumption);energy(600);
    ItemStack water=new ItemStack(Items.WATER_BUCKET);loader.insertHeld(water);require(water.isEmpty(),"Actual water bucket insertion failed");phase(1);
   }else if(phase==1){
    if(loader.getItem(1).isEmpty()&&loader.process().water()+loader.process().oxygen()*25*81<2000*81&&tank.getAmount()<80){
     // Exactly a second source water bucket; it is inserted once when total physical water/gas indicates only the first bucket.
     long consumedMb=(tank.getAmount()+loader.process().oxygen())*25;
     if(loader.process().water()/81+consumedMb==1000){ItemStack second=new ItemStack(Items.WATER_BUCKET);loader.insertHeld(second);require(second.isEmpty(),"Second native water bucket insertion failed");}
    }
    long gas=tank.getAmount()+loader.process().oxygen();require(gas<=80&&gas%4==0,"Unaccounted machine gas production");
    if(gas<80)return;
    require(tank.getAmount()==80&&loader.process().oxygen()==0&&loader.process().energy()==0&&loader.process().water()==0,"Actual twenty source batches do not conserve 600E/2000mB/80gas");
    gasAtPositive=tank.getAmount();positiveStart=Services.PLATFORM.getOxygen(player);pose(402.5,machine.getY(),401.5);
    require(tank.getGas()==GasResource.OXYGEN,"Native gas identity mismatch");phase(2);
   }else if(phase==2&&elapsed>=20){
    int ratio=NerospaceConfig.scale(2,NerospaceConfig.fuelCostMultiplier());require(ratio>0,"Configured native gas cost is not finite positive");
    long debit=gasAtPositive-tank.getAmount();int after=Services.PLATFORM.getOxygen(player),gain=after-positiveStart;
    require(debit>0&&debit%ratio==0&&gain>0,"Normal connected-player callbacks did not exchange native tank gas for actual reserve");
    require(debit/ratio-gain==negativeConsumption,"Scheduled tank debit/reserve gain does not account for matching20tick native metabolism");
    report.addProperty("native_gas_mb_per_air",ratio);report.addProperty("positive_tank_debit_mb",debit);report.addProperty("positive_reserve_gain",gain);
    report.addProperty("automatic_connected_player_scheduling","PASS: actual installed callbacks observed over20negative+20positive ticks; no direct hazard callback invoked");
    finish(null);
   }
   if(!finished)write();
  }catch(Throwable failure){finish(failure);}
 }
 private void finish(Throwable failure){
  if(finished)return;finished=true;Throwable error=failure;
  try{
   if(playerOwned){player.closeContainer();player.getInventory().clearContent();for(int i=0;i<previousInventory.size();i++)player.getInventory().setItem(i,previousInventory.get(i));
    player.setGameMode(previousMode);player.teleportTo(previousLevel,previousPosition.x,previousPosition.y,previousPosition.z,Set.of(),previousYaw,previousPitch,false);
    if(player.isAlive())player.setHealth(Math.min(previousHealth,player.getMaxHealth()));player.getFoodData().setFoodLevel(previousFood);player.getFoodData().setSaturation(previousSaturation);player.setAirSupply(previousAir);
    Services.PLATFORM.setOxygen(player,previousReserve);report.addProperty("cleanup_test_reserve_restores",1);player.inventoryMenu.broadcastChanges();report.addProperty("synthetic_profile_restored",true);
   }
  }catch(Throwable cleanup){if(error==null)error=cleanup;else error.addSuppressed(cleanup);}
  try{
   if(worldOwned){for(int i=owned.size()-1;i>=0;i--){var blockEntity=level.getBlockEntity(owned.get(i));if(blockEntity instanceof net.minecraft.world.Container c)c.clearContent();level.setBlockAndUpdate(owned.get(i),original.get(i));}
    if(forced)level.setChunkForced(25,25,false);report.addProperty("owned_fixture_blocks_restored",true);}
  }catch(Throwable cleanup){if(error==null)error=cleanup;else error.addSuppressed(cleanup);}
  report.addProperty("status",error==null?"pass":"fail");report.addProperty("finished_utc",Instant.now().toString());if(error!=null){report.addProperty("error",error.toString());error.printStackTrace();}if(receiptOwned)write();
 }
}
