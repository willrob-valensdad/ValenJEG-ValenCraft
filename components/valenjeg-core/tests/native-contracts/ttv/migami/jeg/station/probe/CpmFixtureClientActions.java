package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Optional fixture transport; normal callbacks only, no editor/file picker/public upload. */
final class CpmFixtureClientActions {
    private static final Path GAME=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/client");
    private static final String VERSION="0.6.27f";
    private static final String SHA="6721bd86ccc2cf72b04c5c89b03030cf75eee11a5519654566353320aed21b8a";
    private static final String GUI="com.tom.cpm.client.GuiImpl";
    private static final String GESTURES="com.tom.cpm.shared.gui.GestureGui";
    private static final String MODELS="com.tom.cpm.shared.gui.ModelsGui";
    private static Observation observed;
    private record Row(Object widget,String label,boolean active,String modelFile) {}
    private record Approved(String id,String filename,String sha256) {}
    private record Observation(String id,Screen screen,Object frame,long at,Map<String,Row> buttons) {}

    static void action(Minecraft client,JsonObject request,JsonObject result)throws Exception {
        KeyMapping key=guard(client);
        switch(request.get("action").getAsString()) {
            case "cpm_open" -> {
                if(client.gui.screen()!=null||client.player.containerMenu!=client.player.inventoryMenu)
                    throw new IllegalStateException("CPM open requires the unobstructed joined fixture world");
                observed=null;
                KeyMapping.click(KeyMappingHelper.getBoundKeyOf(key));
                result.addProperty("action_result","ordinary click of actual bound CPM gesture key queued; read next native frame");
            }
            case "cpm_state" -> result.addProperty("action_result","read-only native CPM frame/widget state");
            case "cpm_click" -> click(client,request,result);
            case "cpm_back","cpm_close" -> {
                Object frame=frame(client);
                requireFrame(frame);
                if(popup(frame))throw new IllegalStateException("Unexpected CPM popup; inspect it, do not dismiss through this route");
                requireObservation(client,frame,request);
                observed=null;
                client.gui.screen().onClose();
                result.addProperty("action_result","ordinary onClose of scoped CPM navigation frame invoked");
            }
            default -> throw new IllegalArgumentException("Unknown bounded CPM fixture action");
        }
        state(client,key,result);
    }

    private static KeyMapping guard(Minecraft client)throws Exception {
        Path game=client.gameDirectory.toPath().toRealPath();
        if(!Boolean.getBoolean("valen.fixture.cpmControls")||!game.equals(GAME.toRealPath())
                ||!Files.isRegularFile(game.resolve("../preparation.json"))
                ||!client.getUser().getName().equals("ValenPackProbe")||client.player==null
                ||!client.player.isAlive()||!client.player.getName().getString().equals("ValenPackProbe")
                ||!client.player.getUUID().equals(UUID.fromString("08d8976e-e59a-3751-ac2c-140b28fb5309"))
                ||client.getConnection()==null)
            throw new IllegalStateException("CPM controls require the explicit alive synthetic copied-fixture client");
        var remote=client.getConnection().getConnection().getRemoteAddress();
        if(!(remote instanceof InetSocketAddress address)||address.getAddress()==null
                ||!address.getAddress().getHostAddress().equals("127.0.0.1")||address.getPort()!=25589)
            throw new IllegalStateException("CPM controls refuse any connection except fixture 127.0.0.1:25589");
        var mod=FabricLoader.getInstance().getModContainer("cpm").orElseThrow();
        if(!mod.getMetadata().getVersion().getFriendlyString().equals(VERSION))
            throw new IllegalStateException("CPM runtime version does not match the inspected official build");
        var origins=mod.getOrigin().getPaths();
        if(origins.size()!=1||!Files.isRegularFile(origins.getFirst())||!sha(origins.getFirst()).equals(SHA))
            throw new IllegalStateException("CPM runtime mod origin does not match the pinned official JAR");
        Object key=type("com.tom.cpm.client.KeyBindings").getField("gestureMenuBinding").get(null);
        if(!(key instanceof KeyMapping mapping))throw new IllegalStateException("Native CPM gesture key is not initialized");
        if(KeyMappingHelper.getBoundKeyOf(mapping).getValue()==InputConstants.UNKNOWN.getValue())
            throw new IllegalStateException("The actual native CPM gesture key is unbound");
        return mapping;
    }

