package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.station.*;

/** Explicit fixture-only probe. Not included in the production mod, never runs without opt-in. */
public final class StationProbe implements ModInitializer {
    private static void check(boolean b,String message) {if(!b)throw new AssertionError(message);}
    @Override public void onInitialize() {
        CustomizationContracts.register();
        StructureFidelityContracts.register();
        HangarHelipadContracts.register();
        BunkerWorldProbe.register();
        IndustrialSourceContracts.register();
        DesertComplexContracts.register();
        BunkerRoomsContracts.register();
        MinerOutpostContracts.register();
        SpaceRoutingProbe.register();
        if(Boolean.getBoolean("valen.fixture.structurePhotos")){StationServerActions.init();ServerLifecycleEvents.SERVER_STARTED.register(ArcticStationWinterAssert::assertLoaded);}
        if(Boolean.getBoolean("valen.structure.probe.spaceRoutingMoon")||Boolean.getBoolean("valen.structure.probe.spaceRoutingOrbit")){
            check(!(Boolean.getBoolean("valen.structure.probe.spaceRoutingMoon")&&Boolean.getBoolean("valen.structure.probe.spaceRoutingOrbit")),"Moon and orbit qualification run one dimension at a time");
            check(!(Boolean.getBoolean("valen.structure.probe.bunkerRooms")||Boolean.getBoolean("valen.desertComplex.probe")||Boolean.getBoolean("valen.industrial.probe")||Boolean.getBoolean("valen.structure.probe.minerOutpost")||Boolean.getBoolean("valen.bunker.probe")),"Space routing stays exclusive of bunker, desert, industrial, and miner");
            ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->SpaceRoutingProbe.run(server)));
            return;
        }
        if(Boolean.getBoolean("valen.structure.probe.bunkerRooms")){
            check(!(Boolean.getBoolean("valen.desertComplex.probe")||Boolean.getBoolean("valen.structure.probe.desertComplex")||Boolean.getBoolean("valen.structure.probe.desertNativeSearch")||Boolean.getBoolean("valen.industrial.probe")||Boolean.getBoolean("valen.structure.probe.industrial")||Boolean.getBoolean("valen.structure.probe.industrialResumeR36")||Boolean.getBoolean("valen.structure.probe.cherryFreshTrace")||Boolean.getBoolean("valen.structure.probe.cherryInner1Trace")||Boolean.getBoolean("valen.bunker.probe")||Boolean.getBoolean("valen.structure.probe.minerOutpost")||Boolean.getBoolean("valen.structure.probe.spaceRoutingMoon")||Boolean.getBoolean("valen.structure.probe.spaceRoutingOrbit")),"Bunker rooms probe stays exclusive of desert, industrial, cherry inner1, and the old bunker probe");
            ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->{BunkerRoomsContracts.requirePassed();StructureProbe.run(server.overworld(),new JsonObject());}));
            return;
        }
        if(Boolean.getBoolean("valen.desertComplex.probe")){
            check(!(Boolean.getBoolean("valen.industrial.probe")||Boolean.getBoolean("valen.structure.probe.industrial")||Boolean.getBoolean("valen.structure.probe.industrialResumeR36")||Boolean.getBoolean("valen.structure.probe.cherryFreshTrace")||Boolean.getBoolean("valen.structure.probe.cherryInner1Trace")),"Desert complex probe stays exclusive of industrial and cherry fresh trace");
            if(Boolean.getBoolean("valen.structure.probe.desertComplex")||Boolean.getBoolean("valen.structure.probe.desertNativeSearch"))ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->{DesertComplexContracts.requirePassed();StructureProbe.run(server.overworld(),new JsonObject());}));
            return;
        }
        if(Boolean.getBoolean("valen.enchantments.probe"))
            ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->runEnchantments(server)));
        ttv.migami.jeg.tech.astra.oxygen.ui.OxygenConnectedChecks.register();
        if(!Boolean.getBoolean("valen.station.probe"))return;
        if(!Boolean.getBoolean("valen.fixture.structurePhotos"))StationServerActions.init();
        if(Boolean.getBoolean("valen.station.readbackOnly"))return; // Manual copied-fixture inbox only; no automatic station/world replay.
        if(Boolean.getBoolean("valen.industrial.probe")){
            if(Boolean.getBoolean("valen.structure.probe.industrial")||Boolean.getBoolean("valen.structure.probe.industrialResumeR36"))ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->{IndustrialSourceContracts.requirePassed();StructureProbe.run(server.overworld(),new JsonObject());}));
            return; // Raw mode never replays legacy station/world checks. The world flag registers only StructureProbe after the raw pass.
        }
        if(Boolean.getBoolean("valen.structure.probe.minerOutpost")){
            ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->{MinerOutpostContracts.requirePassed();StructureProbe.run(server.overworld(),new JsonObject());}));
            return; // Miner preflight is already registered. Do not fall through into station recipe placement.
        }
        if(Boolean.getBoolean("valen.structure.probe.hangarHelipadAnchors")||Boolean.getBoolean("valen.structure.probe.hangarHelipadResume")||Boolean.getBoolean("valen.structure.probe.hangarHelipad")||Boolean.getBoolean("valen.structure.probe.components")||Boolean.getBoolean("valen.structure.probe.tower")||Boolean.getBoolean("valen.structure.probe.scorched")||Boolean.getBoolean("valen.structure.probe.scorchedResume")||Boolean.getBoolean("valen.structure.probe.hbmNext")||Boolean.getBoolean("valen.structure.probe.hbmNextResume"))ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->StructureProbe.run(server.overworld(),new JsonObject())));
        else if(!Boolean.getBoolean("valen.bunker.probe"))ServerLifecycleEvents.SERVER_STARTED.register(server->server.execute(()->run(server)));
    }
    private static void runEnchantments(MinecraftServer server){
        JsonObject receipt=new JsonObject();receipt.addProperty("proper_fabric_initialization",true);
        ServerLevel level=server.overworld();ProbePlayer player=null;
        try{
            level.getChunk(0,1);level.getChunk(1,1);
            player=new ProbePlayer(server,level);player.setPos(.5,221,.5);
            player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            LegacyEnchantmentWorldChecks.run(level,player,receipt);
            receipt.addProperty("status","pass");
        }catch(Throwable failure){receipt.addProperty("status","fail");receipt.addProperty("error",failure.toString());failure.printStackTrace();}
        finally{
            if(player!=null){player.getInventory().clearContent();player.discard();}
            try{java.nio.file.Files.writeString(java.nio.file.Path.of("valen-enchantments-probe.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(receipt));}
            catch(Exception failure){throw new RuntimeException("Cannot persist enchantment fixture receipt",failure);}
        }
    }
    private static final class ProbePlayer extends ServerPlayer {
        ProbePlayer(MinecraftServer server,ServerLevel level) {
            super(server,level,new GameProfile(UUID.fromString("116a953e-3222-443b-a2d4-f1e67674c309"),"ValenStationTest"),ClientInformation.createDefault());
        }
        @Override public boolean hasDisconnected() {return false;}
        @Override public void sendSystemMessage(Component message) {}
    }
    private static int total(Inventory inventory) {int count=0;for(int i=0;i<inventory.getContainerSize();i++)count+=inventory.getItem(i).getCount();return count;}
    private static void fill(Inventory inventory,StationRecipe recipe) {
        inventory.clearContent();int slot=0;
        for(var material:recipe.materials()) {
            Item item;
            if(material.tag()) {
                var holders=BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,material.id())).iterator();
                check(holders.hasNext(),"Empty native ingredient tag "+material.id()+" for "+recipe.id());item=holders.next().value();
            } else item=BuiltInRegistries.ITEM.getValue(material.id());
            int remaining=material.count();
            while(remaining>0) {check(slot<36,"Recipe exceeds test inventory");ItemStack stack=new ItemStack(item,Math.min(remaining,item.getDefaultMaxStackSize()));remaining-=stack.getCount();inventory.setItem(slot++,stack);}
        }
    }
    private static List<ItemEntity> drops(ServerLevel level,BlockPos pos) {return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));}
    private static void clearDrops(ServerLevel level,BlockPos pos) {for(ItemEntity item:drops(level,pos))item.discard();}
    public static void run(MinecraftServer server) {
        JsonObject receipt=new JsonObject();JsonArray passed=new JsonArray();receipt.add("passed",passed);
        receipt.addProperty("production_changes",false);receipt.addProperty("proper_fabric_initialization",true);
        ServerLevel level=server.overworld();BlockPos pos=new BlockPos(0,220,0);
        var old=level.getBlockState(pos);
        ProbePlayer player=null;
        try {
            JsonObject resolvedTags=new JsonObject();int taggedEntries=0,loadedRecipes=0;
            Map<String,List<String>> expected=Map.of(
                "c:ingots/iron",List.of("minecraft:iron_ingot"),"c:ingots/gold",List.of("minecraft:gold_ingot"),
                "c:ingots/copper",List.of("minecraft:copper_ingot"),"c:nuggets/iron",List.of("minecraft:iron_nugget"),
                "c:dyes/black",List.of("minecraft:black_dye"),"c:gunpowders",List.of("minecraft:gunpowder"),
                "c:sandstone/blocks",List.of("minecraft:sandstone","minecraft:red_sandstone"),
                "minecraft:planks",List.of("minecraft:oak_planks"),"minecraft:wool",List.of("minecraft:white_wool"));
            for(String station:List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycler"))for(var recipe:StationRecipes.forStation(station)) {
                loadedRecipes++;
                for(var material:recipe.materials())if(material.tag()) {
                    taggedEntries++;check(!material.id().getNamespace().equals("forge"),"Unmapped original Forge tag "+material.id());
                    TreeSet<String> items=new TreeSet<>();for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,material.id())))items.add(BuiltInRegistries.ITEM.getKey(holder.value()).toString());
                    check(!items.isEmpty(),"Empty actual Fabric material tag "+material.id()+" for "+recipe.id());
                    for(String representative:expected.getOrDefault(material.id().toString(),List.of()))check(items.contains(representative),"Actual tag lost original material "+representative+" in "+material.id());
                    JsonArray values=new JsonArray();items.forEach(values::add);resolvedTags.add(material.id().toString(),values);
                }
            }
            check(loadedRecipes==175,"Actual Fabric recipe count "+loadedRecipes+" expected175");
            receipt.add("actual_fabric_material_tags",resolvedTags);receipt.addProperty("tagged_material_entries",taggedEntries);receipt.addProperty("tag_validated_recipes",loadedRecipes);
            passed.add("All175 actual server recipes resolved material tags and original representative materials before craft tests");
            player=new ProbePlayer(server,level);player.setPos(.5,221,.5);
            // Same disconnected native listener already exercised by LegacyWorldProbe; no socket is opened.
            player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            var ready=server.getAdvancements().get(Identifier.parse("jeg:guide/training/ready"));
            check(ready!=null,"Missing native training-ready advancement");
            for(String criterion:ready.value().criteria().keySet())player.getAdvancements().revoke(ready,criterion);
            ItemStack ownedGun=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:custom_smg")));
            player.getInventory().setItem(0,ownedGun);
            net.minecraft.advancements.triggers.CriteriaTriggers.INVENTORY_CHANGED.trigger(player,player.getInventory(),ownedGun);
            check(!player.getAdvancements().getOrStartProgress(ready).getCriterion("weapon_jeg_custom_smg").isDone(),"Merely owning a gun awarded actual-craft career criterion");
            player.getInventory().clearContent();
            int crafted=0;boolean careerCraftProved=false;
            for(String station:List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","mo_workbench")) {
                var block=BuiltInRegistries.BLOCK.getValue(Identifier.parse((station.equals("mo_workbench")?"mteg:":"jeg:")+station));
                level.setBlock(pos,block.defaultBlockState(),3);
                check(level.getBlockEntity(pos) instanceof StationBlockEntity,"Missing native block entity "+station);
                StationBlockEntity be=(StationBlockEntity)level.getBlockEntity(pos);
                StationMenu menu=(StationMenu)be.createMenu(1,player.getInventory(),player);player.containerMenu=menu;
                check(menu.stillValid(player),"Menu not usable at actual station "+station);
                for(int i=0;i<menu.recipes.size();i++) {
                    var recipe=menu.recipes.get(i);fill(player.getInventory(),recipe);
                    if(station.equals("mo_workbench"))be.setItem(0,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:blue_dye")),4));
                    if(station.equals("blueprint_workbench")) {
                        ItemStack blueprint=new ItemStack(ModItems.STATION_MATERIALS.get("advanced_firearm_blueprint").get());
                        blueprint.set(ModDataComponents.BLUEPRINT_GUN.get(),recipe.result().toString());be.setItem(0,blueprint);
                    }
                    clearDrops(level,pos);
                    int before=total(player.getInventory()),cost=recipe.materials().stream().mapToInt(StationRecipe.Material::count).sum();
                    check(menu.clickMenuButton(player,i),"Native craft failed "+recipe.id());
                    // Test the first gun before an ammo craft completes the OR-goal and vanilla unregisters alternative listeners.
                    if(!careerCraftProved&&recipe.result().toString().equals("jeg:custom_smg")) {
                        check(player.getAdvancements().getOrStartProgress(ready).getCriterion("weapon_jeg_custom_smg").isDone(),"Actual first station gun craft failed to award existing career criterion");
                        careerCraftProved=true;
                    }
                    check(total(player.getInventory())==before-cost,"Wrong material consumption "+recipe.id());
                    int quantity=drops(level,pos).stream().filter(e->e.getItem().is(BuiltInRegistries.ITEM.getValue(recipe.result()))).mapToInt(e->e.getItem().getCount()).sum();
                    check(quantity==recipe.count(),"Wrong native dropped result "+recipe.id()+": "+quantity);
                    if(station.equals("mo_workbench")){
                        check(BuiltInRegistries.ITEM.containsKey(recipe.result()),"Missing actual MTEG output "+recipe.result());
                        boolean gun=recipe.output().getItem() instanceof ttv.migami.jeg.mteg.MtegGunItem;
                        check(gun?be.getItem(0).isEmpty():be.getItem(0).getCount()==4,"Original Mo dye consumption "+recipe.id());
                        if(gun)check(drops(level,pos).stream().anyMatch(e->"minecraft:blue_dye".equals(e.getItem().get(ModDataComponents.GUN_DYE_ATTACHMENT.get()))),"Mo output lost original color "+recipe.id());
                    }
                    player.getInventory().clearContent();check(!menu.clickMenuButton(player,i),"Craft succeeded without materials "+recipe.id());
                    crafted++;
                }
                if(!menu.recipes.isEmpty()) {
                    fill(player.getInventory(),menu.recipes.get(0));int before=total(player.getInventory());
                    player.setPos(30,221,0);check(!menu.clickMenuButton(player,0),"Distant craft accepted");check(total(player.getInventory())==before,"Distant craft consumed materials");player.setPos(.5,221,.5);
                    check(!menu.clickMenuButton(player,-1)&&!menu.clickMenuButton(player,menu.recipes.size()),"Out-of-range menu recipe accepted");
                }
                clearDrops(level,pos);level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
                level.setBlock(pos,block.defaultBlockState(),3);
                check(!menu.stillValid(player),"Old menu retained authority after same-block replacement: "+station);
                level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            }
            check(StationRecipes.forStation("mo_workbench").size()==37,"Actual MTEG recipes missing");
            passed.add("All "+crafted+" original-compatible workbench recipes crafted via actual server menus; exact costs/output; insufficient/distant/index gates");
            check(careerCraftProved,"First native gun career test did not run");
            check(player.getAdvancements().getOrStartProgress(ready).isDone(),"Native gun+ammo OR-goal did not complete");
            JsonObject career=new JsonObject();career.addProperty("ownership_awarded",false);career.addProperty("first_actual_gun_craft_awarded",careerCraftProved);career.addProperty("gun_and_ammo_goal_complete",true);
            career.addProperty("vanilla_behavior","Completed OR-goals stop listening for later alternative criteria; no requirement that every weapon alternative becomes true");receipt.add("career_craft_proof",career);
            passed.add("Existing native career criterion requires first actual gun craft; mere inventory ownership rejected; native gun+ammo OR-goal completes");
            StationRecipe budget=StationRecipe.parse(Identifier.parse("jeg:probe_visible_budget"),JsonParser.parseString("""
                {"type":"jeg:scrap_workbench","materials":[{"item":"minecraft:iron_ingot","count":7}],"result":{"item":"jeg:scrap"}}
                """).getAsJsonObject());
            player.getInventory().clearContent();player.getInventory().setItem(36,new ItemStack(Items.IRON_INGOT,7));
            check(budget.plan(player.getInventory())==null,"Craft budget consumed equipment/offhand beyond36 visible slots");
            check(player.getInventory().getItem(36).getCount()==7,"Planning changed equipment");
            player.getInventory().clearContent();
            passed.add("Visible36-slot budget excludes equipment/offhand; stale menus reject same-block replacement");
            level.setBlock(pos,ModBlocks.SCHEMATIC_STATION.get().defaultBlockState(),3);
            StationBlockEntity schematic=(StationBlockEntity)level.getBlockEntity(pos);
            StationMenu menu=(StationMenu)schematic.createMenu(2,player.getInventory(),player);player.containerMenu=menu;
            ItemStack gun=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:assault_rifle")));gun.set(ModDataComponents.GUN_AMMO.get(),7);
            menu.inventory.setItem(0,gun);menu.inventory.setItem(1,new ItemStack(ModItems.STATION_MATERIALS.get("firearm_blueprint").get(),3));menu.broadcastChanges();
            ItemStack copied=menu.getSlot(2).remove(1);check("jeg:assault_rifle".equals(copied.get(ModDataComponents.BLUEPRINT_GUN.get())),"Native schematic result lost target");
            menu.getSlot(2).onTake(player,copied);
            check(menu.inventory.getItem(1).getCount()==2,"Schematic did not consume exactly one blank");
            check(menu.inventory.getItem(0).get(ModDataComponents.GUN_AMMO.get())==7,"Schematic changed source gun components");
            player.getInventory().clearContent();
            ItemStack shifted=menu.quickMoveStack(player,2);
            check("jeg:assault_rifle".equals(shifted.get(ModDataComponents.BLUEPRINT_GUN.get())),"Schematic shift extraction lost target");
            check(menu.inventory.getItem(1).getCount()==1,"Schematic shift extraction did not consume exactly one blank");
            check(menu.inventory.getItem(0).get(ModDataComponents.GUN_AMMO.get())==7,"Schematic shift extraction changed source components");
            player.getInventory().clearContent();menu.removed(player);
            check(total(player.getInventory())==2,"Schematic close did not return source and one remaining blank");
            check(schematic.isEmpty(),"Schematic introduced persistent/automated source inventory");
            StationMenu staleSchematic=(StationMenu)schematic.createMenu(9,player.getInventory(),player);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);level.setBlock(pos,ModBlocks.SCHEMATIC_STATION.get().defaultBlockState(),3);
            check(!staleSchematic.stillValid(player),"Transient schematic menu retained authority after replacement");
            passed.add("Schematic source/components retained, one blank consumed, virtual result and original close-return behavior");
            level.setBlock(pos,ModBlocks.RECYCLER.get().defaultBlockState(),3);
            StationBlockEntity recycler=(StationBlockEntity)level.getBlockEntity(pos);
            recycler.setItem(0,new ItemStack(Items.GRANITE,2));recycler.setItem(1,new ItemStack(Items.COAL,1));
            for(int i=0;i<25;i++)StationBlockEntity.serverTick(level,pos,recycler.getBlockState(),recycler);
            check(recycler.getItem(2).isEmpty(),"Recycler produced before original50tick recipe time");
            var saved=recycler.saveWithFullMetadata(server.registryAccess());
            StationBlockEntity loaded=(StationBlockEntity)BlockEntity.loadStatic(pos,recycler.getBlockState(),saved,server.registryAccess());
            check(loaded!=null&&loaded.data().get(0)==25&&loaded.getItem(0).getCount()==2,"Recycler native save/load lost in-flight work");
            check(recycler.getItem(1).isEmpty()&&recycler.data().get(2)>0,"Recycler did not consume native fuel exactly once");
            for(int i=0;i<25;i++)StationBlockEntity.serverTick(level,pos,recycler.getBlockState(),recycler);
            check(recycler.getItem(2).is(ModItems.STATION_MATERIALS.get("gunmetal_grit").get())&&recycler.getItem(2).getCount()==2&&recycler.getItem(0).getCount()==1,"Recycler original granite→two gunmetal grit recipe failed");
            recycler.setItem(2,new ItemStack(ModItems.STATION_MATERIALS.get("gunmetal_grit").get(),64));int input=recycler.getItem(0).getCount();
            for(int i=0;i<100;i++)StationBlockEntity.serverTick(level,pos,recycler.getBlockState(),recycler);
            check(recycler.getItem(0).getCount()==input,"Blocked recycler consumed input");
            passed.add("Recycler actual50tick recipe/fuel consumption/output/backpressure/native in-flight save/load");
            var xpSaved=recycler.saveWithFullMetadata(server.registryAccess());xpSaved.putFloat("Experience",7);
            StationBlockEntity withXp=(StationBlockEntity)BlockEntity.loadStatic(pos,recycler.getBlockState(),xpSaved,server.registryAccess());
            level.setBlockEntity(withXp);
            int beforeXp=level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new AABB(pos).inflate(3)).stream().mapToInt(net.minecraft.world.entity.ExperienceOrb::getValue).sum();
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            int afterXp=level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new AABB(pos).inflate(3)).stream().mapToInt(net.minecraft.world.entity.ExperienceOrb::getValue).sum();
            check(afterXp-beforeXp==7,"Recycler break lost stored XP");
            for(var orb:level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new AABB(pos).inflate(3)))orb.discard();
            passed.add("Recycler break releases stored native XP exactly once");
            receipt.addProperty("station_status","pass");receipt.addProperty("workbench_recipe_count",crafted);
        }catch(Throwable failure) {
            receipt.addProperty("station_status","fail");receipt.addProperty("station_error",failure.toString());failure.printStackTrace();
        }finally {
            // Each phase owns a fresh dummy and restores its blocks; a station assertion must not hide independent failures.
            if(level.getBlockEntity(pos) instanceof StationBlockEntity station)station.clearContent();
            level.setBlock(pos,old,3);clearDrops(level,pos);
            if(player!=null){player.getInventory().clearContent();player.discard();}
            ProbePlayer legacyPlayer=null;
            try {
                if(Boolean.getBoolean("valen.legacy.probe")) {
                    legacyPlayer=new ProbePlayer(server,level);legacyPlayer.setPos(.5,221,.5);
                    LegacyWorldProbe.run(level,legacyPlayer,receipt);
                }else receipt.addProperty("legacy_status","disabled");
            }catch(Throwable failure) {
                receipt.addProperty("legacy_status","fail");receipt.addProperty("legacy_error",failure.toString());failure.printStackTrace();
            }finally {if(legacyPlayer!=null){legacyPlayer.getInventory().clearContent();legacyPlayer.discard();}}
            try {
                if(Boolean.getBoolean("valen.structure.probe")) {
                    Class<?> helper=Class.forName("ttv.migami.jeg.station.probe.StructureProbe");
                    helper.getMethod("run",ServerLevel.class,JsonObject.class).invoke(null,level,receipt);
                    receipt.addProperty("structure_status","pending independent incremental receipt");
                    receipt.addProperty("structure_receipt",Boolean.getBoolean("valen.structure.probe.hangarHelipadAnchors")?"valen-structure-probe-hangar-helipad-anchors-r32.json":Boolean.getBoolean("valen.structure.probe.hangarHelipadResume")?"valen-structure-probe-hangar-helipad-resume-r31.json":Boolean.getBoolean("valen.structure.probe.hangarHelipad")?"valen-structure-probe-hangar-helipad.json":Boolean.getBoolean("valen.structure.probe.components")?"valen-structure-probe-components.json":Boolean.getBoolean("valen.structure.probe.tower")?"valen-structure-probe-tower.json":Boolean.getBoolean("valen.structure.probe.scorchedResume")?"valen-structure-probe-scorched-resume.json":Boolean.getBoolean("valen.structure.probe.scorched")?"valen-structure-probe-scorched.json":Boolean.getBoolean("valen.structure.probe.hbmNextResume")?"valen-structure-probe-hbm-next-resume.json":Boolean.getBoolean("valen.structure.probe.hbmNext")?"valen-structure-probe-hbm-next.json":Boolean.getBoolean("valen.structure.probe.retryKnown")?"valen-structure-probe-expansion-r12.json":Boolean.getBoolean("valen.structure.probe.expansionResume")?"valen-structure-probe-expansion-r11.json":Boolean.getBoolean("valen.structure.probe.expansion")?"valen-structure-probe-expansion.json":Boolean.getBoolean("valen.structure.probe.resume")?"valen-structure-probe-extension-r6.json":Boolean.getBoolean("valen.structure.probe.pendingOnly")?"valen-structure-probe-extension.json":"valen-structure-probe.json");
                }else receipt.addProperty("structure_status","disabled");
            }catch(Throwable failure) {
                receipt.addProperty("structure_status","fail");receipt.addProperty("structure_error",failure.toString());failure.printStackTrace();
            }
            if(Boolean.getBoolean("valen.structure.details"))try {
                Class.forName("ttv.migami.jeg.station.probe.StructureDetailsProbe").getMethod("run",ServerLevel.class).invoke(null,level);
                receipt.addProperty("structure_details_status","observer executed; separate valen-structure-details.json authoritative");
            }catch(Throwable failure){receipt.addProperty("structure_details_status","fail");receipt.addProperty("structure_details_error",failure.toString());failure.printStackTrace();}
            if(Boolean.getBoolean("valen.mteg.probe"))try{MtegWorldChecks.run(server,receipt);}catch(Throwable failure){receipt.addProperty("mteg_status","fail");receipt.addProperty("mteg_error",failure.toString());failure.printStackTrace();}
            if(Boolean.getBoolean("valen.astra.probe"))try{ttv.migami.jeg.tech.astra.CoalGeneratorWorldChecks.start(server);receipt.addProperty("astra_status","pending separate valen-astra-tech-probe.json");}catch(Throwable failure){receipt.addProperty("astra_status","fail");receipt.addProperty("astra_error",failure.toString());failure.printStackTrace();}
            if(Boolean.getBoolean("valen.astra.oxygen.probe"))try{ttv.migami.jeg.tech.astra.oxygen.OxygenLoaderWorldChecks.start(server);receipt.addProperty("oxygen_status","pending separate valen-astra-oxygen-probe.json");}catch(Throwable failure){receipt.addProperty("oxygen_status","fail");receipt.addProperty("oxygen_error",failure.toString());failure.printStackTrace();}
            boolean failed=List.of("station_status","legacy_status","structure_status","mteg_status","astra_status","oxygen_status").stream().anyMatch(key->receipt.has(key)&&receipt.get(key).getAsString().equals("fail"));
            // A running required structure phase is never reported as overall PASS. Its separate receipt includes visual/activation gates.
            receipt.addProperty("status",failed?"fail":(Boolean.getBoolean("valen.structure.probe")||Boolean.getBoolean("valen.astra.probe")||Boolean.getBoolean("valen.mteg.probe")||Boolean.getBoolean("valen.astra.oxygen.probe"))?"pending independent structure/tech/MTEG acceptance":"pass");
            try {Files.writeString(Path.of(System.getProperty("valen.station.probe.output","valen-station-probe.json")),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));}
            catch(Exception io) {io.printStackTrace();}
            System.out.println("VALEN_STATION_FIXTURE "+receipt);
        }
    }
}
