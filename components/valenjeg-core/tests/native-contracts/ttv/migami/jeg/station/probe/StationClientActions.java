package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.station.StationMenu;

/** Fixture-only ordinary client callbacks/packets, with explicit action receipts and no local craft shortcut. */
final class StationClientActions {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    static void tick(Minecraft client) {
        Path game=client.gameDirectory.toPath(),inbox=game.resolve("valen-station-request.json");
        try {
            if(!Files.exists(game.resolve("../preparation.json")))throw new IllegalStateException("Missing staged fixture preparation");
            boolean joined=client.player!=null&&client.getConnection()!=null;
            String remote=joined?client.getConnection().getConnection().getRemoteAddress().toString():"not joined";
            if(!client.getUser().getName().equals("ValenPackProbe") || joined&&(!remote.contains("127.0.0.1") || !remote.endsWith(":25589")))
                throw new IllegalStateException("Refusing non-fixture server or owner player");
            if(!Files.exists(inbox)) {writeState(client,game,new JsonObject());return;}
            JsonObject request=JsonParser.parseString(Files.readString(inbox)).getAsJsonObject();Files.delete(inbox);
            JsonObject result=new JsonObject();result.add("request",request);
            String action=request.get("action").getAsString();
            if(!joined&&!action.equals("ack")&&!action.equals("dismiss_prompt")&&!action.equals("state"))throw new IllegalStateException("Gameplay action requires joined fixture");
            switch(action) {
                case "weapon_attack","weapon_use","weapon_reload","weapon_cancel" -> LegacyWeaponClientActions.action(client,request,result);
                case "firearm_sample_start" -> FirearmSampleClient.start(client,request,result);
                case "bunker_move","bunker_look","bunker_release","bunker_state" -> BunkerAccessClientActions.action(client,request,result);
                case "cpm_open","cpm_state","cpm_click","cpm_back","cpm_close" -> CpmFixtureClientActions.action(client,request,result);
                case "state" -> result.addProperty("action_result","read");
                case "photo_hud" -> {
                    if(!Boolean.getBoolean("valen.fixture.photoHud"))throw new IllegalStateException("photo hud requires explicit copied-fixture photo profile");
                    if(client.player==null||!client.player.isAlive())throw new IllegalStateException("photo hud requires the alive synthetic player");
                    boolean previous=client.gui.hud.isHidden();
                    result.addProperty("previous_hide_gui", previous);
                    if(request.has("hide")){
                        if(client.gui.hud.isHidden()!=request.get("hide").getAsBoolean())client.gui.hud.toggle();
                        result.addProperty("action_result","native 26.3 Hud.toggle applied; options file not written");
                    }else result.addProperty("action_result","native 26.3 Hud.isHidden read");
                    result.addProperty("hide_gui", client.gui.hud.isHidden());
                }
                case "customization_open","customization_probe" -> CustomizationClientProbe.action(client,request,result);
                case "asset_probe" -> FixtureAssetProbe.run(client,result);
                case "structure_fidelity_models" -> StructureFidelityClientProbe.probe(client,result);
                case "oxygen_config" -> {
                    if(!(client.player.containerMenu instanceof ttv.migami.jeg.tech.astra.oxygen.ui.OxygenLoaderMenu menu))throw new IllegalStateException("Not an actual Oxygen Loader menu");
                    int channel=request.get("channel").getAsInt();
                    Direction side=Direction.valueOf(request.get("side").getAsString());
                    client.gameMode.handleInventoryButtonClick(menu.containerId,ttv.migami.jeg.tech.astra.oxygen.ui.OxygenSideConfiguration.button(channel,side));
                    result.addProperty("action_result","normal native menu button packet sent; authoritative server readback required");
                }
                case "interact" -> {
                    BlockPos pos=new BlockPos(request.get("x").getAsInt(),request.get("y").getAsInt(),request.get("z").getAsInt());
                    var accepted=client.gameMode.useItemOn(client.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
                    result.addProperty("action_result","native useItemOn sent: "+accepted);
                }
                case "craft" -> {
                    if(!(client.player.containerMenu instanceof StationMenu menu))throw new IllegalStateException("Not a restored station menu");
                    String id=request.get("recipe").getAsString();int selected=-1;
                    for(int i=0;i<menu.recipes.size();i++)if(menu.recipes.get(i).id().toString().equals(id))selected=i;
                    if(selected<0)throw new IllegalArgumentException("Recipe not supplied by current server menu: "+id);
                    client.gameMode.handleInventoryButtonClick(menu.containerId,selected);
                    result.addProperty("action_result","native inventory-button packet sent; server outcome requires subsequent readback");
                }
                case "slot" -> {
                    int slot=request.get("slot").getAsInt(),button=request.has("button")?request.get("button").getAsInt():0;
                    ContainerInput input=ContainerInput.valueOf(request.has("input")?request.get("input").getAsString():"PICKUP");
                    client.gameMode.handleContainerInput(client.player.containerMenu.containerId,slot,button,input,client.player);
                    result.addProperty("action_result","native container-input packet sent");
                }
                case "close" -> {client.player.closeContainer();result.addProperty("action_result","native menu close sent");}
                case "rei" -> {
                    String id=request.get("category").getAsString();if(!id.startsWith("jeg:")&&!id.equals("mteg:mo_workbench"))throw new IllegalArgumentException("Only fixture JEG categories");
                    result.addProperty("action_result",ViewSearchBuilder.builder().addCategory(CategoryIdentifier.of(id)).open());
                }
                case "ack","dismiss_prompt" -> {
                    var screen=client.gui.screen();if(screen==null)throw new IllegalStateException("No prompt");
                    String type=screen.getClass().getName();
                    boolean known=type.equals("de.ambertation.wover.ui.impl.client.UpdatesScreen")
                        || type.startsWith("ttv.migami.jeg.")&&type.contains("MagazineModeClient");
                    boolean respawn=type.equals("net.minecraft.client.gui.screens.DeathScreen");
                    if(respawn&&(!joined||!client.player.getName().getString().equals("ValenPackProbe")||!action.equals("ack")||!request.has("label")||!request.get("label").getAsString().equals("Respawn")))
                        throw new IllegalStateException("DeathScreen permits only its observed ordinary Respawn button on the connected synthetic fixture");
                    if(!known&&!respawn)throw new IllegalStateException("Prompt is outside exact fixture JEG/WorldWeaver/Respawn scope: "+type);
                    if(action.equals("dismiss_prompt")) {
                        if(!screen.shouldCloseOnEsc())throw new IllegalStateException("Known prompt does not permit ordinary Escape close");
                        screen.onClose();result.addProperty("action_result","known prompt ordinary onClose callback invoked; shouldCloseOnEsc=true");
                    }else {
                        String label=request.get("label").getAsString();boolean clicked=false;
                        for(Button button:buttons(screen))if(button.active&&button.visible&&button.getMessage().getString().equals(label)) {
                            button.onPress(null);clicked=true;break;
                        }
                        if(!clicked)throw new IllegalStateException("No active normal prompt button "+label);
                        result.addProperty("action_result","normal nested button callback invoked: "+label);
                    }
                }
                default -> throw new IllegalArgumentException("Unknown fixture action "+action);
            }
            writeState(client,game,result);
            Files.writeString(game.resolve("valen-station-actions.jsonl"),result.toString()+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        }catch(Throwable error) {
            error.printStackTrace();try{JsonObject result=new JsonObject();result.addProperty("error",error.toString());writeState(client,game,result);}catch(Exception ignored){}
        }
    }
    private static List<Button> buttons(net.minecraft.client.gui.components.events.ContainerEventHandler parent) {
        List<Button> found=new ArrayList<>();Set<Object> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<net.minecraft.client.gui.components.events.GuiEventListener> pending=new ArrayDeque<>(parent.children());
        while(!pending.isEmpty()&&seen.size()<2048) {
            var child=pending.removeFirst();if(!seen.add(child))continue;
            if(child instanceof Button button)found.add(button);
            if(child instanceof net.minecraft.client.gui.components.events.ContainerEventHandler nested)pending.addAll(nested.children());
        }
        return found;
    }
    private static JsonObject stack(ItemStack stack) {
        JsonObject value=new JsonObject();value.addProperty("item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());value.addProperty("count",stack.getCount());
        String blueprint=stack.get(ModDataComponents.BLUEPRINT_GUN.get());if(blueprint!=null)value.addProperty("blueprint_gun",blueprint);
        Integer ammo=stack.get(ModDataComponents.GUN_AMMO.get());if(ammo!=null)value.addProperty("gun_ammo",ammo);return value;
    }
    private static void writeState(Minecraft client,Path game,JsonObject result)throws Exception {
        LegacyWeaponClientActions.state(client,result);
        ttv.migami.jeg.tech.astra.oxygen.ui.OxygenUiClientReadback.add(client,result);
        var screen=client.gui.screen();result.addProperty("screen",screen==null?"world":screen.getClass().getName());
        JsonArray buttonLabels=new JsonArray();if(screen!=null)for(Button button:buttons(screen))buttonLabels.add(button.getMessage().getString());result.add("buttons",buttonLabels);
        if(screen!=null){result.addProperty("screen_title",screen.getTitle().getString());result.addProperty("shouldCloseOnEsc",screen.shouldCloseOnEsc());}
        if(client.player==null){Files.writeString(game.resolve("valen-station-client-state.json"),GSON.toJson(result));return;}
        result.addProperty("menu",client.player.containerMenu.getClass().getName());result.addProperty("menu_id",client.player.containerMenu.containerId);
        JsonArray slots=new JsonArray();for(int i=0;i<client.player.containerMenu.slots.size();i++){JsonObject value=stack(client.player.containerMenu.getSlot(i).getItem());value.addProperty("slot",i);slots.add(value);}result.add("slots",slots);
        result.add("carried",stack(client.player.containerMenu.getCarried()));
        JsonArray recipes=new JsonArray();if(client.player.containerMenu instanceof StationMenu menu)for(var recipe:menu.recipes)recipes.add(recipe.id().toString());result.add("server_menu_recipes",recipes);
        result.addProperty("x",client.player.getX());result.addProperty("y",client.player.getY());result.addProperty("z",client.player.getZ());
        result.addProperty("proof","Actual client state; sent action alone is not server acceptance");
        Files.writeString(game.resolve("valen-station-client-state.json"),GSON.toJson(result));
    }
}