    private static void click(Minecraft client,JsonObject request,JsonObject result)throws Exception {
        if(request.has("target")&&request.get("target").getAsString().equals("render_toggle")) {
            if(client.gui.screen()!=null||client.player.containerMenu!=client.player.inventoryMenu)
                throw new IllegalStateException("CPM render toggle requires the unobstructed fixture world");
            requireObservation(client,null,request);
            Class<?> player=type("com.tom.cpm.shared.config.Player");
            boolean enabled=(boolean)player.getMethod("isEnableRendering").invoke(null);
            if(!request.has("expected_render_enabled")||!request.get("expected_render_enabled").isJsonPrimitive()
                    ||!request.getAsJsonPrimitive("expected_render_enabled").isBoolean()
                    ||enabled!=request.get("expected_render_enabled").getAsBoolean())
                throw new IllegalStateException("CPM render-enabled state changed or was not explicitly observed");
            observed=null;
            player.getMethod("setEnableRendering",boolean.class).invoke(null,!enabled);
            result.addProperty("action_result","official CPM render-enable API used by GestureGui checkbox and render-toggle key invoked; restore after baseline QA");
            return;
        }
        if(request.has("target")&&request.get("target").getAsString().equals("perspective")) {
            if(client.gui.screen()!=null||client.player.containerMenu!=client.player.inventoryMenu)
                throw new IllegalStateException("Perspective click requires the unobstructed fixture world");
            requireObservation(client,null,request);
            if(!client.options.getCameraType().name().equals(request.get("expected_perspective").getAsString()))
                throw new IllegalStateException("Perspective changed since the observed state");
            observed=null;
            KeyMapping.click(KeyMappingHelper.getBoundKeyOf(client.options.keyTogglePerspective));
            result.addProperty("action_result","ordinary bound perspective-key click queued; read next state and restore after QA");
            return;
        }
        Object frame=frame(client);requireFrame(frame);
        if(popup(frame))throw new IllegalStateException("Unexpected CPM popup");
        Observation old=requireObservation(client,frame,request);
        String path=request.get("button_path").getAsString(),label=request.get("label").getAsString();
        Row prior=old.buttons().get(path);
        Map<String,Row> now=new LinkedHashMap<>();
        collect(frame,"root",true,now,new JsonArray(),new JsonArray(),Collections.newSetFromMap(new IdentityHashMap<>()));
        Row row=now.get(path);
        if(prior==null||row==null||row.widget()!=prior.widget()||!row.active()||!prior.active()
                ||!row.label().equals(label)||!prior.label().equals(label))
            throw new IllegalStateException("Button is stale, inactive or unobserved");
        String operation="Models navigation";
        if(frame.getClass().getName().equals(GESTURES)) {
            if(!label.equals(modelsLabel(client.gui.screen())))throw new IllegalStateException("Only Models navigation permitted here");
        }else {
            Object panel=modelsPanel(frame);String selected=(String)field(panel,"selected");
            String filename=request.get("filename").getAsString();Approved approved=approvedModels().get(filename);
            if(approved==null||!approved.sha256().equals(request.get("sha256").getAsString())
                    ||!((String)field(panel,"selectedFolder")).isEmpty())
                throw new IllegalStateException("Model callback requires the exact approved root filename and SHA");
            if(row.modelFile()!=null&&filename.equals(row.modelFile())&&label.equals(i18n(client.gui.screen(),"button.cpm.select")))
                operation="approved native model Select";
            else if(row.widget()==field(panel,"set")&&filename.equals(selected)
                    &&label.equals(i18n(client.gui.screen(),"button.cpm.applySkin"))) {
                if(!serverStatus().equals("INSTALLED"))throw new IllegalStateException("Own fixture server must have CPM installed before Apply");
                operation="approved native local Apply and normal own-server update";
            }else throw new IllegalStateException("Only approved Select or local Apply allowed; public skin upload/editor denied");
            result.addProperty("approved_model",filename);result.addProperty("approved_model_sha256",approved.sha256());
        }
        Object callback=type("com.tom.cpl.gui.elements.Button").getMethod("getAction").invoke(row.widget());
        if(!(callback instanceof Runnable action))throw new IllegalStateException("No native CPM button callback");
        observed=null;
        action.run();
        result.addProperty("action_result",operation+" callback invoked; native readback/render/server receipt still required");
    }

