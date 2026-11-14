package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import ttv.migami.jeg.client.KeyBindings;
import ttv.migami.jeg.init.ModDataComponents;
/** Fixture-only native input transport. Does not focus/click any OS window or invoke weapon methods. */
final class LegacyWeaponClientActions {
    private static final Set<String> ITEMS=Set.of("jeg:compound_bow","jeg:atlantean_spear","jeg:explosive_charge","jeg:ammo_box");
    private static int tick,releaseAt;private static boolean attackOwned,useOwned;private static String expected="";
    static void register() {
        if(!Boolean.getBoolean("valen.fixture.weaponControls"))return;
        FirearmSampleClient.register();
        ClientTickEvents.START_CLIENT_TICK.register(client->{
            tick++;
            if(!attackOwned&&!useOwned)return;
            try {guard(client);if(tick>=releaseAt || client.gui.screen()!=null || !held(client).equals(expected))release(client,"bounded_release");}
            catch(Throwable failure){release(client,"guard_cleanup");}
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->release(client,"disconnect_cleanup"));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->release(client,"client_stop_cleanup"));
    }
    private static String held(Minecraft client) { return client.player==null?"none":BuiltInRegistries.ITEM.getKey(client.player.getMainHandItem().getItem()).toString(); }
    private static void guard(Minecraft client) {
        Path game=client.gameDirectory.toPath();
        if(!Boolean.getBoolean("valen.fixture.weaponControls") || !Files.exists(game.resolve("../preparation.json")) || !client.getUser().getName().equals("ValenPackProbe") || client.player==null || client.getConnection()==null)
            throw new IllegalStateException("Weapon action requires explicit copied-fixture synthetic client");
        String remote=client.getConnection().getConnection().getRemoteAddress().toString();
        if(!remote.contains("127.0.0.1") || !remote.endsWith(":25589") || !client.player.getName().getString().equals("ValenPackProbe"))throw new IllegalStateException("Refusing owner/nonfixture connection");
    }
    static void action(Minecraft client,JsonObject request,JsonObject result) {
        guard(client);String action=request.get("action").getAsString();
        if(action.equals("weapon_cancel")){release(client,"requested_cleanup");result.addProperty("action_result","owned keys released through native paths");return;}
        if(client.gui.screen()!=null)throw new IllegalStateException("Weapon action requires world screen");
        String item=request.get("expected_item").getAsString();
        if((!ITEMS.contains(item)&&!ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.containsKey(net.minecraft.resources.Identifier.parse(item))&&!item.equals("mteg:trumpet")&&!item.equals("mteg:taki")&&!customizationGun(item))||!held(client).equals(item))throw new IllegalStateException("Server-supplied actual held item does not match scoped expectation");
        if(attackOwned||useOwned||client.options.keyAttack.isDown()||client.options.keyUse.isDown())throw new IllegalStateException("An input is already held; release it before another action");
        try { switch(action) {
            case "weapon_attack" -> {
                if(!item.equals("jeg:compound_bow")&&!item.equals("jeg:atlantean_spear")&&!mtegGun(item)&&!customizationGun(item))throw new IllegalArgumentException("Attack action requires original bow/spear or registered MTEG gun");
                begin(client,request,item,false);client.options.keyAttack.setDown(true);attackOwned=true;
                result.addProperty("action_result","native keyAttack press; normal JEG/LegacyClient tick handlers own firing; bounded key release queued");
            }
            case "weapon_use" -> {
                if(!item.equals("jeg:explosive_charge")&&!mtegGun(item)&&!customizationGun(item)&&!item.equals("mteg:trumpet")&&!item.equals("mteg:taki")&&!customizationGun(item))throw new IllegalArgumentException("Use action requires original charge or scoped MTEG item");
                begin(client,request,item,true);client.options.keyUse.setDown(true);useOwned=true;
                result.addProperty("native_use_result",client.gameMode.useItem(client.player,InteractionHand.MAIN_HAND).toString());
                result.addProperty("action_result","normal native useItem while keyUse held; native releaseUsingItem queued");
            }
            case "weapon_reload" -> {
                if(!item.equals("jeg:atlantean_spear")&&!mtegGun(item)&&!customizationGun(item))throw new IllegalArgumentException("Reload action requires original spear or registered MTEG gun");
                KeyMapping.click(KeyMappingHelper.getBoundKeyOf(KeyBindings.RELOAD));
                result.addProperty("action_result","native click of current bound reload key; normal consumeClick/ReloadRequest pathway owns reload");
            }
            default -> throw new IllegalArgumentException("Unknown scoped weapon action "+action);
        }
        } catch(RuntimeException|Error failure) { release(client,"action_failure_cleanup");throw failure; }
        event(client,"native_input_queued");state(client,result);
    }
    private static boolean customizationGun(String item){return Boolean.getBoolean("valen.customization.audit")&&BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(item)) instanceof ttv.migami.jeg.item.GunItem;}
    private static boolean mtegGun(String item) { return ttv.migami.jeg.mteg.MtegContent.DEFINITIONS.containsKey(net.minecraft.resources.Identifier.parse(item)); }
    private static void begin(Minecraft client,JsonObject request,String item,boolean use) {
        int duration=request.get("ticks").getAsInt();if(duration<(use?10:1)||duration>80)throw new IllegalArgumentException("Bounded hold must be10..80use or1..80attack ticks");
        expected=item;releaseAt=tick+duration+1;
    }
    private static void release(Minecraft client,String reason) {
        boolean releaseUse=useOwned;boolean had=attackOwned||useOwned;
        if(attackOwned)client.options.keyAttack.setDown(false);
        if(useOwned)client.options.keyUse.setDown(false);
        attackOwned=false;useOwned=false;releaseAt=0;
        if(releaseUse && fixtureConnection(client) && client.gameMode!=null && client.player.isUsingItem())client.gameMode.releaseUsingItem(client.player);
        if(had)event(client,reason);
    }
    private static boolean fixtureConnection(Minecraft client) {
        if(client.player==null || client.getConnection()==null || !client.getUser().getName().equals("ValenPackProbe") || !client.player.getName().getString().equals("ValenPackProbe"))return false;
        String remote=client.getConnection().getConnection().getRemoteAddress().toString();return remote.contains("127.0.0.1")&&remote.endsWith(":25589");
    }
    private static void event(Minecraft client,String phase) {
        try {JsonObject receipt=new JsonObject();receipt.addProperty("phase",phase);receipt.addProperty("client_tick",tick);state(client,receipt);Files.writeString(client.gameDirectory.toPath().resolve("valen-weapon-input-events.jsonl"),receipt+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
        catch(Exception failure){failure.printStackTrace();}
    }
    static void state(Minecraft client,JsonObject result) {
        JsonObject state=new JsonObject();state.addProperty("keyAttack",client.options.keyAttack.isDown());state.addProperty("keyUse",client.options.keyUse.isDown());state.addProperty("owned_attack",attackOwned);state.addProperty("owned_use",useOwned);state.addProperty("release_at_client_tick",releaseAt);state.addProperty("held_item",held(client));
        if(client.player!=null){var stack=client.player.getMainHandItem();state.addProperty("held_count",stack.getCount());state.addProperty("gun_ammo",stack.getOrDefault(ModDataComponents.GUN_AMMO.get(),0));state.addProperty("is_using_item",client.player.isUsingItem());state.addProperty("use_remaining_ticks",client.player.getUseItemRemainingTicks());}
        result.add("weapon_input_state",state);
    }
}
