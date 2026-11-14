package valen.spacereloadedprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import org.alex_melan.spacereloaded.registry.ModItems;

/** Disposable-only visual acceptance. Never packaged in the production mod. */
public final class SpaceClientProbe implements ClientModInitializer {
    private static final Map<String,Integer> FLUID_EXPECTED_MATERIALS = Map.ofEntries(
        Map.entry("minecraft:water",10001),
        Map.entry("minecraft:lava",10106),
        Map.entry("create:potion",10126),
        Map.entry("create:tea",10126),
        Map.entry("create:milk",10126),
        Map.entry("create:honey",10126),
        Map.entry("create:chocolate",10126),
        Map.entry("aerialhell:liquid_of_the_gods_source",10126),
        Map.entry("buildcraftenergy:oil",10126),
        Map.entry("buildcraftenergy:oil_heat_1",10126),
        Map.entry("buildcraftenergy:oil_heat_2",10126),
        Map.entry("buildcraftenergy:oil_residue",10126),
        Map.entry("buildcraftenergy:oil_residue_heat_1",10126),
        Map.entry("buildcraftenergy:oil_residue_heat_2",10126),
        Map.entry("buildcraftenergy:oil_heavy",10126),
        Map.entry("buildcraftenergy:oil_heavy_heat_1",10126),
        Map.entry("buildcraftenergy:oil_heavy_heat_2",10126),
        Map.entry("buildcraftenergy:oil_dense",10126),
        Map.entry("buildcraftenergy:oil_dense_heat_1",10126),
        Map.entry("buildcraftenergy:oil_dense_heat_2",10126),
        Map.entry("buildcraftenergy:oil_distilled",10126),
        Map.entry("buildcraftenergy:oil_distilled_heat_1",10126),
        Map.entry("buildcraftenergy:oil_distilled_heat_2",0),
        Map.entry("buildcraftenergy:fuel_dense",10126),
        Map.entry("buildcraftenergy:fuel_dense_heat_1",10126),
        Map.entry("buildcraftenergy:fuel_dense_heat_2",0),
        Map.entry("buildcraftenergy:fuel_mixed_heavy",10126),
        Map.entry("buildcraftenergy:fuel_mixed_heavy_heat_1",10126),
        Map.entry("buildcraftenergy:fuel_mixed_heavy_heat_2",0),
        Map.entry("buildcraftenergy:fuel_light",10126),
        Map.entry("buildcraftenergy:fuel_light_heat_1",0),
        Map.entry("buildcraftenergy:fuel_light_heat_2",0),
        Map.entry("buildcraftenergy:fuel_mixed_light",10126),
        Map.entry("buildcraftenergy:fuel_mixed_light_heat_1",0),
        Map.entry("buildcraftenergy:fuel_mixed_light_heat_2",0),
        Map.entry("buildcraftenergy:fuel_gaseous",0),
        Map.entry("buildcraftenergy:fuel_gaseous_heat_1",0),
        Map.entry("buildcraftenergy:fuel_gaseous_heat_2",0),
        Map.entry("biomesoplenty:blood",10126),
        Map.entry("biomesoplenty:liquid_null",10126),
        Map.entry("nerospace:rocket_fuel",10126),
        Map.entry("spacereloaded:kerolox",10126),
        Map.entry("spacereloaded:hydrolox",10126),
        Map.entry("spacereloaded:methalox",10126),
        Map.entry("techreborn:beryllium",10124),
        Map.entry("techreborn:calcium",10124),
        Map.entry("techreborn:calcium_carbonate",0),
        Map.entry("techreborn:carbon",0),
        Map.entry("techreborn:carbon_fiber",0),
        Map.entry("techreborn:chlorite",0),
        Map.entry("techreborn:compressed_air",0),
        Map.entry("techreborn:deuterium",0),
        Map.entry("techreborn:diesel",10126),
        Map.entry("techreborn:electrolyzed_water",10126),
        Map.entry("techreborn:glyceryl",10126),
        Map.entry("techreborn:helium",0),
        Map.entry("techreborn:helium3",0),
        Map.entry("techreborn:heliumplasma",0),
        Map.entry("techreborn:hydrogen",0),
        Map.entry("techreborn:lithium",10124),
        Map.entry("techreborn:mercury",10126),
        Map.entry("techreborn:methane",0),
        Map.entry("techreborn:nitro_carbon",0),
        Map.entry("techreborn:nitro_diesel",10126),
        Map.entry("techreborn:nitrocoal_fuel",10126),
        Map.entry("techreborn:nitrofuel",10126),
        Map.entry("techreborn:nitrogen",0),
        Map.entry("techreborn:nitrogen_dioxide",0),
        Map.entry("techreborn:oil",10126),
        Map.entry("techreborn:potassium",10124),
        Map.entry("techreborn:silicon",0),
        Map.entry("techreborn:sodium",10124),
        Map.entry("techreborn:sodium_sulfide",0),
        Map.entry("techreborn:sodium_persulfate",0),
        Map.entry("techreborn:sulfur",0),
        Map.entry("techreborn:sulfuric_acid",10126),
        Map.entry("techreborn:tritium",0),
        Map.entry("techreborn:wolframium",10124),
        Map.entry("techreborn:biofuel",10126),
        Map.entry("techreborn:fluorine",0),
        Map.entry("techreborn:nitric_acid",10126),
        Map.entry("techreborn:uranium_hexafluoride",0),
        Map.entry("travelersbackpack:potion_still",10126));
    private static final Map<String,Integer> TWILIGHT_CATEGORIES = Map.of(
        "twilightforest:uncrafting",4960,"twilightforest:crumble_horn",63,
        "twilightforest:drying",35,"twilightforest:transformation_powder",32,
        "twilightforest:ominous_fire",3,"twilightforest:modifier_removal",4);
    private final Map<String,Object> report = new LinkedHashMap<>();
    private final Set<String> dimensions = new LinkedHashSet<>();
    private final long started = System.currentTimeMillis();
    private int ticks;
    private int thrustTicks;
    private int paneSweepTick = -1, paneSweepRun;
    private boolean paneCapturePending;
    private final List<Map<String,Object>> paneFrames = new ArrayList<>();
    private boolean modelsChecked;

