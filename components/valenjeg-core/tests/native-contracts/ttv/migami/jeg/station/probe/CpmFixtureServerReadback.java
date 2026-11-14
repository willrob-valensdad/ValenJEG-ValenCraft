package ttv.migami.jeg.station.probe;

import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Read-only receipt of actual CPM data stored by the normal network handler. */
final class CpmFixtureServerReadback {
    static void add(MinecraftServer server,ServerPlayer player,JsonObject receipt)throws Exception {
        Path game=Path.of("").toRealPath();
        if(!Boolean.getBoolean("valen.station.probe")
                ||!game.equals(Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server").toRealPath())
                ||!Files.isRegularFile(game.resolve("../preparation.json"))||server.getPort()!=25589
                ||!player.isAlive()||!player.getName().getString().equals("ValenPackProbe")
                ||!player.getUUID().equals(UUID.fromString("08d8976e-e59a-3751-ac2c-140b28fb5309")))
            throw new IllegalStateException("CPM server readback requires the exact alive synthetic fixture player/server");
        var remote=player.connection.getRemoteAddress();
        if(!(remote instanceof InetSocketAddress address)||address.getAddress()==null
                ||!address.getAddress().getHostAddress().equals("127.0.0.1"))
            throw new IllegalStateException("CPM server readback refuses non-loopback players");
        var mod=FabricLoader.getInstance().getModContainer("cpm").orElseThrow();
        var paths=mod.getOrigin().getPaths();
        if(!mod.getMetadata().getVersion().getFriendlyString().equals("0.6.27f")||paths.size()!=1
                ||!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(paths.getFirst())))
                    .equals("6721bd86ccc2cf72b04c5c89b03030cf75eee11a5519654566353320aed21b8a"))
            throw new IllegalStateException("CPM server runtime does not match the pinned official JAR");
        ClassLoader loader=CpmFixtureServerReadback.class.getClassLoader();
        Class<?> access=Class.forName("com.tom.cpm.shared.MinecraftServerAccess",false,loader);
        Object api=access.getMethod("get").invoke(null),handler=access.getMethod("getNetHandler").invoke(api);
        Object net=Class.forName("com.tom.cpm.shared.network.NetHandler",false,loader).getMethod("getSNetH",Object.class).invoke(handler,player);
        boolean hasMod=(boolean)Class.forName("com.tom.cpm.shared.network.NetH",false,loader).getMethod("cpm$hasMod").invoke(net);
        Object data=Class.forName("com.tom.cpm.shared.network.NetH$ServerNetH",false,loader).getMethod("cpm$getEncodedModelData").invoke(net);
        byte[] bytes=data==null?null:(byte[])Class.forName("com.tom.cpm.shared.config.PlayerData",false,loader).getField("data").get(data);
        JsonObject state=new JsonObject();state.addProperty("status","observed; compare with client native data-block representation after normal Apply");
        state.addProperty("player",player.getName().getString());state.addProperty("uuid",player.getUUID().toString());
        state.addProperty("native_has_mod",hasMod);state.addProperty("native_player_data_present",data!=null);
        state.addProperty("native_has_model",bytes!=null&&bytes.length>0);state.addProperty("model_data_bytes",bytes==null?0:bytes.length);
        if(bytes!=null)state.addProperty("model_data_sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        state.addProperty("origin","Native NetH.ServerNetH PlayerData.data; no model/cache writes or packet injection");
        receipt.add("cpm_server_model",state);
    }
}
