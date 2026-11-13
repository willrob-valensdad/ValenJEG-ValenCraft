package ttv.migami.jeg.station;
import com.google.gson.*;
import com.mojang.serialization.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.*;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.fabric.compat.neoforge.neoforge.common.NeoForge;

/** Real mapped-runtime contracts; no game client, world or production fixture is launched. */
public final class StationNativeContracts {
    private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        // Classpath contract harness has no Fabric launcher injection. Reopen only this JVM's
        // bootstrap-frozen native registries to simulate Fabric's mod registration phase.
        // Production entrypoint never uses this reflection.
        var frozen=MappedRegistry.class.getDeclaredField("frozen");frozen.setAccessible(true);
        var intrusive=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);
        var allTags=MappedRegistry.class.getDeclaredField("allTags");allTags.setAccessible(true);
        var unbound=Class.forName("net.minecraft.core.MappedRegistry$TagSet").getDeclaredMethod("unbound");unbound.setAccessible(true);
        List<Registry<?>> registries=List.of(BuiltInRegistries.ENTITY_TYPE,BuiltInRegistries.DATA_COMPONENT_TYPE,
            BuiltInRegistries.SOUND_EVENT,BuiltInRegistries.PARTICLE_TYPE,BuiltInRegistries.MOB_EFFECT,
            BuiltInRegistries.BLOCK,BuiltInRegistries.BLOCK_ENTITY_TYPE,BuiltInRegistries.MENU,BuiltInRegistries.ITEM);
        for(Registry<?> registry:registries) {
            frozen.set(registry,false);allTags.set(registry,unbound.invoke(null));
            if(registry==BuiltInRegistries.ENTITY_TYPE || registry==BuiltInRegistries.BLOCK || registry==BuiltInRegistries.BLOCK_ENTITY_TYPE || registry==BuiltInRegistries.ITEM)
                intrusive.set(registry,new IdentityHashMap<>());
        }
        ModEntities.REGISTER.register(NeoForge.EVENT_BUS);
        ModDataComponents.REGISTER.register(NeoForge.EVENT_BUS);
        ModSounds.REGISTER.register(NeoForge.EVENT_BUS);
        ModParticleTypes.REGISTER.register(NeoForge.EVENT_BUS);
        ModEffects.REGISTER.register(NeoForge.EVENT_BUS);
        ModBlocks.REGISTER.register(NeoForge.EVENT_BUS);
        ttv.migami.jeg.mteg.MtegContent.registerBlocks();
        ModBlockEntities.REGISTER.register(NeoForge.EVENT_BUS);
        ModMenuTypes.REGISTER.register(NeoForge.EVENT_BUS);
        ModItems.REGISTER.register(NeoForge.EVENT_BUS);
        ttv.migami.jeg.legacy.LegacyContent.register();
        ttv.migami.jeg.mteg.MtegContent.registerItems();
        for(Registry<?> registry:registries)registry.freeze();
        var lookup=net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        for(var pending:BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup))pending.apply();
        ttv.migami.jeg.legacy.LegacyNativeContracts.run(lookup);
        ttv.migami.jeg.mteg.MtegNativeContracts.run(lookup);
        System.out.println("Native registries and component initializers initialized");
        System.out.println("Native drum default model component="+new ItemStack(ModItems.DRUM_MAG.get()).get(net.minecraft.core.component.DataComponents.ITEM_MODEL));
        require(Math.abs(ModBlocks.SCRAP_WORKBENCH.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-1.5F)<.0001F,"Original hardness scrap_workbench");
        require(ModBlocks.SCRAP_WORKBENCH.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState().getSoundType(),"Original sound scrap_workbench");
        require(Math.abs(ModBlocks.GUNMETAL_WORKBENCH.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-3.0F)<.0001F,"Original hardness gunmetal_workbench");
        require(ModBlocks.GUNMETAL_WORKBENCH.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.STONE.defaultBlockState().getSoundType(),"Original sound gunmetal_workbench");
        require(Math.abs(ModBlocks.GUNNITE_WORKBENCH.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-5.0F)<.0001F,"Original hardness gunnite_workbench");
        require(ModBlocks.GUNNITE_WORKBENCH.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState().getSoundType(),"Original sound gunnite_workbench");
        require(Math.abs(ModBlocks.BLUEPRINT_WORKBENCH.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-5.0F)<.0001F,"Original hardness blueprint_workbench");
        require(ModBlocks.BLUEPRINT_WORKBENCH.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState().getSoundType(),"Original sound blueprint_workbench");
        require(Math.abs(ModBlocks.SCHEMATIC_STATION.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-5.0F)<.0001F,"Original hardness schematic_station");
        require(ModBlocks.SCHEMATIC_STATION.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState().getSoundType(),"Original sound schematic_station");
        require(Math.abs(ModBlocks.RECYCLER.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-2.0F)<.0001F,"Original hardness recycler");
        require(ModBlocks.RECYCLER.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.CAULDRON.defaultBlockState().getSoundType(),"Original sound recycler");
        require(Math.abs(ModBlocks.SCRAP_BLOCK.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-3.0F)<.0001F,"Original hardness scrap_block");
        require(ModBlocks.SCRAP_BLOCK.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.IRON_BARS.defaultBlockState().getSoundType(),"Original sound scrap_block");
        require(Math.abs(ModBlocks.GUNMETAL_BLOCK.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-4.0F)<.0001F,"Original hardness gunmetal_block");
        require(ModBlocks.GUNMETAL_BLOCK.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.COPPER_BLOCK.weathering().unaffected().defaultBlockState().getSoundType(),"Original sound gunmetal_block");
        require(Math.abs(ModBlocks.GUNNITE_BLOCK.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-5.0F)<.0001F,"Original hardness gunnite_block");
        require(ModBlocks.GUNNITE_BLOCK.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.defaultBlockState().getSoundType(),"Original sound gunnite_block");
        require(Math.abs(ModBlocks.SCRAP_ORE.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-3.0F)<.0001F,"Original hardness scrap_ore");
        require(ModBlocks.SCRAP_ORE.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.STONE.defaultBlockState().getSoundType(),"Original sound scrap_ore");
        require(Math.abs(ModBlocks.DEEPSLATE_SCRAP_ORE.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-4.5F)<.0001F,"Original hardness deepslate_scrap_ore");
        require(ModBlocks.DEEPSLATE_SCRAP_ORE.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.DEEPSLATE.defaultBlockState().getSoundType(),"Original sound deepslate_scrap_ore");
        require(Math.abs(ModBlocks.BRIMSTONE_ORE.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-1.5F)<.0001F,"Original hardness brimstone_ore");
        require(ModBlocks.BRIMSTONE_ORE.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.DRIPSTONE_BLOCK.defaultBlockState().getSoundType(),"Original sound brimstone_ore");
        require(Math.abs(ModBlocks.BLACKSTONE_BRIMSTONE_ORE.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-1.5F)<.0001F,"Original hardness blackstone_brimstone_ore");
        require(ModBlocks.BLACKSTONE_BRIMSTONE_ORE.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.BLACKSTONE.defaultBlockState().getSoundType(),"Original sound blackstone_brimstone_ore");
        require(Math.abs(ModBlocks.BASALT_BRIMSTONE_ORE.get().defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)-1.5F)<.0001F,"Original hardness basalt_brimstone_ore");
        require(ModBlocks.BASALT_BRIMSTONE_ORE.get().defaultBlockState().getSoundType()==net.minecraft.world.level.block.Blocks.BASALT.defaultBlockState().getSoundType(),"Original sound basalt_brimstone_ore");
        require(!ModBlocks.SCRAP_WORKBENCH.get().defaultBlockState().requiresCorrectToolForDrops() && ModBlocks.RECYCLER.get().defaultBlockState().requiresCorrectToolForDrops() && ModBlocks.GUNNITE_WORKBENCH.get().defaultBlockState().requiresCorrectToolForDrops(),"Original workstation tool requirements");
        Path resources=Path.of("src/main/resources");
        Map<Identifier,JsonElement> objects=new HashMap<>();
        try(var files=Files.list(resources.resolve("data/jeg/valen_station"))) {
            for(Path path:files.toList())objects.put(Identifier.parse("jeg:"+path.getFileName().toString().replace(".json","")),JsonParser.parseString(Files.readString(path)));
        }
        StationRecipes.reloadListener().apply(objects,null,null);
        int loaded=List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycler").stream().mapToInt(name->StationRecipes.forStation(name).size()).sum();
        require(loaded==objects.size(),"Recipe loader silently omitted original recipes");
        require(objects.size()==175,"Unexpected restoration recipe count "+objects.size());
        int materials=0;
        for(String station:List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycler")) {
            var recipes=StationRecipes.forStation(station);require(!recipes.isEmpty(),"No recipes for "+station);
            for(var recipe:recipes) {
                require(!recipe.output().isEmpty(),"Missing output "+recipe.id());
                for(var m:recipe.materials()) {
                    if(!m.tag())require(BuiltInRegistries.ITEM.containsKey(m.id()),"Missing ingredient "+m.id()+" in "+recipe.id());
                    materials++;
                }
            }
            System.out.println(station+": "+recipes.size()+" decoded recipes");
        }
        JsonObject ready;
        try(var reader=new java.io.InputStreamReader(java.util.Objects.requireNonNull(StationNativeContracts.class.getResourceAsStream("/data/jeg/advancement/guide/training/ready.json")),java.nio.charset.StandardCharsets.UTF_8)) {ready=JsonParser.parseReader(reader).getAsJsonObject();}
        int stationCriteria=0;
        for(var entry:ready.getAsJsonObject("criteria").entrySet()) {
            JsonObject criterion=entry.getValue().getAsJsonObject();
            if(!criterion.get("trigger").getAsString().equals("jeg:gameplay_action"))continue;
            var condition=ttv.migami.jeg.advancement.GameplayActionTrigger.Conditions.CODEC.parse(JsonOps.INSTANCE,criterion.get("conditions")).result().orElseThrow();
            require(condition.action().equals("station_craft")&&condition.subject().isPresent(),"Guide station-craft condition lost actual action/target");
            boolean route=List.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench").stream().flatMap(station->StationRecipes.forStation(station).stream()).anyMatch(recipe->recipe.result().toString().equals(condition.subject().orElseThrow()));
            require(route,"Guide criterion targets no actual station output: "+entry.getKey());stationCriteria++;
        }
        require(stationCriteria==31&&ready.getAsJsonObject("criteria").size()==35,"Changed saved guide criterion set");
        System.out.println("Native31 station-craft criterion codecs and actual output routes passed;35 saved criterion keys retained");
        var careerRequirements=net.minecraft.advancements.AdvancementRequirements.CODEC.parse(JsonOps.INSTANCE,ready.get("requirements")).result().orElseThrow();
        var careerProgress=new net.minecraft.advancements.AdvancementProgress();careerProgress.update(careerRequirements);
        require(careerProgress.grantProgress("weapon_jeg_custom_smg")&&!careerProgress.isDone(),"Single gun completed native gun+ammo goal");
        require(careerProgress.grantProgress("ammo_jeg_handmade_shell")&&careerProgress.isDone(),"Native gun+ammo OR-goal did not complete at earlier bulk crafts");
        require(!careerProgress.getCriterion("weapon_jeg_revolver").isDone(),"Completed OR-goal requires every alternative weapon criterion");
        System.out.println("Native OR-goal completes on earlier custom_smg+handmade_shell while later revolver alternative remains false");
        var duplicate=StationRecipe.parse(Identifier.parse("jeg:duplicate_test"),JsonParser.parseString("""
            {"type":"jeg:scrap_workbench","materials":[{"item":"minecraft:iron_ingot","count":3},{"item":"minecraft:iron_ingot","count":4}],"result":{"item":"jeg:scrap"}}
            """).getAsJsonObject());
        try {
            StationRecipe.parse(Identifier.parse("jeg:invalid_recycler"),JsonParser.parseString("""
                {"type":"jeg:recycling","ingredient":{"item":"minecraft:iron_ingot","count":2},"result":"jeg:scrap"}
                """).getAsJsonObject());
            throw new AssertionError("Multi-count recycler bypassed original single-input invariant");
        }catch(IllegalArgumentException expected) {}
        SimpleContainer costs=new SimpleContainer(3);costs.setItem(0,new ItemStack(Items.IRON_INGOT,6));
        require(duplicate.plan(costs)==null,"Double-counted overlapping costs");
        require(Arrays.equals(duplicate.deficits(costs),new int[]{0,1}),"UI deficits double-counted overlapping costs");
        costs.setItem(1,new ItemStack(Items.IRON_INGOT,1));var plan=duplicate.plan(costs);
        require(plan!=null&&Arrays.stream(plan).sum()==7,"Wrong split-stack transaction");
        require(costs.getItem(0).getCount()==6,"Planning mutated inventory");
        for(var alias:Map.of("forge:ingots/iron","c:ingots/iron","forge:ingots/gold","c:ingots/gold","forge:ingots/copper","c:ingots/copper","forge:nuggets/iron","c:nuggets/iron","forge:dyes/black","c:dyes/black","forge:gunpowder","c:gunpowders","forge:sandstone","c:sandstone/blocks").entrySet()) {
            JsonObject material=new JsonObject();material.addProperty("tag",alias.getKey());material.addProperty("count",11);
            JsonArray list=new JsonArray();list.add(material);JsonObject recipe=new JsonObject();recipe.addProperty("type","jeg:scrap_workbench");recipe.add("materials",list);recipe.addProperty("result","jeg:scrap");
            var normalized=StationRecipe.parse(Identifier.parse("jeg:legacy_tag_contract"),recipe).materials().getFirst();
            require(normalized.id().toString().equals(alias.getValue())&&normalized.count()==11,"Original tag normalization changed accepted material or count");
        }
        System.out.println("Native item/count transaction boundaries passed");
        ItemStack gun=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:assault_rifle")));
        ItemStack blank=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:firearm_blueprint")),3);
        ItemStack copied=StationBlockEntity.copyBlueprint(gun,blank);
        require(copied.getCount()==1 && "jeg:assault_rifle".equals(copied.get(ModDataComponents.BLUEPRINT_GUN.get())),"Gun blueprint copy failed");
        require(blank.getCount()==3&&!gun.isEmpty(),"Copy preview consumed input");
        require(ItemStack.matches(copied,StationBlockEntity.copyBlueprint(copied,blank)),"Blueprint duplicate copy lost target");
        ItemStack advanced=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:advanced_firearm_blueprint")));
        advanced.set(ModDataComponents.BLUEPRINT_GUN.get(),"jeg:flamethrower");
        require(StationBlockEntity.copyBlueprint(advanced,blank).isEmpty(),"Advanced blueprint was duplicable");
        require(StationBlockEntity.copyBlueprint(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:flamethrower"))),blank).isEmpty(),"Advanced-only gun was blueprintable");
        System.out.println("Native gun/blank/copy/advanced blueprint rules passed");
        var reloadable=lookup;
        var ops=RegistryOps.create(JsonOps.INSTANCE,reloadable);
        StationBlockEntity entity=new StationBlockEntity(BlockPos.ZERO,ModBlocks.RECYCLER.get().defaultBlockState());
        entity.setItem(0,new ItemStack(Items.GRANITE,7));entity.setItem(1,new ItemStack(Items.COAL,2));
        entity.setItem(2,copied);entity.data().set(0,37);entity.data().set(1,50);entity.data().set(2,777);entity.data().set(3,1600);
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,reloadable);entity.saveCustomOnly(output);
        var saved=output.buildResult();
        StationBlockEntity restored=new StationBlockEntity(BlockPos.ZERO,ModBlocks.RECYCLER.get().defaultBlockState());
        restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING,reloadable,saved));
        require(restored.getItem(0).getCount()==7&&restored.getItem(1).getCount()==2&&restored.data().get(0)==37&&restored.data().get(2)==777,"Native station inventory/progress roundtrip failed");
        require(ItemStack.matches(copied,restored.getItem(2)),"Saved component blueprint changed");
        require(StationBlockEntity.isFuel(new ItemStack(Items.COAL))&&!StationBlockEntity.isFuel(new ItemStack(Items.DIAMOND)),"Native fuel classification failed");
        require(Arrays.equals(restored.getSlotsForFace(net.minecraft.core.Direction.DOWN),new int[]{2}),"Sided output extraction changed");
        System.out.println("Native station persistence, fuel component and automation passed");
        int codecs=0;List<String> failures=new ArrayList<>();
        for(String folder:List.of("recipe","loot_table/blocks","loot_table/chests/valen","loot_table/chests/valen_ad_astra","loot_table/entities/ghoul.json","worldgen/feature")) {
            try(var files=Files.walk(resources.resolve("data/jeg/"+folder))) {
                for(Path f:files.filter(x->x.toString().endsWith(".json")).toList()) {
                    if(folder.equals("recipe") && !(f.getFileName().toString().contains("block_")||f.getFileName().toString().contains("block_to_")||f.getFileName().toString().contains("scrap_")||f.getFileName().toString().contains("gunmetal_")||f.getFileName().toString().contains("brimstone_")))continue;
                    JsonElement json=JsonParser.parseString(Files.readString(f));
                    DataResult<?> decoded=folder.equals("recipe") ? Recipe.CODEC.parse(ops,json) : folder.startsWith("loot") ? LootTable.DIRECT_CODEC.parse(ops,json) : Feature.DIRECT_CODEC.parse(ops,json);
                    if(decoded.error().isPresent())failures.add(f+": "+decoded.error().get().message()); else codecs++;
                }
            }
        }
        var bossPool=BlueprintLoot.pool(true,1,"subsonic_rifle","supersonic_shotgun","hypersonic_cannon").build();
        String encodedPool=LootPool.CODEC.encodeStart(ops,bossPool).getOrThrow().toString();
        require(encodedPool.contains("jeg:blueprint_gun") && encodedPool.contains("hypersonic_cannon"),"Earned blueprint component disappeared in native loot codec");
        failures.forEach(System.err::println);require(failures.isEmpty(),"Native resource codec failures: "+failures.size());
        System.out.println("Native resource codecs passed: "+codecs+"; material entries "+materials);
        System.out.println("JEG_STATION_NATIVE_CONTRACTS_PASS");
    }
}