    public void onInitializeClient() {
        report.put("scope", "native client models, server-provided recipes, equipment HUD and planet views");
        report.put("dimensions_seen", dimensions);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Path game = client.gameDirectory.toPath();
            try {
                if (Files.exists(game.resolve("finish-client")) || System.currentTimeMillis()-started > 1080000) {
                    write(game); client.stop(); return;
                }
                if (thrustTicks > 0) {
                    if (--thrustTicks == 35) Screenshot.grab(client.gameDirectory, "space-r3-rocket-plume.png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
                    if (thrustTicks == 0) client.options.keyJump.setDown(false);
                }
                if (paneSweepTick >= 0) {
                    if (client.player == null || client.level == null || client.gui.screen() != null)
                        throw new AssertionError("Pane sweep requires an uninterrupted disposable world view");
                    int step = paneSweepTick++;
                    client.player.turn((step < 20 || step >= 60 ? -0.5 : 0.5) / 0.15, 0.0);
                    if (step % 4 == 3 && !paneCapturePending) {
                        paneCapturePending = true;
                        String name = "pane-motion-" + paneSweepRun + "-" + String.format("%02d", step) + ".png";
                        var frame = new LinkedHashMap<String,Object>();
                        frame.put("file",name); frame.put("requested_at",Instant.now().toString());
                        frame.put("step",step); frame.put("requested_yaw",client.player.getYRot());
                        frame.put("position",List.of(client.player.getX(),client.player.getY(),client.player.getZ()));
                        report.put("pane_sweep_capture_pending",true);
                        Screenshot.grab(client.gameDirectory,name,client.gameRenderer.mainRenderTarget(),1,
                            message -> client.execute(() -> {
                                paneCapturePending=false; frame.put("saved_at",Instant.now().toString());
                                paneFrames.add(frame); report.put("pane_sweep_frames",paneFrames);
                                report.put("pane_sweep_capture_pending",false); write(game);
                            }));
                    }
                    if (paneSweepTick == 80) {
                        paneSweepTick = -1; report.put("pane_sweep_active",false);
                        report.put("pane_sweep_end_yaw",client.player.getYRot()); write(game);
                    }
                }
                if (++ticks % 20 != 0) return;
                report.put("latest_at", Instant.now().toString());
                report.put("screen", client.gui.screen()==null ? "world" : client.gui.screen().getClass().getName());
                if (client.level == null || client.player == null || client.getConnection() == null) {write(game);return;}
                String dimension=client.level.dimension().identifier().toString();
                dimensions.add(dimension);
                report.put("dimension",dimension);
                report.put("position",List.of(client.player.getX(),client.player.getY(),client.player.getZ()));
                report.put("shader_in_use",IrisApi.getInstance().isShaderPackInUse());
                var sky = client.gameRenderer.gameRenderState().levelRenderState.skyRenderState;
                var cam = client.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
                report.put("sky",Map.of("color",String.valueOf(sky.skyColor),"skybox",String.valueOf(sky.skybox),
                    "rain",sky.rainBrightness,"fog_color",String.valueOf(cam.fogData.color),"fog_type",String.valueOf(cam.fogType)));

                report.put("vehicle",client.player.getVehicle()==null ? "none" : client.player.getVehicle().getType().toString());
                var shaderIds=net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings.INSTANCE;
                if(shaderIds.getEntityIds()!=null && shaderIds.getBlockStateIds()!=null) report.put("shader_markers",Map.of(
                    "rocket",shaderIds.getEntityIds().getInt(new net.irisshaders.iris.shaderpack.materialmap.NamespacedId("spacereloaded","rocket")),
                    "lightning",shaderIds.getEntityIds().getInt(new net.irisshaders.iris.shaderpack.materialmap.NamespacedId("minecraft","lightning_bolt")),
                    "eclss_controller",shaderIds.getBlockStateIds().getInt(BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("spacereloaded:eclss_controller")).defaultBlockState())));

                report.put("head_item",BuiltInRegistries.ITEM.getKey(client.player.getItemBySlot(EquipmentSlot.HEAD).getItem()).toString());
                int oxygen=0,capacity=0;
                for (int i=0;i<client.player.getInventory().getContainerSize();i++) {
                    ItemStack s=client.player.getInventory().getItem(i);
                    if (s.is(ModItems.OXYGEN_CANISTER)){oxygen+=s.getMaxDamage()-s.getDamageValue();capacity+=s.getMaxDamage();}
                }
                report.put("space_canisters",Map.of("oxygen",oxygen,"capacity",capacity));
                if (!modelsChecked) {if (!Boolean.getBoolean("valen.probe.tech")) checkModels(client);modelsChecked=true;}
                if (!PluginManager.areAnyReloading()) {
                    Map<String,Integer> categories=new TreeMap<>();
                    DisplayRegistry.getInstance().getAll().forEach((id,displays)-> {
                        if(id.getNamespace().equals("spacereloaded"))categories.put(id.toString(),displays.size());
                    });
                    report.put("space_recipe_categories",categories);
                    report.put("space_recipe_total",categories.values().stream().mapToInt(Integer::intValue).sum());
                    Map<String,Integer> techCategories=new TreeMap<>();
                    DisplayRegistry.getInstance().getAll().forEach((id,displays)-> {
                        if(id.getNamespace().equals("workingvillagers") || id.toString().equals("jeg:vehicle_assembly"))techCategories.put(id.toString(),displays.size());
                    });
                    report.put("tech_recipe_categories",techCategories);
                }
                if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("espiritual-magic")) {
                    Object data=Class.forName("com.sumiqueso.client.ClientRaceState").getMethod("get").invoke(null);
                    if(data!=null) {
                        var state=new TreeMap<String,Object>();
                        for(String method:List.of("raceName","spiritualEnergy","maxSpiritualEnergy","infiniteSpiritualEnergy"))
                            state.put(method,data.getClass().getMethod(method).invoke(data));
                        report.put("spiritual_synced_hud",state);
                    }
                }
                Path request=game.resolve("space-client-request.txt");
                if(Files.exists(request)) {
                    String command=Files.readString(request).trim();Files.delete(request);
                    if(command.equals("world"))client.gui.setScreen(null);
                    else if(command.equals("ack-jeg")) {
                        var screen=client.gui.screen();
                        if(screen==null || !screen.getClass().getName().equals("ttv.migami.jeg.client.MagazineModeClient$1"))
                            throw new AssertionError("Expected native JEG ammunition-rules screen");
                        var buttons=screen.children().stream().filter(child->child instanceof net.minecraft.client.gui.components.Button)
                            .map(child->(net.minecraft.client.gui.components.Button)child)
                            .filter(button->button.getMessage().getString().equals("Got it")).toList();
                        if(buttons.size()!=1 || !buttons.getFirst().active) throw new AssertionError("Native Got it button unavailable");
                        buttons.getFirst().onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN,13,0));
                        report.put("jeg_native_acknowledged",client.gui.screen()!=screen);
                    }
                    else if(command.equals("book-next")) {
                        var screen=client.gui.screen();
                        if (!(screen instanceof net.minecraft.client.gui.screens.inventory.LecternScreen))
                            throw new AssertionError("Expected native LecternScreen");
                        var button=screen.children().stream()
                            .filter(child->child instanceof net.minecraft.client.gui.screens.inventory.PageButton)
                            .map(child->(net.minecraft.client.gui.screens.inventory.PageButton)child)
                            .max(java.util.Comparator.comparingInt(net.minecraft.client.gui.components.AbstractWidget::getX)).orElseThrow();
                        if(!button.active || !button.visible) throw new AssertionError("Native forward page unavailable");
                        button.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN,13,0));
                        report.put("native_lectern_page_forward",((Number)report.getOrDefault("native_lectern_page_forward",0)).intValue()+1);
                    }
                    else if(command.equals("content-materials")) {
                        var ids=net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings.INSTANCE.getBlockStateIds();
                        if(ids==null)throw new AssertionError("Native Iris material map unavailable");
                        Map<String,Integer> expected=Map.ofEntries(
                            Map.entry("spacereloaded:hermetic_glass",10081),
                            Map.entry("spacereloaded:moon_ice",10008),
                            Map.entry("spacereloaded:mars_ice",10008),
                            Map.entry("spacereloaded:hull_plating",10012),
                            Map.entry("spacereloaded:module_hull",10012),
                            Map.entry("spacereloaded:rocket_hull",10012),
                            Map.entry("spacereloaded:steel_shaft",10012),
                            Map.entry("spacereloaded:small_gear",10012),
                            Map.entry("spacereloaded:large_gear",10012),
                            Map.entry("spacereloaded:steel_coil",10012),
                            Map.entry("spacereloaded:rotor_steel_shaft",10012),
                            Map.entry("spacereloaded:rotor_small_gear",10012),
                            Map.entry("spacereloaded:rotor_large_gear",10012));
                        var actual=new TreeMap<String,Integer>();
                        var materialErrors=new ArrayList<String>();
                        for(var entry:expected.entrySet()) {
                            var block=BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(entry.getKey()));
                            if(block==net.minecraft.world.level.block.Blocks.AIR){materialErrors.add("Missing block "+entry.getKey());actual.put(entry.getKey(),-1);continue;}
                            int value=ids.getInt(block.defaultBlockState());actual.put(entry.getKey(),value);
                            if(value!=entry.getValue())materialErrors.add(entry.getKey()+" material "+value+" expected "+entry.getValue());
                        }
                        var heart=net.minecraft.world.level.block.Blocks.CREAKING_HEART;
                        for(var state:heart.getStateDefinition().getPossibleStates()) {
                            var value=state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.CREAKING_HEART_STATE).toString();
                            int expectedId=value.equals("uprooted")?10029:10035;
                            int id=ids.getInt(state);actual.put(state.toString(),id);
                            if(id!=expectedId)throw new AssertionError(state+" material "+id+" expected "+expectedId);
                        }
                        report.put("content_material_ids",actual);report.put("content_materials_pass",materialErrors.isEmpty());
                        Files.writeString(game.resolve("content-materials-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(actual));
                        if(!materialErrors.isEmpty())throw new AssertionError(materialErrors.toString());
                    }
                    else if(command.equals("metal-fluid-materials")) {
                        var ids=net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings.INSTANCE.getBlockStateIds();
                        if(ids==null)throw new AssertionError("Native Iris material map unavailable");
                        var actual=new TreeMap<String,Integer>();
                        var errors=new ArrayList<String>();
                        for(String name:List.of("beryllium","calcium","lithium","potassium","sodium","wolframium","mercury")) {
                            var block=BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("techreborn:"+name));
                            if(block==net.minecraft.world.level.block.Blocks.AIR){errors.add("Missing native fluid block "+name);continue;}
                            int count=0;
                            for(var state:block.getStateDefinition().getPossibleStates()) {
                                int level=state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL),id=ids.getInt(state);
                                actual.put("techreborn:"+name+"[level="+level+"]",id);count++;
                                if(name.equals("mercury")) {
                                    if(id==10124||id==10125)errors.add("Mercury acquired molten mapping "+id);
                                } else if(id!=(level==0?10124:10125)) errors.add(state+" mapped "+id);
                            }
                            if(count!=16)errors.add(name+" native fluid states "+count);
                        }
                        for(var state:net.minecraft.world.level.block.Blocks.LAVA.getStateDefinition().getPossibleStates()) {
                            int level=state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL),id=ids.getInt(state);
                            actual.put("minecraft:lava[level="+level+"]",id);
                            if(id!=(level==0?10106:10105))errors.add(state+" mapped "+id);
                        }
                        report.put("metal_fluid_material_ids",actual);report.put("metal_fluid_materials_pass",errors.isEmpty());
                        Files.writeString(game.resolve("metal-fluid-materials-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(actual));
                        if(!errors.isEmpty())throw new AssertionError(errors.toString());
                    }
                    else if(command.equals("tank-fluid-materials")) { checkTankMaterials(game); }
                    else if(command.equals("tech-recipes")) {
                        Map<String,Integer> expected=Map.of("workingvillagers:milling",11,"workingvillagers:woodcutting",185,"jeg:vehicle_assembly",6);
                        for(var entry:expected.entrySet()) {
                            var values=DisplayRegistry.getInstance().get(CategoryIdentifier.of(entry.getKey()));
                            if(values.size()!=entry.getValue()) throw new AssertionError("Native synced "+entry.getKey()+" displays "+values.size()+" expected "+entry.getValue());
                        }
                        report.put("tech_recipe_counts_pass",true);
                    }
                    else if(command.equals("twilight-recipes")) {
                        requireTwilight();
                        Map<String,Integer> actual=new TreeMap<>();
                        for(var entry:TWILIGHT_CATEGORIES.entrySet()) {
                            int count=DisplayRegistry.getInstance().get(CategoryIdentifier.of(entry.getKey())).size();
                            actual.put(entry.getKey(),count);
                        }
                        report.put("twilight_recipe_categories",actual);
                        if(!actual.equals(TWILIGHT_CATEGORIES))
                            throw new AssertionError("Native synced Twilight categories "+actual+" expected "+TWILIGHT_CATEGORIES);
                        report.put("twilight_recipe_counts_pass",true);
                    }
                    else if(command.equals("twilight-portal-materials")) checkTwilightPortal(client);
                    else if(command.equals("keys")) dumpKeys(client,game);
                    else if(command.startsWith("set-key:")) {
                        String[] parts=command.split(":",3);
                        var key=Arrays.stream(client.options.keyMappings).filter(k->k.getName().equals(parts[1])&&k.getName().contains("espiritual")).findFirst().orElseThrow();
                        String old=key.saveString();key.setKey(com.mojang.blaze3d.platform.InputConstants.getKey(parts[2]));
                        net.minecraft.client.KeyMapping.resetMapping();client.options.save();
                        report.put("last_key_change",Map.of("name",key.getName(),"before",old,"after",key.saveString()));
                        dumpKeys(client,game);
                    }
                    else if(command.equals("respawn")) {client.player.respawn();client.gui.setScreen(null);}
                    else if(command.equals("reload-shaders"))net.irisshaders.iris.Iris.reload();
                    else if(command.equals("equipment-pbr-state")) {
                        var id=net.minecraft.resources.Identifier.parse("techreborn:textures/entity/equipment/humanoid/steel.png");
                        var texture=client.getTextureManager().getTexture(id);
                        var j=new java.util.LinkedHashMap<String,Object>();j.put("texture",id.toString());j.put("class",texture.getClass().getName());j.put("specular_resource",client.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("techreborn:textures/entity/equipment/humanoid/steel_s.png")).isPresent());
                        j.put("loader",String.valueOf(net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry.INSTANCE.getLoader(texture.getClass())));
                        var gpu=texture.getTexture();j.put("gpu_class",gpu.getClass().getName());
                        for(var f:gpu.getClass().getDeclaredFields())if(f.getType()==int.class&&!java.lang.reflect.Modifier.isStatic(f.getModifiers())){f.setAccessible(true);j.put("gpu_field_"+f.getName(),f.getInt(gpu));}
                        Files.writeString(game.resolve("equipment-pbr-state.json"),new GsonBuilder().setPrettyPrinting().create().toJson(j));report.put("equipment_pbr_state",j);
                    }
                    else if(command.equals("tank-render-state")) {
                        var be=(com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity)client.level.getBlockEntity(new net.minecraft.core.BlockPos(44,202,60));
                        var renderer=(com.zurrtum.create.client.content.fluids.tank.FluidTankRenderer)(Object)client.getBlockEntityRenderDispatcher().getRenderer(be);
                        var state=renderer.createRenderState();renderer.extractRenderState(be,state,1.0f,client.player.position(),null);
                        var j=new java.util.LinkedHashMap<String,Object>();j.put("window",be.window);j.put("controller",be.isController());j.put("width",be.getWidth());j.put("height",be.getHeight());j.put("fill",be.getFillState());j.put("fluid",be.getTankInventory().getFluid().toString());j.put("fluid_level",be.getFluidLevel()==null?null:be.getFluidLevel().getValue(1));j.put("extracted",state.fluid!=null);j.put("translate",state.translate);j.put("render_state",String.valueOf(state.fluid));
                        Files.writeString(game.resolve("tank-render-state.json"),new GsonBuilder().setPrettyPrinting().create().toJson(j));report.put("tank_render_state",j);
                    }
                    else if(command.equals("enable-material-pack")) {
                        var packs=client.getResourcePackRepository();packs.reload();
                        var selected=new java.util.ArrayList<String>(packs.getSelectedIds());
                        if(!selected.contains("file/Valen-Material-Surfaces.zip"))selected.add("file/Valen-Material-Surfaces.zip");
                        packs.setSelected(selected);client.options.updateResourcePacks(packs);client.reloadResourcePacks();
                    }


                    else if(command.equals("third-person"))client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    else if(command.equals("first-person"))client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                    else if(command.equals("use-block")) {
                        if (!(client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)) throw new AssertionError("No native block hit");
                        report.put("block_used",hit.getBlockPos().toString());
                        client.gameMode.useItemOn(client.player,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
                    }
                    else if(command.equals("press-key:key.keyboard.f12") || command.equals("press-key:key.keyboard.m")) {
                        if(client.gui.screen()!=null)throw new AssertionError("Key check requires world screen");
                        String key=command.substring("press-key:".length());
                        net.minecraft.client.KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(key));
                        report.put("native_key_clicked",key);
                    }
                    else if(command.equals("pane-sweep")) {
                        if(client.gui.screen()!=null || paneSweepTick>=0 || paneCapturePending)
                            throw new AssertionError("Pane sweep requires a free world camera and no pending capture");
                        paneSweepTick=0; paneSweepRun++; paneFrames.clear();
                        report.put("pane_sweep_active",true); report.put("pane_sweep_start_yaw",client.player.getYRot());
                    }
                    else if(command.equals("thrust-brief")) {client.options.keyJump.setDown(true);thrustTicks=60;}
                    else if(command.equals("inventory"))client.gui.setScreen(new InventoryScreen(client.player));
                    else if((command.startsWith("recipe:spacereloaded:") || command.startsWith("recipe:workingvillagers:") || command.equals("recipe:jeg:vehicle_assembly") || (command.startsWith("recipe:") && TWILIGHT_CATEGORIES.containsKey(command.substring(7))))) {
                        String id=command.substring(7);
                        if(id.startsWith("twilightforest:"))requireTwilight();
                        report.put("last_recipe_open",Map.of("id",id,"opened",ViewSearchBuilder.builder().addCategory(CategoryIdentifier.of(id)).open()));
                    } else if(command.matches("[a-zA-Z0-9_-]+\\.png")) {
                        Screenshot.grab(client.gameDirectory,command,client.gameRenderer.mainRenderTarget(),1,
                            message->client.execute(()->{report.put("last_capture",command);write(game);}));
                    } else throw new IllegalArgumentException(command);
                }
                write(game);
            } catch (ConcurrentModificationException reloadStarted) {
                report.put("reload_deferred",true);
            } catch(Throwable error) {
                report.put("error",error.toString());error.printStackTrace();write(game);client.stop();
            }
        });
    }
    private static void requireTwilight() {
        if(!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("twilightforest"))
            throw new AssertionError("Twilight client action requires the optional mod");
    }
    private void checkTwilightPortal(Minecraft client) {
        requireTwilight();
        var id=net.minecraft.resources.Identifier.parse("twilightforest:twilight_portal");
        if(!BuiltInRegistries.BLOCK.containsKey(id))throw new AssertionError("Missing registered Twilight portal");
        var block=BuiltInRegistries.BLOCK.getValue(id);
        var models=client.getModelManager().getBlockStateModelSet();
        var rows=new ArrayList<Map<String,Object>>();
        var faces=new ArrayList<Direction>(Arrays.asList(Direction.values()));faces.add(null);
        int portalQuads=0,barrierQuads=0;
        for(var state:block.getStateDefinition().getPossibleStates()) {
            var model=models.get(state);
            if(model==models.missingModel())throw new AssertionError("Missing Twilight portal model: "+state);
            var parts=new ArrayList<BlockStateModelPart>();model.collectParts(RandomSource.create(42),parts);
            for(var part:parts)for(var face:faces)for(var quad:part.getQuads(face)) {
                var material=quad.materialInfo();
                String sprite=material.sprite().contents().name().toString();
                rows.add(Map.of("state",state.toString(),"sprite",sprite,"layer",material.layer().name(),
                    "flags",material.flags(),"direction",quad.direction().toString(),"tint",material.tintIndex(),
                    "y",quad.position0().y()));
                report.put("twilight_portal_materials",rows);
                if(sprite.equals("minecraft:block/nether_portal")) {
                    portalQuads++;
                    if(quad.direction()!=Direction.UP || !material.layer().translucent() || (material.flags()&1)==0)
                        throw new AssertionError("Twilight normal UP portal did not bake as translucent: "+rows.getLast());
                } else if(sprite.equals("twilightforest:block/portal_barrier"))barrierQuads++;
                else throw new AssertionError("Unexpected Twilight portal sprite: "+sprite);
            }
        }
        if(portalQuads==0 || barrierQuads==0)throw new AssertionError("Missing normal portal or distinct one-way barrier quads");
        report.put("twilight_portal_materials_pass",true);
    }
    private void dumpKeys(Minecraft client, Path game) throws Exception {
        var keys=new ArrayList<Map<String,String>>();
        var groups=new TreeMap<String,List<String>>();
        for(var key:client.options.keyMappings) {
            String value=key.saveString();
            keys.add(Map.of("name",key.getName(),"key",value,"default",key.getDefaultKey().getName(),"category",key.getCategory().toString()));
            if(!value.equals("key.keyboard.unknown")) groups.computeIfAbsent(value,k->new ArrayList<>()).add(key.getName());
        }
        groups.entrySet().removeIf(e->e.getValue().size()<2);
        Files.writeString(game.resolve("tech-keybindings.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("keys",keys,"shared_bindings",groups))+"\n");
        report.put("keybindings_dumped",keys.size());
    }
    private void checkModels(Minecraft client) {
        var models=client.getModelManager().getBlockStateModelSet();
        int blocks=0,states=0,items=0,quads=0;
        Set<String> sprites=new TreeSet<>();
        List<Direction> faces=new ArrayList<>(Arrays.asList(Direction.values()));faces.add(null);
        for(var block:BuiltInRegistries.BLOCK) {
            if(!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("spacereloaded"))continue;
            blocks++;
            for(var state:block.getStateDefinition().getPossibleStates()) {
                var model=models.get(state);
                if(model==models.missingModel())throw new AssertionError("Missing block model: "+state);
                List<BlockStateModelPart> parts=new ArrayList<>();model.collectParts(RandomSource.create(42),parts);
                for(var part:parts)for(var face:faces)for(var quad:part.getQuads(face)) {
                    String name=quad.materialInfo().sprite().contents().name().toString();
                    if(name.contains("missingno"))throw new AssertionError("Missing texture: "+state);
                    sprites.add(name);quads++;
                }
                states++;
            }
        }
        for(var item:BuiltInRegistries.ITEM) {
            if(!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("spacereloaded"))continue;
            var state=new ItemStackRenderState();
            client.getItemModelResolver().updateForTopItem(state,new ItemStack(item),ItemDisplayContext.GUI,client.level,client.player,0);
            if(state.isEmpty())throw new AssertionError("Empty item model: "+BuiltInRegistries.ITEM.getKey(item));
            items++;
        }
        report.put("models",Map.of("blocks",blocks,"block_states",states,"quads",quads,"items",items,"textures",sprites));
    }
    private void write(Path game) {
        try{Files.writeString(game.resolve("space-client-acceptance.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
        catch(Exception e){throw new RuntimeException(e);}
    }

    private void checkTankMaterials(Path game) throws Exception {
        var actual=new TreeMap<String,Object>();
        var renderClass=net.minecraft.client.renderer.rendertype.RenderType.class;
        var fluidClass=net.minecraft.world.level.material.Fluid.class;
        String[] owners={"com.zurrtum.create.client.catnip.render.MoltenFluidRenderTypes","buildcraft.lib.client.fluid.MoltenFluidRenderTypes"};
        net.minecraft.client.renderer.rendertype.RenderType[][] bases={
            {net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock(),net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()},
            {buildcraft.lib.client.render.BCLibRenderTypes.entityCutout(buildcraft.lib.client.texture.BcTextureAtlases.BLOCKS_TEXTURE),buildcraft.lib.client.render.BCLibRenderTypes.entityTranslucent(buildcraft.lib.client.texture.BcTextureAtlases.BLOCKS_TEXTURE)}};
        int checks=0;
        for(int owner=0;owner<owners.length;owner++) {
            var type=Class.forName(owners[owner]);
            var select=type.getMethod("select",renderClass,fluidClass,boolean.class);
            actual.put(owners[owner]+"/origin",type.getProtectionDomain().getCodeSource().getLocation().toString());
            for(String id:FLUID_EXPECTED_MATERIALS.keySet()) {
                var fluid=BuiltInRegistries.FLUID.getOptional(net.minecraft.resources.Identifier.parse(id)).orElseThrow();
                int stillMarker=FLUID_EXPECTED_MATERIALS.get(id);
                boolean chosen=stillMarker!=0;
                for(int index=0;index<bases[owner].length;index++)for(boolean flow:new boolean[]{false,true}) {
                    var base=bases[owner][index];
                    var layer=(net.minecraft.client.renderer.rendertype.RenderType)select.invoke(null,base,fluid,flow);
                    if(!chosen) {
                        if(layer!=base)throw new AssertionError("Nonselected fluid changed layer: "+id);
                        actual.put(owners[owner]+"/"+index+"/"+id+"/"+flow,"original");checks++;continue;
                    }
                    if(layer==base||!layer.getClass().getName().equals("net.irisshaders.iris.layer.OuterWrappedRenderType"))
                        throw new AssertionError("Selected fluid lacks cached native wrapper: "+id+" "+layer);
                    var extra=layer.getClass().getDeclaredField("extra");extra.setAccessible(true);
                    Object marker=extra.get(layer);var markerField=marker.getClass().getDeclaredField("marker");markerField.setAccessible(true);
                    int value=markerField.getInt(marker),expected=!flow||stillMarker==10001?stillMarker:stillMarker==10106?10105:stillMarker+1;
                    if(value!=expected)throw new AssertionError(id+" marker "+value+" expected "+expected);
                    var expectedBase=owner==0?net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS):bases[owner][bases[owner].length-1];
                    if(layer.pipeline()!=expectedBase.pipeline()||layer.format()!=expectedBase.format()||layer.primitiveTopology()!=base.primitiveTopology()||!layer.hasBlending())
                        throw new AssertionError("Native pipeline/format/topology/blend changed: "+id);
                    if(select.invoke(null,base,fluid,flow)!=layer)throw new AssertionError("Per-call wrapper creation: "+id);
                    if(fluid instanceof net.minecraft.world.level.material.FlowingFluid f && select.invoke(null,base,f.getFlowing(),flow)!=layer)
                        throw new AssertionError("Flowing fluid identity failed canonicalization: "+id);
                    var capturedState=net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE;
                    int beforeBe=capturedState.getCurrentRenderedBlockEntity();
                    int beforeEntity=capturedState.getCurrentRenderedEntity();
                    int beforeItem=capturedState.getCurrentRenderedItem();
                    var captured=new java.util.concurrent.atomic.AtomicReference<net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit>();
                    try {
                        type.getMethod("submit",renderClass,Runnable.class).invoke(null,layer,(Runnable)()->captured.set(
                            new net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit(new com.mojang.blaze3d.vertex.PoseStack().last(),layer,(pose,vertices)->{})));
                        if(capturedState.getCurrentRenderedBlockEntity()!=beforeBe)throw new AssertionError("Submission leaked native marker");
                        ((net.irisshaders.iris.mixinterface.ModelStorage)(Object)captured.get()).iris$set();
                        if(capturedState.getCurrentRenderedBlockEntity()!=value)throw new AssertionError("Native submitted vertex provenance missing "+id+" marker "+value);
                    } finally {
                        capturedState.setCurrentBlockEntity(beforeBe);
                        capturedState.setCurrentEntity(beforeEntity);
                        capturedState.setCurrentRenderedItem(beforeItem);
                    }
                    actual.put(owners[owner]+"/"+index+"/"+id+"/"+flow,Map.of("marker",value,"pipeline",layer.pipeline().getLocation().toString(),"cached",true,"native_submit_captured",true));checks++;
                }
            }
        }
        var fluidModels=Minecraft.getInstance().getModelManager().getFluidStateModelSet();
        int modelChecks=0;
        for(String id:FLUID_EXPECTED_MATERIALS.keySet()) {
            var fluid=BuiltInRegistries.FLUID.getOptional(net.minecraft.resources.Identifier.parse(id)).orElseThrow();
            if(FLUID_EXPECTED_MATERIALS.get(id)==10126 || FLUID_EXPECTED_MATERIALS.get(id)==10001) {
                var model=fluidModels.get(fluid.defaultFluidState());
                if(model.layer()!=net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT) throw new AssertionError("Ordinary world model bypasses water shader: "+id);
                modelChecks++;
            }
        }
        actual.put("ordinary_world_model_checks",modelChecks);
        var modelRows=new java.util.ArrayList<java.util.Map<String,Object>>();
        for(var entry:FLUID_EXPECTED_MATERIALS.entrySet()) {
            var fluid=BuiltInRegistries.FLUID.getOptional(net.minecraft.resources.Identifier.parse(entry.getKey())).orElseThrow();
            var model=fluidModels.get(fluid.defaultFluidState());
            modelRows.add(java.util.Map.of("fluid",entry.getKey(),"marker",entry.getValue(),"still",model.stillMaterial().sprite().contents().name().toString(),"flow",model.flowingMaterial().sprite().contents().name().toString(),"layer",model.layer().toString()));
        }
        Files.writeString(game.resolve("fluid-render-models.json"),new GsonBuilder().setPrettyPrinting().create().toJson(modelRows));

        var potion=BuiltInRegistries.FLUID.getOptional(net.minecraft.resources.Identifier.parse("create:potion")).orElseThrow();
        var redPatch=net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
            new net.minecraft.world.item.alchemy.PotionContents(java.util.Optional.empty(),java.util.Optional.of(0xff2211),java.util.List.of(),java.util.Optional.empty())).build();
        var bluePatch=net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
            new net.minecraft.world.item.alchemy.PotionContents(java.util.Optional.empty(),java.util.Optional.of(0x1122ff),java.util.List.of(),java.util.Optional.empty())).build();
        var redStack=new buildcraft.lib.fluid.stack.FluidStack(potion,1000,redPatch);
        var blueStack=new buildcraft.lib.fluid.stack.FluidStack(potion,1000,bluePatch);
        var red=buildcraft.lib.client.fluid.BcFluidAppearanceCache.get(redStack);
        var blue=buildcraft.lib.client.fluid.BcFluidAppearanceCache.get(blueStack);
        if(red.tint()==blue.tint())throw new AssertionError("Component-bearing potion tints share cached appearance");
        if(red!=buildcraft.lib.client.fluid.BcFluidAppearanceCache.get(redStack.copyWithAmount(2000)))throw new AssertionError("Same fluid/components failed cache reuse");
        actual.put("component_tint_cache",java.util.Map.of("red",red.tint(),"blue",blue.tint(),"amount_independent_cached",true));

        actual.put("native_selection_cases",checks);
        actual.put("scope","Native registry, exact helper selection, actual transformed native submission capture and preserved render properties; actual visible tanks recorded separately.");
        report.put("tank_fluid_materials_pass",true);
        report.put("tank_fluid_materials",actual);
        Files.writeString(game.resolve("tank-fluid-materials-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(actual));
    }
}
