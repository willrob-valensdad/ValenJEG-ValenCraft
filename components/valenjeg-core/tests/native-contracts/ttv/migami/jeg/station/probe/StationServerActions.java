package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.station.*;

/** Explicit copied-fixture control for one synthetic connected player; no owner world/player mutation. */
final class StationServerActions {
    private static final List<String> STATIONS=List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","schematic_station","recycler","mo_workbench");
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static int ticks;
    static void init() {
        if(!Boolean.getBoolean("valen.station.probe"))return;
        ServerTickEvents.END_SERVER_TICK.register(server->{
            if(++ticks%10!=0)return;
            Path game=Path.of("").toAbsolutePath(),inbox=game.resolve("valen-station-server-request.json");
            if(!Files.exists(inbox))return;
            try {
                if(!Files.exists(game.resolve("../preparation.json")))throw new IllegalStateException("Missing copied fixture preparation");
                ServerPlayer player=server.getPlayerList().getPlayerByName("ValenPackProbe");
                if(player==null){if(!CherryCellDiagnostic.consumeIfRequested(server,inbox,game))IndustrialRecheck.consumeIfRequested(server,inbox,game);return;}
                JsonObject request=JsonParser.parseString(Files.readString(inbox)).getAsJsonObject();
                if(request.get("action").getAsString().equals("cherry_diagnostic")){Files.delete(inbox);CherryCellDiagnostic.write(server,request,game);return;}
                if(request.get("action").getAsString().equals("industrial_recheck")){Files.delete(inbox);IndustrialRecheck.write(server,request,game);return;}
                Files.delete(inbox);
                JsonObject receipt=new JsonObject();receipt.add("request",request);
                ServerLevel level=server.overworld();String action=request.get("action").getAsString();
                switch(action) {
                    case "customization_prepare","customization_restore","customization_state" -> CustomizationServerActions.action(server,player,request,receipt);
                    case "prepare_weapon" -> LegacyWeaponServerActions.prepare(server,player,request,receipt);
                    case "bunker_ack" -> BunkerAccessServerReadback.acknowledge(server,player,request,receipt);
                    case "miner_readback" -> MinerOutpostContracts.readNearby(server,player,receipt);
                    case "bunker_decor" -> BunkerAccessServerReadback.decor(server,player,receipt);
                    case "setup" -> {
                        for(int x=-2;x<=16;x++)for(int z=3;z<=11;z++)level.setBlock(new BlockPos(x,219,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
                        for(String station:STATIONS)level.setBlock(pos(station),BuiltInRegistries.BLOCK.getValue(Identifier.parse((station.equals("mo_workbench")?"mteg:":"jeg:")+station)).defaultBlockState(),3);
                        player.setGameMode(GameType.SURVIVAL);player.teleportTo(level,.5,220,5.5,Set.of(),0,0,false);
                        receipt.addProperty("result","seven actual stations/floor placed in copied fixture; synthetic player survival");
                    }
                    case "prepare" -> {
                        String station=request.get("station").getAsString();BlockPos pos=pos(station);
                        player.closeContainer();player.getInventory().clearContent();player.setGameMode(GameType.SURVIVAL);
                        player.teleportTo(level,pos.getX()+.5,220,5.5,Set.of(),0,0,false);
                        if(!(level.getBlockEntity(pos) instanceof StationBlockEntity be))throw new IllegalStateException("Run setup first");
                        be.clearContent();
                        if(station.equals("schematic_station")) {
                            ItemStack gun=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:assault_rifle")));gun.set(ModDataComponents.GUN_AMMO.get(),7);
                            player.getInventory().setItem(0,gun);
                            player.getInventory().setItem(1,new ItemStack(ModItems.STATION_MATERIALS.get("firearm_blueprint").get(),3));
                            receipt.addProperty("result","native owned gun + three blanks; client inserts through normal slots");
                        }else if(station.equals("recycler")) {
                            player.getInventory().setItem(0,new ItemStack(Items.GRANITE,2));player.getInventory().setItem(1,new ItemStack(Items.COAL,1));
                            receipt.addProperty("result","two granite + one coal; client inserts through normal slots");
                        }else {
                            Identifier id=Identifier.parse(request.get("recipe").getAsString());StationRecipe recipe=StationRecipes.get(id);
                            if(recipe==null||!recipe.station().equals(station))throw new IllegalArgumentException("Recipe does not belong to station");
                            int slot=0;
                            for(var material:recipe.materials()) {
                                Item item;
                                if(material.tag()) {
                                    var iterator=BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,material.id())).iterator();
                                    if(!iterator.hasNext())throw new IllegalStateException("Empty actual native tag "+material.id());
                                    item=iterator.next().value();
                                }else item=BuiltInRegistries.ITEM.getValue(material.id());
                                int remaining=material.count();while(remaining>0) {ItemStack stack=new ItemStack(item,Math.min(remaining,item.getDefaultMaxStackSize()));remaining-=stack.getCount();player.getInventory().setItem(slot++,stack);}
                            }
                            if(station.equals("blueprint_workbench")) {
                                ItemStack blueprint=new ItemStack(ModItems.STATION_MATERIALS.get("advanced_firearm_blueprint").get());blueprint.set(ModDataComponents.BLUEPRINT_GUN.get(),recipe.result().toString());player.getInventory().setItem(slot,blueprint);
                            }
                            receipt.addProperty("prepared_recipe",recipe.id().toString());receipt.addProperty("expected_result",recipe.result().toString());receipt.addProperty("expected_count",recipe.count());
                            receipt.addProperty("result","actual source recipe materials prepared; actual craft still requires native client packet");
                        }
                        player.inventoryMenu.broadcastChanges();
                    }
                    case "cpm_state" -> CpmFixtureServerReadback.add(server,player,receipt);
                    case "state" -> receipt.addProperty("result","authoritative server readback");
                    default -> throw new IllegalArgumentException("Unknown copied-fixture server action "+action);
                }
                receipt.addProperty("production_world_modified",false);receipt.addProperty("player",player.getName().getString());receipt.addProperty("survival",!player.isCreative());
                JsonArray inventory=new JsonArray();for(int i=0;i<36;i++){JsonObject value=stack(player.getInventory().getItem(i));value.addProperty("slot",i);inventory.add(value);}receipt.add("player_main_inventory",inventory);
                JsonObject stations=new JsonObject();
                for(String station:STATIONS) {
                    BlockPos pos=pos(station);JsonObject state=new JsonObject();state.addProperty("x",pos.getX());state.addProperty("y",pos.getY());state.addProperty("z",pos.getZ());
                    state.addProperty("block",BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString());
                    if(level.getBlockEntity(pos) instanceof StationBlockEntity be) {
                        JsonArray slots=new JsonArray();for(int i=0;i<be.getContainerSize();i++)slots.add(stack(be.getItem(i)));state.add("persistent_slots",slots);
                        state.addProperty("progress",be.data().get(0));state.addProperty("duration",be.data().get(1));state.addProperty("burn",be.data().get(2));
                    }
                    JsonArray drops=new JsonArray();for(ItemEntity item:level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1.5)))drops.add(stack(item.getItem()));state.add("actual_dropped_outputs",drops);
                    stations.add(station,state);
                }
                var ready=server.getAdvancements().get(Identifier.parse("jeg:guide/training/ready"));
                if(ready!=null) {
                    JsonObject career=new JsonObject();var progress=player.getAdvancements().getOrStartProgress(ready);
                    for(String key:ready.value().criteria().keySet())career.addProperty(key,progress.getCriterion(key).isDone());
                    receipt.add("training_ready_actual_criteria",career);
                }
                receipt.add("stations",stations);
                LegacyWeaponServerActions.state(level,player,receipt);
                ttv.migami.jeg.tech.astra.oxygen.ui.OxygenUiServerReadback.add(player,receipt);
                Files.writeString(game.resolve("valen-station-server-state.json"),GSON.toJson(receipt));
                Files.writeString(game.resolve("valen-station-server-actions.jsonl"),receipt.toString()+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }catch(Throwable error) {
                error.printStackTrace();try{Files.writeString(game.resolve("valen-station-server-state.json"),GSON.toJson(Map.of("status","fail","error",error.toString())));}catch(Exception ignored){}
            }
        });
    }
    private static BlockPos pos(String station) {int i=STATIONS.indexOf(station);if(i<0)throw new IllegalArgumentException(station);return new BlockPos(i*2,220,8);}
    private static JsonObject stack(ItemStack stack) {JsonObject value=new JsonObject();value.addProperty("item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());value.addProperty("count",stack.getCount());String target=stack.get(ModDataComponents.BLUEPRINT_GUN.get());if(target!=null)value.addProperty("blueprint_gun",target);Integer ammo=stack.get(ModDataComponents.GUN_AMMO.get());if(ammo!=null)value.addProperty("gun_ammo",ammo);return value;}
}
