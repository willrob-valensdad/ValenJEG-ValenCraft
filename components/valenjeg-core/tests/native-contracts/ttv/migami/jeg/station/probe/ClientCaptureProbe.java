package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.time.Instant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Screenshot;

/** Fixture-only native capture inbox, adapted from the existing CreateClientProbe
 * Screenshot.grab path. Capture/world/finish only; never edits Iris, GLSL or quality.
 */
public final class ClientCaptureProbe implements ClientModInitializer {
    private final Gson gson=new GsonBuilder().setPrettyPrinting().create();
    private final JsonObject state=new JsonObject();
    private final long created=System.currentTimeMillis();private int ticks;
    @Override public void onInitializeClient(){
        if(!Boolean.getBoolean("valen.fixture.capture"))return;
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            Path game=client.gameDirectory.toPath();
            try{
                if(!Files.exists(game.resolve("../preparation.json")))throw new IllegalStateException("Capture helper requires the separately staged fixture");
                if(!client.getUser().getName().equals("ValenPackProbe"))throw new IllegalStateException("Capture helper refuses an owner or non-fixture user");
                if(System.currentTimeMillis()-created>480_000||Files.exists(game.resolve("finish-client"))){client.stop();return;}
                boolean joined=client.level!=null&&client.player!=null&&client.getConnection()!=null;
                String remote=joined?client.getConnection().getConnection().getRemoteAddress().toString():"not connected";
                if(joined&&(!remote.contains("127.0.0.1")||!remote.endsWith(":25589")||!client.player.getName().getString().equals("ValenPackProbe")))throw new IllegalStateException("Capture helper refuses a non-fixture connection or owner account");
                if(++ticks%20==0){
                    state.addProperty("latest_at",Instant.now().toString());state.addProperty("connected_server",remote);state.addProperty("screen",client.gui.screen()==null?"world":client.gui.screen().getClass().getName());
                    if(joined){JsonArray pos=new JsonArray();pos.add(client.player.getX());pos.add(client.player.getY());pos.add(client.player.getZ());state.add("position",pos);state.addProperty("yaw",client.player.getYRot());state.addProperty("pitch",client.player.getXRot());state.addProperty("dimension",client.level.dimension().identifier().toString());}
                    Files.writeString(game.resolve("valen-capture-state.json"),gson.toJson(state));
                }
                Path inbox=game.resolve("valen-capture-request.txt");if(!Files.exists(inbox))return;
                String command=Files.readString(inbox).trim();
                if(command.equals("finish")){Files.delete(inbox);client.stop();return;}
                if(command.equals("world")){if(!joined)throw new IllegalStateException("World action requires joined loopback fixture");Files.delete(inbox);client.gui.setScreen(null);return;}
                if(!joined&&(client.player!=null||client.getConnection()!=null||client.level!=null||client.gui.screen()==null))throw new IllegalStateException("Prejoin capture requires offline synthetic fixture UI");
                if(!command.matches("[a-zA-Z0-9_-]{1,80}\\.png"))throw new IllegalArgumentException("Allowed fixture inbox commands: named PNG, world, finish");
                Files.delete(inbox);state.addProperty("capture_scope",joined?"joined loopback synthetic fixture":"offline prejoin synthetic fixture UI");state.addProperty("capture_requested",command);state.addProperty("capture_requested_at",Instant.now().toString());
                Screenshot.grab(client.gameDirectory,command,client.gameRenderer.mainRenderTarget(),1,message->client.execute(()->{
                    try{
                        JsonObject receipt=new JsonObject();receipt.addProperty("filename",command);receipt.addProperty("callback_at",Instant.now().toString());receipt.addProperty("native_message",message.getString());receipt.addProperty("source","actual main render target via native Screenshot.grab");receipt.addProperty("capture_scope",joined?"joined loopback synthetic fixture":"offline prejoin synthetic fixture UI");receipt.addProperty("pixel_and_visual_inspection","pending artifact decode and image inspection");
                        Files.writeString(game.resolve("valen-capture-acknowledgements.jsonl"),receipt.toString()+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                        state.add("last_capture_callback",receipt);Files.writeString(game.resolve("valen-capture-state.json"),gson.toJson(state));
                    }catch(Exception e){e.printStackTrace();}
                }));
            }catch(Throwable error){
                state.addProperty("error",error.toString());error.printStackTrace();
                try{Files.writeString(game.resolve("valen-capture-state.json"),gson.toJson(state));}catch(Exception io){io.printStackTrace();}
            }
        });
    }
}