    private static void state(Minecraft client,KeyMapping key,JsonObject result)throws Exception {
        JsonObject state=new JsonObject();
        state.addProperty("version",VERSION);state.addProperty("official_jar_sha256",SHA);
        state.addProperty("bound_gesture_key",key.getTranslatedKeyMessage().getString());
        state.addProperty("perspective",client.options.getCameraType().name());
        Class<?> nativePlayer=type("com.tom.cpm.shared.config.Player");
        state.addProperty("render_enabled",(boolean)nativePlayer.getMethod("isEnableRendering").invoke(null));
        Class<?> access=type("com.tom.cpm.shared.MinecraftClientAccess");
        Object actor=access.getMethod("getCurrentClientPlayer").invoke(access.getMethod("get").invoke(null));
        state.addProperty("current_player_definition_present",actor!=null&&nativePlayer.getMethod("getModelDefinition0").invoke(actor)!=null);
        state.addProperty("server_cpm_status",serverStatus());
        state.addProperty("local_runtime_model_directory",GAME.resolve("player_models").toString());
        state.addProperty("model_io_actions","only root-whitelisted native Select/local Apply; no editor/public upload/export");
        Object config=type("com.tom.cpm.shared.config.ModConfig").getMethod("getCommonConfig").invoke(null);
        String applied=(String)type("com.tom.cpl.config.ModConfigFile").getMethod("getString",String.class,String.class).invoke(config,"selectedModel",null);
        state.addProperty("applied_model",applied);
        if(applied!=null&&System.getProperty("valen.fixture.cpmModelsSha")!=null) {
            Approved approved=approvedModels().get(applied);
            if(approved!=null) {
                Class<?> model=type("com.tom.cpm.shared.io.ModelFile");
                Object file=model.getMethod("load",java.io.File.class).invoke(null,GAME.resolve("player_models").resolve(applied).toFile());
                byte[] bytes=(byte[])model.getMethod("getDataBlock").invoke(file);
                JsonObject transport=new JsonObject();transport.addProperty("filename",applied);
                transport.addProperty("data_bytes",bytes.length);
                transport.addProperty("data_sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
                transport.addProperty("origin","Exact ModelFile.getDataBlock representation consumed by normal NetworkUtil.sendSkinDataToServer; not a captured wire packet or whole-file hash");
                state.add("applied_transport_data",transport);
            }
        }
        Object frame=frame(client);
        if(frame==null){
            state.addProperty("frame","none");
            if(client.gui.screen()==null){String token=UUID.randomUUID().toString();observed=new Observation(token,null,null,System.nanoTime(),Map.of());state.addProperty("observation_id",token);}
            else observed=null;
        }
        else {
            state.addProperty("frame",frame.getClass().getName());
            boolean allowed=allowedFrame(frame);state.addProperty("navigation_scope_allowed",allowed);
            if(allowed) {
                state.addProperty("popup_open",popup(frame));
                JsonArray buttons=new JsonArray(),labels=new JsonArray();Map<String,Row> rows=new LinkedHashMap<>();
                collect(frame,"root",true,rows,buttons,labels,Collections.newSetFromMap(new IdentityHashMap<>()));
                String token=UUID.randomUUID().toString();
                observed=new Observation(token,client.gui.screen(),frame,System.nanoTime(),rows);
                state.addProperty("observation_id",token);state.add("buttons",buttons);state.add("labels",labels);
                state.addProperty("only_clickable_navigation",frame.getClass().getName().equals(GESTURES)?modelsLabel(client.gui.screen()):"approved Select/local Apply or observed cpm_back");
                if(frame.getClass().getName().equals(MODELS)) {
                    Object panel=modelsPanel(frame);state.addProperty("preview_selected_file",(String)field(panel,"selected"));
                    Object definition=type("com.tom.cpm.shared.gui.panel.ModelsPanel").getMethod("getSelectedDefinition").invoke(panel);
                    if(definition!=null) {
                        Class<?> def=type("com.tom.cpm.shared.definition.ModelDefinition");JsonObject preview=new JsonObject();
                        preview.addProperty("resolve_state",String.valueOf(def.getMethod("getResolveState").invoke(definition)));
                        preview.addProperty("do_render",(boolean)def.getMethod("doRender").invoke(definition));
                        preview.addProperty("error",String.valueOf(def.getMethod("getError").invoke(definition)));
                        Object cubes=field(definition,"cubes"),roots=field(definition,"rootRenderingCubes"),textures=field(definition,"textures");
                        preview.addProperty("native_cube_count",cubes instanceof List<?> c?c.size():0);
                        preview.addProperty("native_roots",roots instanceof Map<?,?> m?String.valueOf(m.keySet()):"unresolved");
                        preview.addProperty("native_texture_sheets",textures instanceof Map<?,?> m?String.valueOf(m.keySet()):"unresolved");
                        state.add("native_preview_definition",preview);
                    }
                }
            }else observed=null;
        }
        result.add("cpm_fixture",state);
    }

    private static void collect(Object element,String path,boolean ancestorsActive,Map<String,Row> rows,
            JsonArray buttons,JsonArray labels,Set<Object> seen)throws Exception {
        if(seen.size()>=2048)throw new IllegalStateException("CPM widget tree exceeds bounded readback");
        if(!seen.add(element))return;
        Class<?> guiElement=type("com.tom.cpl.gui.elements.GuiElement");
        boolean active=ancestorsActive&&(boolean)guiElement.getMethod("isVisible").invoke(element)
                &&(boolean)guiElement.getMethod("isEnabled").invoke(element);
        Class<?> button=type("com.tom.cpl.gui.elements.Button"),label=type("com.tom.cpl.gui.elements.Label");
        if(button.isInstance(element)) {
            String text=(String)button.getMethod("getText").invoke(element);
            String file=modelFile(element);rows.put(path,new Row(element,text,active,file));JsonObject item=new JsonObject();
            item.addProperty("path",path);item.addProperty("label",text);item.addProperty("active",active);
            if(file!=null)item.addProperty("model_filename",file);
            item.addProperty("class",element.getClass().getName());buttons.add(item);
        }else if(label.isInstance(element))labels.add((String)label.getMethod("getText").invoke(element));
        Class<?> panel=type("com.tom.cpl.gui.elements.Panel");
        Class<?> scroll=type("com.tom.cpl.gui.elements.ScrollPanel");
        if(scroll.isInstance(element)){Object display=scroll.getMethod("getDisplay").invoke(element);if(display!=null)collect(display,path+"/display",active,rows,buttons,labels,seen);}
        if(panel.isInstance(element)) {
            List<?> children=(List<?>)panel.getMethod("getElements").invoke(element);
            for(int i=0;i<children.size();i++)collect(children.get(i),path+"/"+i,active,rows,buttons,labels,seen);
        }
    }

    private static Object frame(Minecraft client)throws Exception {
        Screen screen=client.gui.screen();
        return screen!=null&&screen.getClass().getName().equals(GUI)
                ?type(GUI).getMethod("getFrame").invoke(screen):null;
    }
    private static boolean allowedFrame(Object frame){return frame!=null&&(frame.getClass().getName().equals(GESTURES)||frame.getClass().getName().equals(MODELS));}
    private static void requireFrame(Object frame){if(!allowedFrame(frame))throw new IllegalStateException("Not an inspected native CPM Gestures/Models frame");}
    private static boolean popup(Object frame)throws Exception{return (boolean)type("com.tom.cpl.gui.Frame").getMethod("hasPopupOpen").invoke(frame);}
    private static Observation requireObservation(Minecraft client,Object frame,JsonObject request) {
        Observation old=observed;
        if(old==null||old.screen()!=client.gui.screen()||old.frame()!=frame
                ||System.nanoTime()-old.at()>30_000_000_000L||!request.has("observation_id")
                ||!old.id().equals(request.get("observation_id").getAsString()))
            throw new IllegalStateException("Read a fresh cpm_state for this exact native screen first");
        return old;
    }
    private static String modelsLabel(Screen screen)throws Exception{return (String)type(GUI).getMethod("i18nFormat",String.class,Object[].class).invoke(screen,"button.cpm.models",new Object[0]);}
    private static String i18n(Screen screen,String key)throws Exception{return (String)type(GUI).getMethod("i18nFormat",String.class,Object[].class).invoke(screen,key,new Object[0]);}
    private static Object modelsPanel(Object frame)throws Exception{return field(frame,"panel");}
    private static Object field(Object object,String name)throws Exception {
        for(Class<?> c=object.getClass();c!=null;c=c.getSuperclass())try{var f=c.getDeclaredField(name);f.setAccessible(true);return f.get(object);}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(name);
    }
    private static String modelFile(Object button)throws Exception {
        Object action=type("com.tom.cpl.gui.elements.Button").getMethod("getAction").invoke(button);
        if(action==null||!action.getClass().getName().startsWith("com.tom.cpm.shared.gui.panel.ModelsPanel$ModelPanel$$Lambda"))return null;
        for(var f:action.getClass().getDeclaredFields())if(f.getType().getName().equals("com.tom.cpm.shared.io.ModelFile")) {
            f.setAccessible(true);return (String)type("com.tom.cpm.shared.io.ModelFile").getMethod("getFileName").invoke(f.get(action));
        }
        return null;
    }
    private static String serverStatus()throws Exception {
        Class<?> access=type("com.tom.cpm.shared.MinecraftClientAccess");Object client=access.getMethod("get").invoke(null);
        return String.valueOf(access.getMethod("getServerSideStatus").invoke(client));
    }
    private static Map<String,Approved> approvedModels()throws Exception {
        String pin=System.getProperty("valen.fixture.cpmModelsSha","");Path whitelist=GAME.resolve("valen-cpm-approved-models.json");
        if(!pin.matches("[0-9a-f]{64}")||!sha(whitelist).equals(pin))throw new IllegalStateException("Root-approved model whitelist SHA mismatch");
        JsonObject list=JsonParser.parseString(Files.readString(whitelist)).getAsJsonObject();
        if(list.get("schema").getAsInt()!=1||!list.get("source_manifest_sha256").getAsString().matches("[0-9a-f]{64}")||list.getAsJsonArray("models").size()!=6)
            throw new IllegalStateException("Expected exactly six root-approved runtime models");
        Path root=GAME.resolve("player_models");if(Files.isSymbolicLink(root))throw new IllegalStateException("Model directory must be fixture-owned");root=root.toRealPath();
        Map<String,Approved> approved=new LinkedHashMap<>();
        for(JsonElement e:list.getAsJsonArray("models")) {
            JsonObject m=e.getAsJsonObject();String filename=m.get("filename").getAsString(),hash=m.get("sha256").getAsString();
            if(filename.contains("/")||filename.contains("\\")||!filename.endsWith(".cpmmodel")||!hash.matches("[0-9a-f]{64}"))throw new IllegalStateException("Invalid approved model basename/SHA");
            Path file=root.resolve(filename);
            if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||!file.toRealPath().getParent().equals(root)||!sha(file).equals(hash)
                    ||approved.put(filename,new Approved(m.get("id").getAsString(),filename,hash))!=null)
                throw new IllegalStateException("Approved model bytes/path/uniqueness mismatch");
        }
        try(var files=Files.list(root)){if(files.anyMatch(p->!approved.containsKey(p.getFileName().toString())))throw new IllegalStateException("Only root-staged six files may appear in the model directory");}
        return approved;
    }
    private static Class<?> type(String name)throws ClassNotFoundException{return Class.forName(name,false,CpmFixtureClientActions.class.getClassLoader());}
    private static String sha(Path path)throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        try(var in=Files.newInputStream(path)){byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);}
        return HexFormat.of().formatHex(digest.digest());
    }
}
