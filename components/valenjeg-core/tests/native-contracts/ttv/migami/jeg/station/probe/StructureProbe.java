package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import ttv.migami.jeg.init.ModDataComponents;

/** Fixture-only opt-in native structure probe, excluded from production JAR.
 * Real configured structure candidates; no invented/forced structure generator.
 * One candidate/chunk step per server tick keeps the normal watchdog meaningful.
 */
public final class StructureProbe {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final boolean HANGAR_HELIPAD_ANCHORS=Boolean.getBoolean("valen.structure.probe.hangarHelipadAnchors");
    private static final boolean CHERRY_INNER1=HANGAR_HELIPAD_ANCHORS&&Boolean.getBoolean("valen.structure.probe.cherryInner1Trace");
    private static final boolean CHERRY_INNER1_CONTINUE=CHERRY_INNER1&&Boolean.getBoolean("valen.structure.probe.cherryInner1Continue");
    private static final boolean CHERRY_FRESH=HANGAR_HELIPAD_ANCHORS&&(Boolean.getBoolean("valen.structure.probe.cherryFreshTrace")||CHERRY_INNER1);
    private static final boolean HANGAR_HELIPAD_RESUME=HANGAR_HELIPAD_ANCHORS||Boolean.getBoolean("valen.structure.probe.hangarHelipadResume");
    private static final boolean HANGAR_HELIPAD=HANGAR_HELIPAD_RESUME||Boolean.getBoolean("valen.structure.probe.hangarHelipad");
    private static final boolean COMPONENTS=Boolean.getBoolean("valen.structure.probe.components");
    private static final boolean TOWER=Boolean.getBoolean("valen.structure.probe.tower");
    private static final boolean SCORCHED_RESUME=Boolean.getBoolean("valen.structure.probe.scorchedResume");
    private static final boolean SCORCHED=SCORCHED_RESUME||Boolean.getBoolean("valen.structure.probe.scorched");
    private static final Path PRIOR_SCORCHED_PARTIAL=Path.of(System.getProperty("valenjeg.evidence.c7ee33009826", "VALENJEG_EVIDENCE_NOT_CONFIGURED/native-scorched-three-pass-partial.json"));
    private static final String PRIOR_SCORCHED_PARTIAL_SHA="41231aa40c3ef53ab3e98a6baffef8e8f4c20d51b58c3d1117b9bd5f1590e774";
    private static final boolean INDUSTRIAL_RESUME=Boolean.getBoolean("valen.structure.probe.industrialResumeR36");
    private static final boolean INDUSTRIAL=INDUSTRIAL_RESUME||Boolean.getBoolean("valen.structure.probe.industrial");
    private static final boolean DESERT_LOCATOR=Boolean.getBoolean("valen.structure.probe.desertNativeSearch");
    private static final boolean DESERT=DESERT_LOCATOR||Boolean.getBoolean("valen.structure.probe.desertComplex");
    private static final boolean BUNKER=Boolean.getBoolean("valen.structure.probe.bunkerRooms");
    private static final boolean MINER=Boolean.getBoolean("valen.structure.probe.minerOutpost");
    private static final Path PRIOR_R39=Path.of(System.getProperty("valenjeg.evidence.80cba961b0f7", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-desert-complex.json"));
    private static final String PRIOR_R39_SHA="203a04c524f6082b078c7b943b0a0696d7dcd428b37ea77ade0d7761d0958ea9";
    private static final Path PRIOR_36=Path.of(System.getProperty("valenjeg.evidence.cc354fa5adce", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-36.json"));
    private static final String PRIOR_36_SHA="4d778dda9690dad8736561ee8a9fbe781ac0fde2916504a0f6b148096ba248be";
    private static final String PRIOR_R36_SHA="c7059665d0f1c250734bf4730b7dea0c1158d9e139ae4f81f0f8e31bd43274d5";
    private static final Path PRIOR_34=Path.of(System.getProperty("valenjeg.evidence.d448615a7633", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-34.json"));
    private static final Path PRIOR_R35=Path.of(System.getProperty("valenjeg.evidence.a93c1a6a4c54", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-industrial.json"));
    private static final String PRIOR_R35_SHA="e342418f93b9638ab40e7c90c5cde2c18858709782ed132d76d920dc1e5c4bdd";
    private static final String PRIOR_34_SHA="53807e814653b830c77883329ada4dffb9a5be8e4c62c7179c2d2a0cae944b80";
    private static final Map<String,Integer> INDUSTRIAL_SLOTS=Map.of("jeg:valen_industrial/train_station",36,"jeg:valen_industrial/warehouse",37);
    private static final boolean HBM_NEXT_RESUME=Boolean.getBoolean("valen.structure.probe.hbmNextResume");
    private static final boolean HBM_NEXT=HBM_NEXT_RESUME||Boolean.getBoolean("valen.structure.probe.hbmNext");
    private static final Path PRIOR_HBM_NEXT=Path.of(System.getProperty("valenjeg.evidence.fb0701cda1ff", "VALENJEG_EVIDENCE_NOT_CONFIGURED/native-hbm-three-pass-partial.json"));
    private static final String PRIOR_HBM_NEXT_SHA="68e078c823793361759307f53d4c260827a9c137612814ef3efee468d7bb955f";
    private static final int BASELINE_COUNT=HANGAR_HELIPAD?25:COMPONENTS?23:TOWER?22:SCORCHED?18:HBM_NEXT?14:8,EXPECTED_EXPANSION_COUNT=HANGAR_HELIPAD?10:COMPONENTS?2:TOWER?1:SCORCHED||HBM_NEXT?4:6;
    private static final boolean RETRY_KNOWN=!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&Boolean.getBoolean("valen.structure.probe.retryKnown");
    private static final boolean EXPANSION_RESUME=!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&(RETRY_KNOWN||Boolean.getBoolean("valen.structure.probe.expansionResume"));
    private static final boolean EXPANSION=HANGAR_HELIPAD||COMPONENTS||TOWER||SCORCHED||HBM_NEXT||EXPANSION_RESUME||Boolean.getBoolean("valen.structure.probe.expansion");
    private static final boolean RESUME=!EXPANSION&&Boolean.getBoolean("valen.structure.probe.resume");
    private static final boolean EXTENSION=EXPANSION||RESUME||Boolean.getBoolean("valen.structure.probe.pendingOnly");
    private static final Path REPORT=Path.of(MINER?"valen-structure-probe-miner-outpost.json":BUNKER?"valen-structure-probe-bunker-rooms.json":DESERT_LOCATOR?"valen-structure-probe-desert-native-search-r43.json":DESERT?"valen-structure-probe-desert-complex.json":CHERRY_INNER1?"valen-structure-probe-cherry-inner1-trace.json":CHERRY_FRESH?"valen-structure-probe-cherry-fresh-trace.json":INDUSTRIAL_RESUME?"valen-structure-probe-industrial-resume-r36.json":INDUSTRIAL?"valen-structure-probe-industrial.json":HANGAR_HELIPAD_ANCHORS?"valen-structure-probe-hangar-helipad-anchors-r32.json":HANGAR_HELIPAD_RESUME?"valen-structure-probe-hangar-helipad-resume-r31.json":HANGAR_HELIPAD?"valen-structure-probe-hangar-helipad.json":COMPONENTS?"valen-structure-probe-components.json":TOWER?"valen-structure-probe-tower.json":SCORCHED_RESUME?"valen-structure-probe-scorched-resume.json":SCORCHED?"valen-structure-probe-scorched.json":HBM_NEXT_RESUME?"valen-structure-probe-hbm-next-resume.json":HBM_NEXT?"valen-structure-probe-hbm-next.json":RETRY_KNOWN?"valen-structure-probe-expansion-r12.json":EXPANSION_RESUME?"valen-structure-probe-expansion-r11.json":EXPANSION?"valen-structure-probe-expansion.json":RESUME?"valen-structure-probe-extension-r6.json":EXTENSION?"valen-structure-probe-extension.json":"valen-structure-probe.json");
    private static final Set<String> EXPANSION_IDS=HANGAR_HELIPAD?Set.of("jeg:valen_hangars/bamboo_hangar","jeg:valen_hangars/birch_hangar","jeg:valen_hangars/cherry_hangar","jeg:valen_hangars/dark_oak_hangar","jeg:valen_hangars/jungle_hangar","jeg:valen_hangars/plains_hangar","jeg:valen_hangars/savannah_hangar","jeg:valen_hangars/taiga_hangar","jeg:valen_helipads/desert_helipad","jeg:valen_helipads/forest_helipad"):COMPONENTS?Set.of("jeg:valen/armory","jeg:valen/listening_post"):TOWER?Set.of("jeg:valen/watchtower"):SCORCHED?Set.of("jeg:valen_scorched/asgharian_citadel","jeg:valen_scorched/osgood_lab","jeg:valen_scorched/osgood_lab_outpost","jeg:valen_scorched/trench"):HBM_NEXT?Set.of("jeg:valen/beached_patrol","jeg:valen/civil_defense_shelter","jeg:valen/oil_rig","jeg:valen/submarine_wreck"):Set.of("jeg:valen/aircraft_carrier","jeg:valen/forest_outpost","jeg:valen/arctic_station","jeg:valen/radio_house","jeg:valen/broadcasting_tower","jeg:valen/abandoned_factory");
    private static final Map<String,int[]> EXPANSION_COUNTS=HANGAR_HELIPAD?Map.of("jeg:valen_helipads/desert_helipad",new int[]{6,8,0},"jeg:valen_helipads/forest_helipad",new int[]{8,4,0}):COMPONENTS?Map.of("jeg:valen/armory",new int[]{5,28,0},"jeg:valen/listening_post",new int[]{2,2,0}):TOWER?Map.of("jeg:valen/watchtower",new int[]{2,0,0}):SCORCHED?Map.of("jeg:valen_scorched/asgharian_citadel",new int[]{6,8,0},"jeg:valen_scorched/osgood_lab",new int[]{3,5,0},"jeg:valen_scorched/osgood_lab_outpost",new int[]{4,4,0},"jeg:valen_scorched/trench",new int[]{3,7,0}):HBM_NEXT?Map.of("jeg:valen/beached_patrol",new int[]{3,9,0},"jeg:valen/civil_defense_shelter",new int[]{10,18,0},"jeg:valen/oil_rig",new int[]{9,65,0},"jeg:valen/submarine_wreck",new int[]{9,17,0}):Map.of(
        "jeg:valen/aircraft_carrier",new int[]{7,89,1},"jeg:valen/forest_outpost",new int[]{1,3,1},"jeg:valen/arctic_station",new int[]{9,16,1},
        "jeg:valen/radio_house",new int[]{9,7,1},"jeg:valen/broadcasting_tower",new int[]{4,0,1},"jeg:valen/abandoned_factory",new int[]{6,26,4});
    private static final Path PRIOR_EXPANSION=Path.of(RETRY_KNOWN?System.getProperty("valenjeg.evidence.2e25860f3a44", "VALENJEG_EVIDENCE_NOT_CONFIGURED/r11-expansion-native.json"):System.getProperty("valenjeg.evidence.6224099ae356", "VALENJEG_EVIDENCE_NOT_CONFIGURED/native-structure-expansion.json"));
    private static final Path PRIOR_EIGHT=Path.of(HBM_NEXT?System.getProperty("valenjeg.evidence.a3624f2f351a", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index.json"):System.getProperty("valenjeg.evidence.e7a1c2b93321", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index.json"));
    private static final Set<String> PENDING_IDS=RESUME?Set.of("jeg:valen/vertibird_wreck"):Set.of("jeg:valen/vertibird_wreck","jeg:valen_ad_astra/moon_dungeon");
    private static final Map<BlockPos,Integer> priorDelays=new HashMap<>();
    private static final Map<BlockPos,Set<UUID>> priorGhouls=new HashMap<>();
    private static final Map<BlockPos,JsonObject> trackedSpawners=new LinkedHashMap<>();
    private static JsonObject activeReceipt;
    private static int monitorTicks;
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    private static JsonArray xyz(BlockPos p){JsonArray a=new JsonArray();a.add(p.getX());a.add(p.getY());a.add(p.getZ());return a;}
    private static JsonArray box(BoundingBox b){JsonArray a=new JsonArray();for(int v:new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()})a.add(v);return a;}
    private static boolean ours(Identifier id){String path=id.getPath();return id.getNamespace().equals("jeg")&&(path.startsWith("valen/")||path.startsWith("valen_ad_astra/")||path.startsWith("valen_community/")||path.startsWith("valen_scorched/")||path.startsWith("valen_hangars/")||path.startsWith("valen_helipads/")||INDUSTRIAL&&path.startsWith("valen_industrial/")||BUNKER&&path.equals("bunker_rooms")||MINER&&path.equals("valen_mining/miner_hut"));}
    private static boolean reservedIndustrialChunk(int x,int z){return x==20485&&z==30718||x==19960&&z==29441||x==23568&&z==37124||x==23999&&z==38090;}
    private static boolean reservedIndustrialBox(BoundingBox box){return box.intersects(327748,50,491476,327788,83,491518)||box.intersects(319329,51,471027,319372,102,471068)||box.intersects(377064,55,593960,377096,80,593992)||box.intersects(383992,76,609416,384024,101,609448);}
    private static void save(JsonObject r){try{Files.writeString(REPORT,GSON.toJson(r));}catch(Exception e){throw new RuntimeException(e);}}
    public static void run(ServerLevel level,JsonObject stationReceipt) {
        if(!Boolean.getBoolean("valen.structure.probe")){stationReceipt.addProperty("structure_probe","explicit opt-in not set");return;}
        if(MINER)MinerOutpostContracts.requirePassed();
        else if(BUNKER)BunkerRoomsContracts.requirePassed();
        else if(DESERT)DesertComplexContracts.requirePassed();
        else if(INDUSTRIAL)IndustrialSourceContracts.requirePassed();
        check(Files.exists(Path.of("../preparation.json")),"Structure probe requires the separately prepared fixture");
        if(HANGAR_HELIPAD)HangarHelipadContracts.requirePassed();
        Job job=new Job(level);stationReceipt.add("structures",job.receipt);activeReceipt=job.receipt;save(job.receipt);
        ServerTickEvents.END_SERVER_TICK.register(server->{if(!job.done)job.step();monitor(level);});
    }
    private static final class Job {
        final ServerLevel level;
        final JsonObject receipt=new JsonObject();final JsonArray sites=new JsonArray();
        List<Holder.Reference<Structure>> structures;final Map<Holder.Reference<Structure>,Integer> originalIndices=new HashMap<>();
        int priorFullChunks,eligibleTrials,startAttempt,siteEmptyStorage,emptyStorage,contractMismatches,importedExpansionChunks,knownIndex;final List<JsonObject> knownCandidates=new ArrayList<>();final Map<String,Integer> resumeAfter=new HashMap<>();
        final Set<Long> fullChunks=new HashSet<>(),historicalReadChunks=new HashSet<>(),r39Chunks=new HashSet<>();final Set<ResourceKey<LootTable>> lootKeys=new LinkedHashSet<>();
        final int maxChunks=EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER?384:Integer.getInteger("valen.structure.probe.maxChunks",192),maxCandidates=EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER?64:Integer.getInteger("valen.structure.probe.maxCandidates",24);
        int index,attempt,generated,containers,spawners,siteContainers,siteSpawners,ticks,revalidatedNaturalStarts;boolean revalidatingJungle;JsonObject originalJungleRow;JsonArray jungleSavedTables;
        String phase="select";boolean done;Structure structure;Holder.Reference<Structure> holder;JsonObject site;
        final Map<String,List<BlockPos>> recordedBiomeOrigins=new HashMap<>();final Set<Long> inner1CheckedChunks=new HashSet<>();int inner1NextGrid;AirfieldNativeSearch airfieldSearch,desertSearch,minerSearch;StructureStart nativeStart;BoundingBox bounds;List<BoundingBox> pieceBounds=List.of();List<ChunkPos> chunks;int chunkIndex;int minerEntityTicks;final Set<Long> minerTickets=new HashSet<>();
        Job(ServerLevel level){
            this.level=level;check(maxChunks>0&&maxChunks<=384&&maxCandidates>0&&maxCandidates<=64,"Probe bounds outside staged limits");
            var all=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().filter(h->ours(h.key().identifier())).sorted(Comparator.comparing(h->h.key().identifier().toString())).toList();
            for(int n=0;n<all.size();n++)originalIndices.put(all.get(n),n);
            structures=MINER?all.stream().filter(h->h.key().identifier().toString().equals("jeg:valen_mining/miner_hut")).toList():BUNKER?all.stream().filter(h->h.key().identifier().toString().equals("jeg:bunker_rooms")).toList():DESERT?all.stream().filter(h->h.key().identifier().toString().equals("jeg:valen/desert_atom_complex")).toList():INDUSTRIAL?all.stream().filter(h->INDUSTRIAL_SLOTS.containsKey(h.key().identifier().toString())).toList():EXPANSION?all.stream().filter(h->EXPANSION_IDS.contains(h.key().identifier().toString())&&(!EXPANSION_RESUME||h.key().identifier().toString().equals("jeg:valen/arctic_station"))&&(!HBM_NEXT_RESUME||h.key().identifier().toString().equals("jeg:valen/beached_patrol"))&&(!SCORCHED_RESUME||h.key().identifier().toString().equals("jeg:valen_scorched/osgood_lab"))).toList():EXTENSION?all.stream().filter(h->PENDING_IDS.contains(h.key().identifier().toString())).toList():all;
            if(MINER){
                check(!DESERT&&!INDUSTRIAL&&!BUNKER&&!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&!Boolean.getBoolean("valen.structure.probe.expansion")&&!Boolean.getBoolean("valen.structure.probe.resume")&&!Boolean.getBoolean("valen.structure.probe.pendingOnly"),"Miner outpost search must stay exclusive of industrial, desert, bunker, and older probe modes");
                check(structures.size()==1,"Expected only jeg:valen_mining/miner_hut");MinerOutpostContracts.importThirtyNine(receipt);
                for(var holder:structures)originalIndices.put(holder,40);
                receipt.addProperty("search_grid_slot",40);
                receipt.addProperty("search_origin","One miner outpost target uses reserved grid 40. Slots through 39 stay reserved. The 39 passed rows are imported verbatim and are not regenerated.");
                receipt.addProperty("old_world_replayed",false);receipt.addProperty("old_inventory_replayed",false);receipt.addProperty("forced_fake_starts",false);
            }else if(BUNKER){
                check(!DESERT&&!INDUSTRIAL&&!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&!Boolean.getBoolean("valen.structure.probe.expansion")&&!Boolean.getBoolean("valen.structure.probe.resume")&&!Boolean.getBoolean("valen.structure.probe.pendingOnly")&&!Boolean.getBoolean("valen.structure.probe.cherryFreshTrace")&&!Boolean.getBoolean("valen.structure.probe.cherryInner1Trace"),"Bunker rooms search must stay exclusive of desert, industrial, cherry inner1, and older probe modes");
                check(structures.size()==1,"Expected only jeg:bunker_rooms");importPriorThirtySix();
                for(var holder:structures)originalIndices.put(holder,39);
                receipt.addProperty("search_grid_slot",39);receipt.remove("template_ground_y");receipt.remove("source_ground_y");receipt.remove("heightmap_proposal_absolute");
                receipt.addProperty("search_origin","One bunker_rooms target uses reserved grid 39. Slots 27, 35, 36, 37, and 38 stay reserved. The 36 passed rows are imported verbatim and are not regenerated.");
                receipt.addProperty("reserved_r39_cherry",true);receipt.addProperty("reserved_r40_failed_desert",true);receipt.addProperty("reserved_r41_wet_desert",true);receipt.addProperty("reserved_r43_desert_atom",true);
                receipt.addProperty("old_inventory_replayed",false);receipt.addProperty("ghoul_activation",false);
            }else if(DESERT){
                check(!INDUSTRIAL&&!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&!Boolean.getBoolean("valen.structure.probe.expansion")&&!Boolean.getBoolean("valen.structure.probe.resume")&&!Boolean.getBoolean("valen.structure.probe.pendingOnly"),"Desert complex search must stay exclusive of industrial and older probe modes");
                check(structures.size()==1,"Expected only jeg:valen/desert_atom_complex");importPriorThirtySix();if(DESERT_LOCATOR)importPriorR39();
            }else if(INDUSTRIAL){
                check(!HANGAR_HELIPAD&&!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&!Boolean.getBoolean("valen.structure.probe.expansion")&&!Boolean.getBoolean("valen.structure.probe.resume")&&!Boolean.getBoolean("valen.structure.probe.pendingOnly"),"Industrial search must stay exclusive of older probe modes");
                check(structures.size()==2,"Expected only the warehouse and train station");importPriorThirtyFour();if(INDUSTRIAL_RESUME)importIndustrialR35();
            }else if(HANGAR_HELIPAD){
                check(!COMPONENTS&&!TOWER&&!SCORCHED&&!HBM_NEXT&&!HBM_NEXT_RESUME,"Hangar/helipad mode must be exclusive");
                check(structures.size()==10,"Expected exact8hangar/2helipad registrations");importPriorTwentyFive(all);if(HANGAR_HELIPAD_RESUME)importAirfieldPartial();
            }else if(COMPONENTS){
                check(!TOWER&&!SCORCHED&&!HBM_NEXT&&!HBM_NEXT_RESUME,"Component and other appended modes are mutually exclusive");
                check(structures.size()==2,"Expected exact two authored component registrations");
                importPriorTwentyThree(all);
            }else if(TOWER){
                check(!SCORCHED&&!HBM_NEXT&&!HBM_NEXT_RESUME,"Tower and other appended modes are mutually exclusive");
                check(structures.size()==1,"Expected exact standalone watchtower registration");
                importPriorTwentyTwo(all);
            }else if(SCORCHED){
                check(!HBM_NEXT&&!HBM_NEXT_RESUME,"Scorched and HBM modes are mutually exclusive");
                check(structures.size()==(SCORCHED_RESUME?1:4),"Expected exact Scorched registrations");
                if(SCORCHED_RESUME)importScorchedPartial(all);else importPriorEighteen(all);
            }else if(EXPANSION){
                check(structures.size()==(HBM_NEXT_RESUME||EXPANSION_RESUME?1:EXPECTED_EXPANSION_COUNT),"Expected expansion registrations missing");
                try{var prior=JsonParser.parseString(Files.readString(PRIOR_EIGHT)).getAsJsonObject();
                    check(level.getSeed()==-4056840922791553232L&&!prior.get("forced_starts").getAsBoolean(),"Expansion requires original copied fixture seed and natural baseline");
                    var old=prior.getAsJsonArray("sites");check(old.size()==BASELINE_COUNT,"Expected exact previously proved baseline starts");
                    priorFullChunks=prior.get("full_chunks").getAsInt();check(priorFullChunks>=0,"Prior chunk budget invalid");
                    List<String> baselineIds=new ArrayList<>();for(var row:old)baselineIds.add(row.getAsJsonObject().get("id").getAsString());Collections.sort(baselineIds);
                    var newIds=EXPANSION_IDS.stream().sorted().toList();for(var h:all){String id=h.key().identifier().toString();if(EXPANSION_IDS.contains(id)){check(!baselineIds.contains(id),"Expansion target already in prior baseline");originalIndices.put(h,BASELINE_COUNT+newIds.indexOf(id));}}
                    receipt.addProperty(HBM_NEXT?"prior_fourteen_receipt":"prior_eight_receipt",PRIOR_EIGHT.toString());receipt.addProperty("prior_full_chunk_count",priorFullChunks);receipt.addProperty(HBM_NEXT?"prior_fourteen_terrain_searches_replayed":"prior_eight_terrain_searches_replayed",false);
                    receipt.addProperty("search_origin",HBM_NEXT?"Original14 proven grid slots reserved; four new sorted IDs use slots14..17, trial1. Only these four new configured IDs searched.":"Original eight grid slots reserved; six new sorted IDs use stable slots8..13 and begin trial1. Only six new IDs searched.");
                    if(HBM_NEXT){check(priorFullChunks==284,"Exact prior fourteen FULL budget differs");check(new HashSet<>(baselineIds).size()==14,"Prior fourteen IDs are not unique");for(var row:old)check(row.getAsJsonObject().get("status").getAsString().startsWith("actual native start"),"Prior baseline contains an unproved site");receipt.add("prior_fourteen_site_ids",GSON.toJsonTree(baselineIds));receipt.addProperty("prior_marker_counts","147loot/85Ghoulspawners plus141new emptycontainers retained without replay");}
                    receipt.addProperty("max_grid_trials_each",384);receipt.addProperty("max_placement_eligible_candidates_each",64);
                    if(HBM_NEXT_RESUME){
                        byte[] frozen=Files.readAllBytes(PRIOR_HBM_NEXT);String actual;
                        try{actual=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(frozen));}catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
                        check(actual.equals(PRIOR_HBM_NEXT_SHA),"Immutable three-pass native receipt hash differs");
                        var previous=JsonParser.parseString(new String(frozen,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                        check(previous.get("world_seed").getAsLong()==level.getSeed()&&!previous.get("forced_fake_starts").getAsBoolean()&&previous.get("registered_structure_count").getAsInt()==4&&previous.get("generated_structure_count").getAsInt()==3&&previous.get("source_marker_count_mismatches").getAsInt()==0,"Prior native seed/count/marker contracts differ");
                        check(previous.get("full_chunk_count").getAsInt()==30&&previous.get("expansion_FULL_chunks_including_imported").getAsInt()==30&&previous.get("total_full_chunks_including_prior").getAsInt()==314,"Prior native FULL counters differ");
                        check(previous.get("placed_cache_count").getAsInt()==28&&previous.get("placed_empty_storage_count").getAsInt()==100&&previous.get("placed_spawner_count").getAsInt()==0,"Prior native placed marker totals differ");
                        var provenance=new JsonObject();Set<String> ids=new HashSet<>();int imported=0;
                        for(var element:previous.getAsJsonArray("sites")){
                            var row=element.getAsJsonObject();String id=row.get("id").getAsString();
                            check(EXPANSION_IDS.contains(id)&&ids.add(id),"Unexpected or duplicate prior native site");
                            check(row.get("search_grid_slot").getAsInt()==BASELINE_COUNT+newIds.indexOf(id),"Original native search slot differs");
                            if(id.equals("jeg:valen/beached_patrol")){
                                check(row.get("status").getAsString().startsWith("pending:")&&row.get("candidates_checked").getAsInt()==68&&row.get("placement_eligible_candidates").getAsInt()==64,"Actual unfinished beach counters/status differ");
                                resumeAfter.put(id,row.get("candidates_checked").getAsInt());receipt.add("prior_beach_diagnostics",row.deepCopy());
                            }else{
                                int[] expected=EXPANSION_COUNTS.get(id);
                                check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==expected[0]&&row.get("placed_empty_storage_count").getAsInt()==expected[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==expected[2],"Unproved prior site cannot be imported");
                                sites.add(row.deepCopy());provenance.addProperty(id,PRIOR_HBM_NEXT.toString());imported++;
                            }
                        }
                        check(ids.equals(EXPANSION_IDS)&&imported==3&&resumeAfter.size()==1,"Expected exact three native passed sites and one beach counter");
                        generated=3;containers=28;emptyStorage=100;spawners=0;importedExpansionChunks=30;priorFullChunks=314;
                        receipt.addProperty("prior_three_pass_receipt",PRIOR_HBM_NEXT.toString());receipt.addProperty("prior_three_pass_sha256",actual);receipt.add("imported_native_row_provenance",provenance);
                        receipt.addProperty("imported_completed_sites",3);receipt.addProperty("imported_completed_rows_unchanged",true);receipt.addProperty("completed_three_terrain_or_loot_replayed",false);receipt.addProperty("imported_expansion_FULL_chunks",30);receipt.addProperty("prior_full_chunk_count",314);
                        receipt.addProperty("search_origin","Only beached_patrol continues after actual grid68/eligible64 at unchanged slot14. Exact three native rows imported without mutation or replay; old14/284FULL retained.");
                        receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));receipt.addProperty("max_grid_trials_total",384);receipt.addProperty("remaining_grid_trials_at_resume",316);receipt.addProperty("max_placement_eligible_candidates_this_run",64);
                    }
                    if(EXPANSION_RESUME){
                        var previous=JsonParser.parseString(Files.readString(PRIOR_EXPANSION)).getAsJsonObject();
                        check(previous.get("world_seed").getAsLong()==level.getSeed()&&previous.get("registered_structure_count").getAsInt()==6&&previous.get("source_marker_count_mismatches").getAsInt()==0,"Prior expansion seed/count/marker contract differs");
                        int imported=0;for(var element:previous.getAsJsonArray("sites")){var row=element.getAsJsonObject();String id=row.get("id").getAsString();check(EXPANSION_IDS.contains(id),"Unexpected prior expansion ID");
                            if(id.equals("jeg:valen/arctic_station")){check(row.get("status").getAsString().startsWith("pending:"),"Arctic already completed");resumeAfter.put(id,row.get("candidates_checked").getAsInt());receipt.add("prior_arctic_diagnostics",row.deepCopy());}
                            else{check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("native_marker_counts_match_source").getAsBoolean(),"Cannot import unproved expansion site");var copy=row.deepCopy();copy.addProperty("readback_imported_unchanged",true);if(!copy.has("source_native_receipt"))copy.addProperty("source_native_receipt",PRIOR_EXPANSION.toString());copy.addProperty("imported_via_native_receipt",PRIOR_EXPANSION.toString());sites.add(copy);imported++;}
                        }
                        check(imported==5&&resumeAfter.containsKey("jeg:valen/arctic_station"),"Expected five completed sites and one Arctic counter");
                        generated=imported;containers=previous.get("placed_cache_count").getAsInt();emptyStorage=previous.get("placed_empty_storage_count").getAsInt();spawners=previous.get("placed_spawner_count").getAsInt();
                        importedExpansionChunks=previous.has("expansion_FULL_chunks_including_imported")?previous.get("expansion_FULL_chunks_including_imported").getAsInt():previous.get("full_chunk_count").getAsInt();priorFullChunks=previous.get("total_full_chunks_including_prior").getAsInt();check(priorFullChunks>=0,"Imported FULL chunk budget invalid");
                        receipt.addProperty("prior_expansion_receipt",PRIOR_EXPANSION.toString());receipt.addProperty("imported_completed_expansion_sites",5);receipt.addProperty("imported_expansion_FULL_chunks",importedExpansionChunks);receipt.addProperty("prior_full_chunk_count",priorFullChunks);receipt.addProperty("completed_five_terrain_or_loot_replayed",false);
                        receipt.addProperty("search_origin","Only Arctic resumes after immutableR10 candidates_checked, same grid slot10; imported five native starts/readbacks are not re-read or replayed.");receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));
                        if(RETRY_KNOWN){
                            var oldDiagnostics=previous.getAsJsonObject("prior_arctic_diagnostics").getAsJsonArray("candidate_diagnostics");
                            for(int number:new int[]{7,36}){JsonObject found=null;for(var entry:oldDiagnostics){var candidate=entry.getAsJsonObject();if(candidate.get("grid_attempt").getAsInt()==number)found=candidate;}check(found!=null&&found.get("result").getAsString().equals("native_biome_filter_reject"),"Known native candidate context missing");knownCandidates.add(found.deepCopy());}
                            receipt.add("known_native_candidates_from_R10",GSON.toJsonTree(knownCandidates));receipt.addProperty("search_origin","Retry original native configured Arctic candidates7then36 under corrected34biome tag; current placement eligibility and native starts only. Linear search counter137 is retained, not reset.");receipt.addProperty("max_known_candidates",2);
                        }
                    }
                }catch(java.io.IOException e){throw new IllegalStateException("Missing preserved original eight-site index/expansion receipt",e);}
            }
            if(EXTENSION&&!EXPANSION&&!INDUSTRIAL){
                check(structures.size()==(RESUME?1:2),"Pending-only IDs missing");
                try{var prior=JsonParser.parseString(Files.readString(Path.of("valen-structure-probe.json"))).getAsJsonObject();
                    check(prior.get("world_seed").getAsLong()==level.getSeed(),"Extension baseline seed differs");
                    priorFullChunks=prior.get("full_chunk_count").getAsInt();check(priorFullChunks>=0,"Prior chunk budget exceeded");
                    receipt.addProperty("prior_receipt","valen-structure-probe.json (unchanged)");receipt.addProperty("prior_full_chunk_count",priorFullChunks);
                    JsonArray activation=new JsonArray();receipt.add("prior_sites_activation_only",activation);
                    for(var priorSite:prior.getAsJsonArray("sites")){
                        var ps=priorSite.getAsJsonObject();if(!ps.has("spawners"))continue;
                        for(var entry:ps.getAsJsonArray("spawners")){var row=entry.deepCopy().getAsJsonObject();row.addProperty("source_site",ps.get("id").getAsString());var xyz=row.getAsJsonArray("pos");trackedSpawners.put(new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt()),row);activation.add(row);}
                    }
                }catch(java.io.IOException e){throw new IllegalStateException("Missing preserved first-slice receipt",e);}
                if(RESUME){
                    try{var extension=JsonParser.parseString(Files.readString(Path.of("valen-structure-probe-extension.json"))).getAsJsonObject();
                        check(extension.get("world_seed").getAsLong()==level.getSeed(),"Resume receipt seed differs");
                        priorFullChunks=extension.get("total_full_chunks_including_prior").getAsInt();check(priorFullChunks>=0,"Resume prior chunks exceed budget");
                        receipt.addProperty("prior_extension_receipt","valen-structure-probe-extension.json (unchanged)");receipt.addProperty("prior_full_chunk_count",priorFullChunks);
                        for(var element:extension.getAsJsonArray("sites")){var ps=element.getAsJsonObject();String id=ps.get("id").getAsString();
                            if(PENDING_IDS.contains(id)){check(ps.get("status").getAsString().startsWith("pending:"),"Resume target already completed");resumeAfter.put(id,ps.get("candidates_checked").getAsInt());}
                            if(ps.has("spawners"))for(var entry:ps.getAsJsonArray("spawners")){var row=entry.deepCopy().getAsJsonObject();row.addProperty("source_site",id);var pos=row.getAsJsonArray("pos");trackedSpawners.put(new BlockPos(pos.get(0).getAsInt(),pos.get(1).getAsInt(),pos.get(2).getAsInt()),row);receipt.getAsJsonArray("prior_sites_activation_only").add(row);}
                        }
                        check(resumeAfter.keySet().equals(PENDING_IDS),"Resume target counters missing");receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));
                    }catch(java.io.IOException e){throw new IllegalStateException("Missing preserved pending extension receipt",e);}
                }
                receipt.addProperty("search_origin",RESUME?"Original registry indices/grid retained; each target resumes after its preserved prior candidates_checked":"Original sorted registry indices and grid retained; continue after original24 trials");
                receipt.addProperty("max_grid_trials_each",384);receipt.addProperty("max_placement_eligible_candidates_each",64);
                receipt.addProperty("native_biome_diagnostic","Native findValidGenerationPoint with original biome predicate recorded; no pieces builder invoked, no start inserted");
            }
            check(!structures.isEmpty(),"No actual Valen structure registrations");receipt.add("sites",sites);receipt.addProperty("status","running incrementally");receipt.addProperty("world_seed",level.getSeed());
            receipt.addProperty("scope","actual configured native starts, full-chunk generation and placed block entity readback in copied fixture world");
            receipt.addProperty("forced_fake_starts",false);receipt.addProperty("production_world_modified",false);receipt.addProperty("max_full_chunks",maxChunks);receipt.addProperty("full_chunk_budget_basis","NEW FULL chunks generated in this run only; immutable historical totals remain informational");receipt.addProperty("candidate_limit_each",maxCandidates);
            receipt.addProperty("terrain_visual_acceptance","pending native screenshots/inspection; actual start bounds and terrain measurements recorded separately");
            receipt.addProperty("activation_proof",DESERT?"Desert complex adds no Ghoul activation and no CacheItemPool roll. Visual, kinetic, and mesh acceptance stay separate.":INDUSTRIAL?"Source zombie and zombie_villager spawners stay on the warehouse; no old Ghoul activation replay. Visual acceptance stays separate.":COMPONENTS?"These two authored components add0spawners; prior85spawner/visual evidence remains separate.":TOWER?"This authored watchtower adds0spawners; prior85spawner/visual evidence remains separate.":SCORCHED||HBM_NEXT?"These four source templates add0spawners; prior85spawner/visual evidence remains separate.":"pending actual nearby fixture player; new Ghoul plus native spawner delay reset monitored");
        }
        // Launch-bound immutable proof import. Old rows remain verbatim; no terrain,
        // loot or activation is replayed, and older schemas need no invented fields.
        JsonObject boundReceipt(Path path,String expected)throws java.io.IOException {
            check(path.isAbsolute()&&expected!=null&&expected.matches("[0-9a-f]{64}"),"Absolute receipt path and exact SHA-256 required");
            byte[] bytes=Files.readAllBytes(path);String actual;
            try{actual=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
            check(actual.equals(expected),"Prior receipt SHA-256 differs: "+path);
            return JsonParser.parseString(new String(bytes,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        }
        void importPriorThirtyFour(){
            try{
                var prior=boundReceipt(PRIOR_34,PRIOR_34_SHA);
                check(level.getSeed()==-4056840922791553232L&&!prior.get("forced_starts").getAsBoolean()&&!prior.get("old_rows_replayed").getAsBoolean()&&prior.get("entry_count").getAsInt()==34,"Exact 34-row natural index required");
                var parents=prior.getAsJsonArray("parents");check(parents.size()==3,"34-row ancestry is not the three frozen parents");
                String[][] expectedParents={
                    {System.getProperty("valenjeg.evidence.06bce6bb2695", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-25.json"),"d39d8a0b6b3b15f809bb6bbcc0aac4e2b5283692dea5b5766f937f325acda09c"},
                    {System.getProperty("valenjeg.evidence.7c58eeb7f13d", "VALENJEG_EVIDENCE_NOT_CONFIGURED/native-hangar-helipad-eight-pass-partial.json"),"bdbd97c3ec399c97aed4e17c98d01f6dbea09b44766d0d3eb5091382e4b64605"},
                    {System.getProperty("valenjeg.evidence.383f66330cac", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-hangar-helipad-anchors-r32.json"),"0f94f6db7b26af4185cb2cdb5d0d1f177b631b437ee18fe6382804ad8200cd29"}};
                for(int n=0;n<3;n++){var parent=parents.get(n).getAsJsonObject();check(parent.get("path").getAsString().equals(expectedParents[n][0])&&parent.get("sha256").getAsString().equals(expectedParents[n][1]),"34-row parent path or hash differs");var proof=boundReceipt(Path.of(parent.get("path").getAsString()),parent.get("sha256").getAsString());check(proof.get("world_seed").getAsLong()==level.getSeed(),"34-row parent seed differs");}
                var rows=prior.getAsJsonArray("sites");Set<String> ids=new HashSet<>();Set<Integer> slots=new HashSet<>();
                for(var element:rows){var row=element.getAsJsonObject();String id=row.get("id").getAsString();check(ids.add(id)&&row.get("status").getAsString().startsWith("actual native start"),"34-row proof is not a unique passed source row");check(!id.equals("jeg:valen_hangars/cherry_hangar")&&!id.equals("jeg:valen_minipurge/underground_shelter"),"Failed cherry or bunker row is not one of the 34 passed sources");if(row.has("search_grid_slot")){int slot=row.get("search_grid_slot").getAsInt();check(slot!=27&&slot!=35&&slot<36,"Passed rows must not occupy the failed cherry slot, bunker slot, or the new industrial slots");slots.add(slot);}}
                check(ids.size()==34&&slots.containsAll(Set.of(25,26,28,29,30,31,32,33,34))&&!slots.contains(27),"Hangar slots 25..34 stay reserved, including the still-failed cherry 27; passed count 34 is not the next grid");
                for(var holder:structures){int slot=INDUSTRIAL_SLOTS.get(holder.key().identifier().toString());check(slot==36||slot==37,"Industrial grid slot must be36or37");originalIndices.put(holder,slot);}
                priorFullChunks=0;receipt.addProperty("prior_thirty_four_receipt",PRIOR_34.toString());receipt.addProperty("prior_thirty_four_sha256",PRIOR_34_SHA);receipt.add("prior_thirty_four_sites",rows.deepCopy());receipt.add("prior_thirty_four_parents",parents.deepCopy());
                receipt.addProperty("prior_thirty_four_rows_imported_verbatim",true);receipt.addProperty("prior_thirty_four_world_loot_or_activation_replayed",false);receipt.add("historical_r32_new_full_not_a_search_base",prior.get("R32_native_NEW_FULL").deepCopy());
                receipt.add("industrial_grid_slots",GSON.toJsonTree(INDUSTRIAL_SLOTS));receipt.addProperty("reserved_failed_cherry_slot",27);receipt.addProperty("reserved_bunker_slot",35);receipt.addProperty("grid_base_not_passed_count",true);
                receipt.addProperty("search_origin","Sorted train station and warehouse use reserved grids 36 and 37. Slots 25..34 and bunker slot 35 stay reserved. The 34 passed rows are imported verbatim and are not regenerated.");
                receipt.addProperty("max_grid_trials_each",384);receipt.addProperty("max_placement_eligible_candidates_each",64);receipt.addProperty("max_new_full_chunks",384);
                receipt.addProperty("marker_expectations","Read from the passing raw preflight native Container decode. Empty Create container totals are not a fixed 72 or 108.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing immutable 34-row structure index",error);}
        }
        void importIndustrialR35(){
            try{
                var prior=boundReceipt(PRIOR_R35,PRIOR_R35_SHA);
                check(level.getSeed()==-4056840922791553232L&&prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_fake_starts").getAsBoolean(),"Frozen R35 industrial world seed required");
                check(prior.get("prior_thirty_four_sha256").getAsString().equals(PRIOR_34_SHA)&&prior.get("generated_structure_count").getAsInt()==2&&prior.get("searched_structure_count_this_run").getAsInt()==2,"R35 is not the two-structure run over the 34-row index");
                var rows=prior.getAsJsonArray("sites");check(rows.size()==2,"R35 did not record the two industrial sites");
                var train=rows.get(0).getAsJsonObject();var warehouse=rows.get(1).getAsJsonObject();
                check(train.get("id").getAsString().equals("jeg:valen_industrial/train_station")&&train.get("search_grid_slot").getAsInt()==36&&train.get("candidates_checked").getAsInt()==5&&train.get("start_chunk_x").getAsInt()==20485&&train.get("start_chunk_z").getAsInt()==30718,"R35 train station identity differs");
                check(warehouse.get("id").getAsString().equals("jeg:valen_industrial/warehouse")&&warehouse.get("search_grid_slot").getAsInt()==37&&warehouse.get("candidates_checked").getAsInt()==1&&warehouse.get("start_chunk_x").getAsInt()==19960&&warehouse.get("start_chunk_z").getAsInt()==29441,"R35 warehouse identity differs");
                check(train.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&train.getAsJsonObject("authored_source_contract").get("sha256").getAsString().equals("8f3f9c68d5e63bd0fff17eccac20a8d931557b5d52c942ed08c00460ea97abb4"),"R35 train station proof differs");
                check(warehouse.get("status").getAsString().equals("actual native start placed; source marker count mismatch")&&warehouse.get("authored_source_contract_error").getAsString().equals("java.lang.AssertionError: Placed source block changed or became air at BlockPos{x=319360, y=64, z=471056}"),"R35 warehouse failure proof differs");
                check(boxEquals(train.getAsJsonArray("bounding_box"),327748,50,491476,327788,83,491518)&&boxEquals(warehouse.getAsJsonArray("bounding_box"),319329,51,471027,319372,102,471068),"R35 industrial boxes differ");
                resumeAfter.put(train.get("id").getAsString(),5);resumeAfter.put(warehouse.get("id").getAsString(),1);
                receipt.add("prior_r35_sites",rows.deepCopy());receipt.addProperty("prior_r35_receipt",PRIOR_R35.toString());receipt.addProperty("prior_r35_sha256",PRIOR_R35_SHA);
                receipt.addProperty("prior_r35_train_station_status",train.get("status").getAsString());receipt.addProperty("prior_r35_warehouse_error",warehouse.get("authored_source_contract_error").getAsString());
                receipt.addProperty("prior_r35_native_field_loss","R35 raw load returned nonnull while logging Casing NONE, Phase SEARCH_INPUTS, empty HeldItem, LastKnownPos/Controller compounds, and Inventory compounds. That loss stays historical and is not new-source proof.");
                receipt.addProperty("prior_r35_world_regions_replayed",false);receipt.addProperty("prior_r35_lost_nbt_relabeled_compatible",false);receipt.addProperty("old_inventory_replayed",false);
                receipt.addProperty("fresh_source_warehouse_sha256","76fbfa91f53059ced9581f49d7da16408319bddb5da5fa7beb27436d47d2965a");receipt.addProperty("fresh_source_train_station_sha256","5ad98bcf4eae732ec0e936bfee34d956d0b3e122487efc6b6b2d8850740fa3d6");
                receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));receipt.addProperty("max_grid_trials_total_each",384);receipt.addProperty("max_placement_eligible_candidates_this_run",64);receipt.addProperty("max_new_full_chunks",384);
                receipt.addProperty("search_origin","Fresh train station and warehouse continue grids 36 and 37 after R35 attempts 5 and 1, up to 384 total each. This run may add 64 new eligible candidates and 384 new FULL chunks. The two R35 boxes and both bunker boxes stay reserved. The 34 rows stay verbatim. Old placements and inventories are not reread.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing frozen R35 industrial report",error);}
        }
        void importPriorThirtySix(){
            try{
                var prior=boundReceipt(PRIOR_36,PRIOR_36_SHA);
                check(level.getSeed()==-4056840922791553232L&&prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_starts").getAsBoolean()&&!prior.get("old_rows_replayed").getAsBoolean()&&prior.get("entry_count").getAsInt()==36,"Exact 36-row natural index required");
                var parents=prior.getAsJsonArray("parents");check(parents.size()==2,"36-row ancestry is not the two frozen parents");
                String[][] expectedParents={
                    {System.getProperty("valenjeg.evidence.d448615a7633", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-34.json"),"53807e814653b830c77883329ada4dffb9a5be8e4c62c7179c2d2a0cae944b80"},
                    {System.getProperty("valenjeg.evidence.33a512bc7cc7", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-industrial-resume-r36.json"),PRIOR_R36_SHA}};
                for(int n=0;n<2;n++){var parent=parents.get(n).getAsJsonObject();check(parent.get("path").getAsString().equals(expectedParents[n][0])&&parent.get("sha256").getAsString().equals(expectedParents[n][1]),"36-row parent path or hash differs");var proof=boundReceipt(Path.of(parent.get("path").getAsString()),parent.get("sha256").getAsString());if(n==0)check(proof.get("entry_count").getAsInt()==34&&!proof.get("forced_starts").getAsBoolean()&&!proof.get("old_rows_replayed").getAsBoolean(),"34-row parent is not the frozen natural index");else check(proof.get("world_seed").getAsLong()==level.getSeed(),"R36 parent seed differs");}
                String[] expectedIds={"jeg:valen/field_lab","jeg:valen/military_base","jeg:valen/missile_silo","jeg:valen/research_lab","jeg:valen/satellite_station","jeg:valen_ad_astra/space_station","jeg:valen_ad_astra/moon_dungeon","jeg:valen/vertibird_wreck","jeg:valen/abandoned_factory","jeg:valen/aircraft_carrier","jeg:valen/broadcasting_tower","jeg:valen/forest_outpost","jeg:valen/radio_house","jeg:valen/arctic_station","jeg:valen/civil_defense_shelter","jeg:valen/oil_rig","jeg:valen/submarine_wreck","jeg:valen/beached_patrol","jeg:valen_scorched/asgharian_citadel","jeg:valen_scorched/osgood_lab_outpost","jeg:valen_scorched/trench","jeg:valen_scorched/osgood_lab","jeg:valen/watchtower","jeg:valen/armory","jeg:valen/listening_post","jeg:valen_hangars/plains_hangar","jeg:valen_hangars/savannah_hangar","jeg:valen_hangars/taiga_hangar","jeg:valen_helipads/forest_helipad","jeg:valen_hangars/bamboo_hangar","jeg:valen_hangars/birch_hangar","jeg:valen_hangars/dark_oak_hangar","jeg:valen_hangars/jungle_hangar","jeg:valen_helipads/desert_helipad","jeg:valen_industrial/train_station","jeg:valen_industrial/warehouse"};
                var rows=prior.getAsJsonArray("sites");check(rows.size()==expectedIds.length,"36-row index length differs");Set<Integer> slots=new HashSet<>();
                for(int n=0;n<rows.size();n++){var row=rows.get(n).getAsJsonObject();String id=row.get("id").getAsString();check(id.equals(expectedIds[n])&&row.get("status").getAsString().startsWith("actual native start"),"36-row proof is not the verbatim passed source row");check(!id.equals("jeg:valen/desert_atom_complex")&&!id.equals("jeg:valen_hangars/cherry_hangar")&&!id.equals("jeg:valen_minipurge/underground_shelter"),"Desert, failed cherry, or bunker row is inside the imported 36");if(row.has("search_grid_slot")){int slot=row.get("search_grid_slot").getAsInt();check(slot!=27&&slot!=35&&slot!=38,"Imported rows must not occupy cherry 27, bunker 35, or desert 38");slots.add(slot);}}
                check(slots.contains(36)&&slots.contains(37)&&!slots.contains(27)&&!slots.contains(35)&&!slots.contains(38),"Train 36 and warehouse 37 stay reserved; cherry, bunker, and desert slots stay unused by the 36");
                for(var holder:structures)originalIndices.put(holder,38);
                receipt.add("prior_thirty_six_sites",rows.deepCopy());receipt.add("prior_thirty_six_parents",parents.deepCopy());receipt.addProperty("prior_thirty_six_receipt",PRIOR_36.toString());receipt.addProperty("prior_thirty_six_sha256",PRIOR_36_SHA);
                receipt.addProperty("prior_thirty_six_rows_imported_verbatim",true);receipt.addProperty("prior_thirty_six_world_loot_or_activation_replayed",false);receipt.addProperty("old_inventory_replayed",false);receipt.addProperty("forced_fake_starts",false);
                receipt.addProperty("search_grid_slot",38);receipt.addProperty("reserved_failed_cherry_slot",27);receipt.addProperty("reserved_bunker_slot",35);receipt.addProperty("reserved_train_station_slot",36);receipt.addProperty("reserved_warehouse_slot",37);
                receipt.addProperty("max_grid_trials_total",384);receipt.addProperty("max_placement_eligible_candidates",64);receipt.addProperty("max_new_full_chunks",384);
                receipt.addProperty("template_ground_y",4);receipt.addProperty("source_ground_y",-1);receipt.addProperty("heightmap_proposal_absolute",-4);receipt.addProperty("placement_equivalence_claimed",false);
                receipt.addProperty("search_origin","One desert complex target uses reserved grid 38. Slots 27, 35, 36, and 37 stay reserved. The 36 passed rows are imported verbatim and are not regenerated.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing immutable 36-row structure index",error);}
        }
        boolean protectedHistoricalChunk(int x,int z){
            for(var entry:receipt.getAsJsonArray("prior_thirty_six_sites")){
                var row=entry.getAsJsonObject();if(!row.has("bounding_box"))continue;var b=row.getAsJsonArray("bounding_box");
                if(x>=(b.get(0).getAsInt()>>4)&&x<=(b.get(3).getAsInt()>>4)&&z>=(b.get(2).getAsInt()>>4)&&z<=(b.get(5).getAsInt()>>4))return true;
            }
            return (x>=16753&&x<=16756&&z>=25261&&z<=25265)||(x>=16978&&x<=16982&&z>=25132&&z<=25135)||(x>=13675&&x<=13677&&z>=22432&&z<=22434)||(x>=57466&&x<=57470&&z>=106684&&z<=106687);
        }
        boolean protectedHistoricalBox(BoundingBox bounds){
            for(var entry:receipt.getAsJsonArray("prior_thirty_six_sites")){
                var row=entry.getAsJsonObject();if(!row.has("bounding_box"))continue;var b=row.getAsJsonArray("bounding_box");
                if(bounds.intersects(b.get(0).getAsInt(),b.get(2).getAsInt(),b.get(3).getAsInt(),b.get(5).getAsInt()))return true;
            }
            return bounds.intersects(268052,404188,268109,404252)||bounds.intersects(271660,402115,271724,402172)||bounds.intersects(218806,358916,218844,358958)||bounds.intersects(919468,1706947,919532,1707004);
        }
        void importPriorR39(){
            try{
                var prior=boundReceipt(PRIOR_R39,PRIOR_R39_SHA);
                check(level.getSeed()==-4056840922791553232L&&prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_fake_starts").getAsBoolean()&&prior.get("generated_structure_count").getAsInt()==0&&prior.get("prior_thirty_six_sha256").getAsString().equals(PRIOR_36_SHA),"Frozen R39 desert report seed or 36-row parent differs");
                var rows=prior.getAsJsonArray("sites");check(rows.size()==1,"R39 did not record the one desert site");
                var row=rows.get(0).getAsJsonObject();
                check(row.get("id").getAsString().equals("jeg:valen/desert_atom_complex")&&row.get("search_grid_slot").getAsInt()==38&&row.get("candidates_checked").getAsInt()==67&&row.get("placement_eligible_candidates").getAsInt()==64&&row.get("status").getAsString().startsWith("pending:"),"R39 desert identity differs");
                int biome=0,exclusion=0;var diags=row.getAsJsonArray("candidate_diagnostics");check(diags.size()==67,"R39 did not record 67 candidates");
                for(var element:diags){var trial=element.getAsJsonObject();String result=trial.get("result").getAsString();if(result.equals("native_biome_filter_reject"))biome++;else if(result.equals("native_exclusion_reject"))exclusion++;else check(false,"R39 candidate result was not a biome or exclusion reject");check(r39Chunks.add(ChunkPos.pack(trial.get("chunk_x").getAsInt(),trial.get("chunk_z").getAsInt())),"R39 chunk repeated inside its own report");}
                check(biome==64&&exclusion==3&&r39Chunks.size()==67,"R39 distribution is not 64 biome rejects and 3 exclusion rejects");
                receipt.addProperty("prior_r39_receipt",PRIOR_R39.toString());receipt.addProperty("prior_r39_sha256",PRIOR_R39_SHA);receipt.addProperty("prior_r39_grid_attempts",67);receipt.addProperty("prior_r39_eligible",64);receipt.addProperty("prior_r39_chunks_not_repeated",67);receipt.addProperty("prior_r39_rows_replayed",false);receipt.addProperty("prior_thirty_six_rows_unchanged",true);
                receipt.addProperty("this_run_grid_cap",384);receipt.addProperty("this_run_eligible_cap",64);receipt.addProperty("this_run_new_full_cap",384);receipt.addProperty("locator_biome_calls_max",4);receipt.addProperty("locator_origins",4);receipt.addProperty("locator_configured_regions_max",100);receipt.addProperty("native_success_claimed",false);
                receipt.addProperty("desert_native_search","Fresh findClosestBiome3d over registered allowed biomes at four immutable actual desert observations. The 67 R39 chunks and all36historicalsiteboxes/bothcherryboxes/bunkers stay protected.");
                var failedR40=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.e63c886f1e92", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-desert-native-search.json")),"b1d477102f54cd7e3517d2bb7c3257e99f021f0294c4856d4b4fbfe53274ecfb");
                check(failedR40.get("world_seed").getAsLong()==level.getSeed(),"Failed R40 seed differs");
                var failedSite=failedR40.getAsJsonArray("sites").get(0).getAsJsonObject();
                check(failedSite.get("start_chunk_x").getAsInt()==16982&&failedSite.get("start_chunk_z").getAsInt()==25135&&!failedSite.get("native_marker_counts_match_source").getAsBoolean(),"Failed R40 site identity differs");
                r39Chunks.add(ChunkPos.pack(16982,25135));receipt.addProperty("failed_R40_site_preserved_not_replayed",true);
                receipt.addProperty("failed_R40_receipt_sha256","b1d477102f54cd7e3517d2bb7c3257e99f021f0294c4856d4b4fbfe53274ecfb");
                var wetR41=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.6a4bed31e5fd", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-desert-native-search-r41.json")),"d3a2d20c2761a24475e65db8f7a01eee3578df33d597bc02ae03449ddf6e4add");
                check(wetR41.get("world_seed").getAsLong()==level.getSeed(),"R41 failed-site seed differs");
                var wetSite=wetR41.getAsJsonArray("sites").get(0).getAsJsonObject();
                check(wetSite.get("start_chunk_x").getAsInt()==16754&&wetSite.get("start_chunk_z").getAsInt()==25265&&!wetSite.get("native_marker_counts_match_source").getAsBoolean(),"R41 wet-site identity differs");
                r39Chunks.add(ChunkPos.pack(16754,25265));receipt.addProperty("failed_R41_wet_site_preserved_not_replayed",true);
                receipt.addProperty("failed_R41_receipt_sha256","d3a2d20c2761a24475e65db8f7a01eee3578df33d597bc02ae03449ddf6e4add");
                var observed=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.4fe698e05bdd", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-cherry-fresh-trace.json")),"9795d03f02487cfd61fed17e24ee607584da5849848a7a2ea32b14f9ea2c0792");
                check(observed.get("world_seed").getAsLong()==level.getSeed(),"Desert observation seed differs");
                List<BlockPos> origins=new ArrayList<>();JsonArray originEvidence=new JsonArray();
                for(var observation:observed.getAsJsonArray("immutable_native_biome_observation_origins")){
                    var origin=observation.getAsJsonObject();
                    if(!origin.get("target").getAsString().equals("jeg:valen_helipads/desert_helipad")||!origin.get("actual_observed_biome").getAsString().equals("minecraft:desert")||origin.get("actual_observed_biome_count").getAsInt()<1)continue;
                    if(origins.size()==4)break;
                    origins.add(new BlockPos(origin.get("chunk_x").getAsInt()*16+8,0,origin.get("chunk_z").getAsInt()*16+8));originEvidence.add(origin.deepCopy());
                }
                var r31=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.7c58eeb7f13d", "VALENJEG_EVIDENCE_NOT_CONFIGURED/native-hangar-helipad-eight-pass-partial.json")),"bdbd97c3ec399c97aed4e17c98d01f6dbea09b44766d0d3eb5091382e4b64605");
                check(r31.get("world_seed").getAsLong()==level.getSeed(),"Fourth desert origin seed differs");
                for(var originRow31:r31.getAsJsonArray("sites")){
                    var site31=originRow31.getAsJsonObject();if(!site31.get("id").getAsString().equals("jeg:valen_helipads/desert_helipad"))continue;
                    for(var query:site31.getAsJsonArray("native_biome_locator_queries")){
                        var found=query.getAsJsonObject();
                        if(found.get("result").getAsString().equals("native_biome_found")&&found.get("biome").getAsString().equals("minecraft:desert")){
                            origins.add(new BlockPos(found.get("x").getAsInt(),found.get("y").getAsInt(),found.get("z").getAsInt()));originEvidence.add(found.deepCopy());break;
                        }
                    }
                }
                check(origins.size()==4,"Four immutable actual desert observations required");
                var r42=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.2a99d9680889", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-desert-native-search-r42.json")),"2755ef4041c310d3baebae454b3652f9d17c1e5a18523c6ef28db3d29d2bd340");
                check(r42.get("world_seed").getAsLong()==level.getSeed()&&r42.get("source_marker_count_mismatches").getAsInt()==0,"R42 search parent differs");
                var r42site=r42.getAsJsonArray("sites").get(0).getAsJsonObject();check(r42site.get("candidates_checked").getAsInt()==50,"R42 did not exhaust exactly50recorded candidates");
                for(var entry:r42site.getAsJsonArray("candidate_diagnostics")){var trial42=entry.getAsJsonObject();r39Chunks.add(ChunkPos.pack(trial42.get("chunk_x").getAsInt(),trial42.get("chunk_z").getAsInt()));}
                receipt.addProperty("R42_checked_candidates_skipped",50);receipt.addProperty("R42_search_sha256","2755ef4041c310d3baebae454b3652f9d17c1e5a18523c6ef28db3d29d2bd340");

                recordedBiomeOrigins.put("jeg:valen/desert_atom_complex",List.copyOf(origins));receipt.add("native_desert_observation_origins",originEvidence);
                receipt.addProperty("native_desert_observation_parent_sha256","9795d03f02487cfd61fed17e24ee607584da5849848a7a2ea32b14f9ea2c0792");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing frozen R39 desert report",error);}
        }
        boolean boxEquals(JsonArray box,int... expected){if(box.size()!=expected.length)return false;for(int i=0;i<expected.length;i++)if(box.get(i).getAsInt()!=expected[i])return false;return true;}
        void importPriorEighteen(List<Holder.Reference<Structure>> all){
            importPriorEighteen(all,System.getProperty("valen.structure.probe.scorchedPrior18"),System.getProperty("valen.structure.probe.scorchedPrior18Sha"));
        }
        void importPriorEighteen(List<Holder.Reference<Structure>> all,String configured,String expected){
            check(configured!=null&&!configured.isBlank(),"Scorched prior18 launch property required");
            Path path=Path.of(configured);
            try{
                var prior=boundReceipt(path,expected);
                check(level.getSeed()==-4056840922791553232L&&prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_starts").getAsBoolean(),"Exact natural prior18 world seed required");
                var bindings=prior.getAsJsonObject("source_receipts");
                var oldBinding=bindings.getAsJsonObject("prior14");var hbmBinding=bindings.getAsJsonObject("hbm4");
                String oldSha=oldBinding.get("sha256").getAsString(),hbmSha=hbmBinding.get("sha256").getAsString();
                check(oldSha.equals("47040516396e9606d2b162500fb6b98feac72c952b29e0cbc8731c227be29821")&&hbmSha.equals("665ce04f201c7649ac0811f5b20a497e287ad0ba43c5f47c5cd423617e34bf01"),"Exact old14 and final HBM4 proof bindings differ");
                var old=boundReceipt(Path.of(oldBinding.get("path").getAsString()),oldSha);
                var hbm=boundReceipt(Path.of(hbmBinding.get("path").getAsString()),hbmSha);
                var oldRows=old.getAsJsonArray("sites");var hbmRows=hbm.getAsJsonArray("sites");var rows=prior.getAsJsonArray("sites");
                check(oldRows.size()==14&&hbmRows.size()==4&&rows.size()==18&&!old.get("forced_starts").getAsBoolean(),"Exact fourteen plus four natural baseline required");
                check(old.get("full_chunks").getAsInt()==284&&old.get("loot_containers").getAsInt()==147&&old.get("ghoul_spawners").getAsInt()==85,"Old14 proven native counts differ");
                check(hbm.get("world_seed").getAsLong()==level.getSeed()&&!hbm.get("forced_fake_starts").getAsBoolean()&&hbm.get("generated_structure_count").getAsInt()==4&&hbm.get("source_marker_count_mismatches").getAsInt()==0,"Final HBM4 proof incomplete");
                check(hbm.get("placed_cache_count").getAsInt()==31&&hbm.get("placed_empty_storage_count").getAsInt()==109&&hbm.get("placed_spawner_count").getAsInt()==0&&hbm.get("total_full_chunks_including_prior").getAsInt()==322,"Final HBM4 marker/FULL counts differ");
                Map<String,int[]> hbmCounts=Map.of("jeg:valen/beached_patrol",new int[]{3,9,0},"jeg:valen/civil_defense_shelter",new int[]{10,18,0},"jeg:valen/oil_rig",new int[]{9,65,0},"jeg:valen/submarine_wreck",new int[]{9,17,0});
                Set<String> ids=new HashSet<>(),hbmIds=new HashSet<>();
                for(int n=0;n<18;n++){
                    var row=rows.get(n).getAsJsonObject();String id=row.get("id").getAsString();
                    check(ids.add(id)&&!EXPANSION_IDS.contains(id)&&row.equals(n<14?oldRows.get(n):hbmRows.get(n-14)),"Prior18 rows must match immutable original proofs verbatim");
                    check(row.get("status").getAsString().startsWith("actual native start"),"Unproved baseline row cannot be imported");
                    if(n>=14){int[] count=hbmCounts.get(id);check(count!=null&&hbmIds.add(id)&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==count[0]&&row.get("placed_empty_storage_count").getAsInt()==count[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==count[2],"Final four HBM row marker contracts differ");}
                }
                check(hbmIds.equals(hbmCounts.keySet())&&prior.get("full_chunks").getAsInt()==322&&prior.get("loot_containers").getAsInt()==178&&prior.get("ghoul_spawners").getAsInt()==85&&prior.get("known_empty_storage_containers").getAsInt()==250,"Combined prior18 totals differ");
                priorFullChunks=322;var newIds=EXPANSION_IDS.stream().sorted().toList();
                for(var holder:all){String id=holder.key().identifier().toString();if(EXPANSION_IDS.contains(id))originalIndices.put(holder,18+newIds.indexOf(id));}
                receipt.addProperty("prior_eighteen_receipt",path.toString());receipt.addProperty("prior_eighteen_sha256",expected);receipt.add("prior_eighteen_sites",rows.deepCopy());receipt.add("prior_eighteen_source_receipts",bindings.deepCopy());
                receipt.addProperty("prior_full_chunk_count",322);receipt.addProperty("prior_eighteen_terrain_searches_replayed",false);receipt.addProperty("prior_eighteen_loot_or_activation_replayed",false);receipt.addProperty("prior_eighteen_rows_imported_verbatim",true);
                receipt.addProperty("prior_marker_counts","178loot/85Ghoulspawners/250knownempty retained as immutable evidence, separate from new4 counters");
                receipt.addProperty("search_origin","Only four pinned Scorched single-root structures; stable sorted slots18..21 trial1. Prior18 exact source-bound rows imported without world replay.");
                receipt.addProperty("max_grid_trials_each",384);receipt.addProperty("max_placement_eligible_candidates_each",64);
            }catch(java.io.IOException error){throw new IllegalStateException("Missing launch-bound prior18 native proof",error);}
        }
        void importScorchedPartial(List<Holder.Reference<Structure>> all){
            try{
                var prior=boundReceipt(PRIOR_SCORCHED_PARTIAL,PRIOR_SCORCHED_PARTIAL_SHA);
                importPriorEighteen(all,prior.get("prior_eighteen_receipt").getAsString(),prior.get("prior_eighteen_sha256").getAsString());
                check(prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_fake_starts").getAsBoolean()&&prior.get("registered_structure_count").getAsInt()==4&&prior.get("generated_structure_count").getAsInt()==3&&prior.get("source_marker_count_mismatches").getAsInt()==0,"Exact partial Scorched native seed/counts required");
                check(prior.get("full_chunk_count").getAsInt()==48&&prior.get("total_full_chunks_including_prior").getAsInt()==370&&prior.get("placed_cache_count").getAsInt()==13&&prior.get("placed_empty_storage_count").getAsInt()==19&&prior.get("placed_spawner_count").getAsInt()==0,"Actual partial Scorched marker/FULL totals differ");
                check(prior.getAsJsonArray("prior_eighteen_sites").equals(receipt.getAsJsonArray("prior_eighteen_sites")),"Partial Scorched prior18 rows differ");
                Set<String> ids=new HashSet<>();int imported=0;var provenance=new JsonObject();var sorted=EXPANSION_IDS.stream().sorted().toList();
                for(var item:prior.getAsJsonArray("sites")){
                    var row=item.getAsJsonObject();String id=row.get("id").getAsString();check(EXPANSION_IDS.contains(id)&&ids.add(id)&&row.get("search_grid_slot").getAsInt()==18+sorted.indexOf(id),"Unexpected partial Scorched ID/slot");
                    if(id.equals("jeg:valen_scorched/osgood_lab")){
                        check(row.get("status").getAsString().startsWith("pending:")&&row.get("candidates_checked").getAsInt()==64&&row.get("placement_eligible_candidates").getAsInt()==64,"Actual Lab pending counter differs");
                        resumeAfter.put(id,row.get("candidates_checked").getAsInt());receipt.add("prior_lab_diagnostics",row.deepCopy());
                    }else{
                        int[] expected=EXPANSION_COUNTS.get(id);
                        check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("piece_count").getAsInt()==1&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==expected[0]&&row.get("placed_empty_storage_count").getAsInt()==expected[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Unproved partial Scorched row");
                        sites.add(row.deepCopy());provenance.addProperty(id,PRIOR_SCORCHED_PARTIAL.toString());imported++;
                    }
                }
                check(ids.equals(EXPANSION_IDS)&&imported==3&&resumeAfter.size()==1,"Exactly three completed Scorched rows and one Lab pending counter required");
                generated=3;containers=13;emptyStorage=19;spawners=0;priorFullChunks=370;importedExpansionChunks=48;
                receipt.addProperty("prior_scorched_partial",PRIOR_SCORCHED_PARTIAL.toString());receipt.addProperty("prior_scorched_partial_sha256",PRIOR_SCORCHED_PARTIAL_SHA);receipt.add("imported_native_row_provenance",provenance);
                receipt.addProperty("imported_completed_sites",3);receipt.addProperty("completed_three_rows_imported_verbatim",true);receipt.addProperty("completed_three_world_or_loot_replayed",false);
                receipt.addProperty("prior_eighteen_full_chunk_count",322);receipt.addProperty("prior_full_chunk_count",370);receipt.addProperty("imported_expansion_FULL_chunks",48);
                receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));receipt.addProperty("search_origin","Only Osgood Lab resumes after actual64 grid trials at stable slot19; three native rows imported verbatim; no old21 replay");
                receipt.addProperty("max_grid_trials_total",384);receipt.addProperty("remaining_grid_trials_at_resume",320);receipt.addProperty("max_new_placement_eligible_candidates",64);
            }catch(java.io.IOException error){throw new IllegalStateException("Missing immutable partial Scorched native receipt",error);}
        }
        void importPriorTwentyTwo(List<Holder.Reference<Structure>> all){
            importPriorTwentyTwo(all,System.getProperty("valen.structure.probe.towerPrior22"),System.getProperty("valen.structure.probe.towerPrior22Sha"));
        }
        void importPriorTwentyTwo(List<Holder.Reference<Structure>> all,String configured,String expected){
            check(configured!=null&&!configured.isBlank(),"Tower prior22 absolute launch-bound receipt required");
            Path path=Path.of(configured);
            try{
                var prior=boundReceipt(path,expected);var bindings=prior.getAsJsonObject("source_receipts");
                var oldBinding=bindings.getAsJsonObject("prior18");var newBinding=bindings.getAsJsonObject("scorched4");
                String oldSha=oldBinding.get("sha256").getAsString();
                check(oldSha.equals("007a3cd1f96d8ca875a997ddb72c0ac7fc6c12290acbf67e2fee24f32dfa68cc"),"Exact original prior18 proof binding differs");
                var old=boundReceipt(Path.of(oldBinding.get("path").getAsString()),oldSha);
                var scorched=boundReceipt(Path.of(newBinding.get("path").getAsString()),newBinding.get("sha256").getAsString());
                importPriorEighteen(all,oldBinding.get("path").getAsString(),oldSha);
                check(prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_starts").getAsBoolean(),"Exact natural combined prior22 seed required");
                check(scorched.get("world_seed").getAsLong()==level.getSeed()&&!scorched.get("forced_fake_starts").getAsBoolean()&&scorched.get("registered_structure_count").getAsInt()==4&&scorched.get("generated_structure_count").getAsInt()==4&&scorched.get("source_marker_count_mismatches").getAsInt()==0,"Four actual Scorched natural starts/readbacks required");
                check(scorched.get("placed_cache_count").getAsInt()==16&&scorched.get("placed_empty_storage_count").getAsInt()==24&&scorched.get("placed_spawner_count").getAsInt()==0&&scorched.get("total_full_chunks_including_prior").getAsInt()>=322,"Scorched final marker/FULL counts differ");
                check(scorched.get("prior_eighteen_sha256").getAsString().equals(oldSha)&&scorched.getAsJsonArray("prior_eighteen_sites").equals(old.getAsJsonArray("sites")),"Scorched prior18 immutable rows/binding differ");
                var rows=prior.getAsJsonArray("sites");var oldRows=old.getAsJsonArray("sites");var newRows=scorched.getAsJsonArray("sites");
                check(rows.size()==22&&oldRows.size()==18&&newRows.size()==4,"Exact22prior rows required");
                Map<String,int[]> counts=Map.of("jeg:valen_scorched/asgharian_citadel",new int[]{6,8,0},"jeg:valen_scorched/osgood_lab",new int[]{3,5,0},"jeg:valen_scorched/osgood_lab_outpost",new int[]{4,4,0},"jeg:valen_scorched/trench",new int[]{3,7,0});
                var sortedIds=counts.keySet().stream().sorted().toList();Set<String> ids=new HashSet<>(),newIds=new HashSet<>();
                for(int n=0;n<22;n++){
                    var row=rows.get(n).getAsJsonObject();String id=row.get("id").getAsString();
                    check(ids.add(id)&&!EXPANSION_IDS.contains(id)&&row.equals(n<18?oldRows.get(n):newRows.get(n-18))&&row.get("status").getAsString().startsWith("actual native start"),"All prior22 unique rows must match original proofs verbatim");
                    if(n>=18){int[] c=counts.get(id);check(c!=null&&newIds.add(id)&&row.get("search_grid_slot").getAsInt()==18+sortedIds.indexOf(id)&&row.get("piece_count").getAsInt()==1&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==c[0]&&row.get("placed_empty_storage_count").getAsInt()==c[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==c[2],"Actual Scorched row contracts differ");}
                }
                int phaseFull=scorched.has("expansion_FULL_chunks_including_imported")?scorched.get("expansion_FULL_chunks_including_imported").getAsInt():scorched.get("full_chunk_count").getAsInt();
                if(scorched.has("prior_scorched_partial")){
                    check(scorched.get("prior_scorched_partial_sha256").getAsString().equals(PRIOR_SCORCHED_PARTIAL_SHA),"Final Scorched partial binding differs");
                    var partial=boundReceipt(Path.of(scorched.get("prior_scorched_partial").getAsString()),PRIOR_SCORCHED_PARTIAL_SHA);
                    check(scorched.get("prior_full_chunk_count").getAsInt()==370&&scorched.get("imported_expansion_FULL_chunks").getAsInt()==48&&phaseFull==48+scorched.get("full_chunk_count").getAsInt()&&partial.get("prior_eighteen_sha256").getAsString().equals(oldSha),"Resumed Scorched FULL/ancestry differs");
                    int matched=0;for(var oldRow:partial.getAsJsonArray("sites"))if(oldRow.getAsJsonObject().get("status").getAsString().equals("actual native start and full-chunk placement read back")){check(newRows.asList().stream().anyMatch(oldRow::equals),"Passed Scorched row changed during resume");matched++;}
                    check(matched==3,"Exact three prior Scorched actual rows required");
                }else check(scorched.get("prior_full_chunk_count").getAsInt()==322&&phaseFull==scorched.get("full_chunk_count").getAsInt(),"Initial Scorched FULL accounting differs");
                int full=prior.get("full_chunks").getAsInt();
                check(newIds.equals(counts.keySet())&&full==scorched.get("total_full_chunks_including_prior").getAsInt()&&full==322+phaseFull&&full>=0&&prior.get("loot_containers").getAsInt()==194&&prior.get("ghoul_spawners").getAsInt()==85&&prior.get("known_empty_storage_containers").getAsInt()==274,"Combined prior22 FULL/marker totals differ");
                priorFullChunks=full;for(var holder:all)if(EXPANSION_IDS.contains(holder.key().identifier().toString()))originalIndices.put(holder,22);
                receipt.addProperty("prior_twenty_two_receipt",path.toString());receipt.addProperty("prior_twenty_two_sha256",expected);receipt.add("prior_twenty_two_sites",rows.deepCopy());receipt.add("prior_twenty_two_source_receipts",bindings.deepCopy());
                receipt.addProperty("prior_full_chunk_count",full);receipt.addProperty("prior_twenty_two_rows_imported_verbatim",true);receipt.addProperty("prior_twenty_two_terrain_searches_replayed",false);receipt.addProperty("prior_twenty_two_loot_or_activation_replayed",false);
                receipt.addProperty("prior_marker_counts","194loot/85Ghoulspawners/274knownempty retained as immutable evidence; tower counters remain separate");
                receipt.addProperty("search_origin","Only actual standalone authored watchtower; stable slot22 trial1. Exact prior22 proof rows imported without world replay.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing launch-bound prior22 native proof",error);}
        }
        void importPriorTwentyThree(List<Holder.Reference<Structure>> all){
            importPriorTwentyThree(all,System.getProperty("valen.structure.probe.componentsPrior23"),System.getProperty("valen.structure.probe.componentsPrior23Sha"));
        }
        void importPriorTwentyThree(List<Holder.Reference<Structure>> all,String configured,String expected){
            check(configured!=null&&!configured.isBlank(),"Component prior23 absolute launch-bound receipt required");
            Path path=Path.of(configured);
            try{
                var prior=boundReceipt(path,expected);var bindings=prior.getAsJsonObject("source_receipts");
                var oldBinding=bindings.getAsJsonObject("prior22");var towerBinding=bindings.getAsJsonObject("tower1");
                String oldPath=oldBinding.get("path").getAsString(),oldSha=oldBinding.get("sha256").getAsString();
                var old=boundReceipt(Path.of(oldPath),oldSha);var tower=boundReceipt(Path.of(towerBinding.get("path").getAsString()),towerBinding.get("sha256").getAsString());
                importPriorTwentyTwo(all,oldPath,oldSha);
                check(prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_starts").getAsBoolean(),"Exact natural combined prior23 seed required");
                check(tower.get("world_seed").getAsLong()==level.getSeed()&&!tower.get("forced_fake_starts").getAsBoolean()&&tower.get("registered_structure_count").getAsInt()==1&&tower.get("generated_structure_count").getAsInt()==1&&tower.get("source_marker_count_mismatches").getAsInt()==0,"One actual natural standalone tower start/readback required");
                check(tower.get("placed_cache_count").getAsInt()==2&&tower.get("placed_empty_storage_count").getAsInt()==0&&tower.get("placed_spawner_count").getAsInt()==0&&tower.get("prior_twenty_two_sha256").getAsString().equals(oldSha)&&tower.getAsJsonArray("prior_twenty_two_sites").equals(old.getAsJsonArray("sites")),"Tower final markers/prior22 immutable rows/binding differ");
                var rows=prior.getAsJsonArray("sites");var oldRows=old.getAsJsonArray("sites");var towerRows=tower.getAsJsonArray("sites");
                check(rows.size()==23&&oldRows.size()==22&&towerRows.size()==1,"Exact23prior rows required");Set<String> ids=new HashSet<>();
                for(int n=0;n<23;n++){
                    var row=rows.get(n).getAsJsonObject();String id=row.get("id").getAsString();
                    check(ids.add(id)&&!EXPANSION_IDS.contains(id)&&row.equals(n<22?oldRows.get(n):towerRows.get(0))&&row.get("status").getAsString().startsWith("actual native start"),"All prior23 unique rows must match original proofs verbatim");
                    if(n==22)check(id.equals("jeg:valen/watchtower")&&row.get("search_grid_slot").getAsInt()==22&&row.get("piece_count").getAsInt()==1&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==2&&row.get("placed_empty_storage_count").getAsInt()==0&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Actual tower row contracts differ");
                }
                int oldFull=old.get("full_chunks").getAsInt(),towerFull=tower.get("full_chunk_count").getAsInt(),full=prior.get("full_chunks").getAsInt();
                check(towerFull>0&&towerFull<=384&&tower.get("prior_full_chunk_count").getAsInt()==oldFull&&full==tower.get("total_full_chunks_including_prior").getAsInt()&&full==oldFull+towerFull&&full>=0&&prior.get("loot_containers").getAsInt()==196&&prior.get("ghoul_spawners").getAsInt()==85&&prior.get("known_empty_storage_containers").getAsInt()==274,"Combined prior23 historicalFULL/marker totals differ");
                priorFullChunks=full;var sortedIds=EXPANSION_IDS.stream().sorted().toList();for(var holder:all){String id=holder.key().identifier().toString();if(EXPANSION_IDS.contains(id))originalIndices.put(holder,23+sortedIds.indexOf(id));}
                receipt.addProperty("prior_twenty_three_receipt",path.toString());receipt.addProperty("prior_twenty_three_sha256",expected);receipt.add("prior_twenty_three_sites",rows.deepCopy());receipt.add("prior_twenty_three_source_receipts",bindings.deepCopy());
                receipt.addProperty("prior_full_chunk_count",full);receipt.addProperty("prior_twenty_three_rows_imported_verbatim",true);receipt.addProperty("prior_twenty_three_terrain_searches_replayed",false);receipt.addProperty("prior_twenty_three_loot_or_activation_replayed",false);
                receipt.addProperty("prior_marker_counts","196loot/85Ghoulspawners/274knownempty retained as immutable evidence; new2component counters remain separate");
                receipt.addProperty("search_origin","Only actual authored armory/listeningpost; sorted stable slots23..24 trial1. Exact prior23 proof rows imported without world replay;384NEW FULL current-run budget retained.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing launch-bound prior23 native proof",error);}
        }
        void importPriorTwentyFive(List<Holder.Reference<Structure>> all){
            String configured=System.getProperty("valen.structure.probe.hangarHelipadPrior25"),expected=System.getProperty("valen.structure.probe.hangarHelipadPrior25Sha");
            check(configured!=null&&!configured.isBlank(),"New10 requires launch-bound actual prior25, never pending/fabricated rows");
            try{
                var prior=boundReceipt(Path.of(configured),expected);check(expected.equals("d39d8a0b6b3b15f809bb6bbcc0aac4e2b5283692dea5b5766f937f325acda09c"),"Exact actual R26 prior25 required");
                var bindings=prior.getAsJsonObject("source_receipts");var oldBinding=bindings.getAsJsonObject("prior23");var newBinding=bindings.getAsJsonObject("components2");
                String oldPath=oldBinding.get("path").getAsString(),oldSha=oldBinding.get("sha256").getAsString();var old=boundReceipt(Path.of(oldPath),oldSha);
                importPriorTwentyThree(all,oldPath,oldSha);var components=boundReceipt(Path.of(newBinding.get("path").getAsString()),newBinding.get("sha256").getAsString());
                check(newBinding.get("sha256").getAsString().equals("ba740409f0a55f97671a81ebd2f363c883a93705a587f6f108311f51e2966877"),"Exact actual two-component native proof required");
                check(prior.get("world_seed").getAsLong()==level.getSeed()&&!prior.get("forced_starts").getAsBoolean()&&components.get("world_seed").getAsLong()==level.getSeed()&&!components.get("forced_fake_starts").getAsBoolean(),"Prior25 natural seed differs");
                check(components.get("generated_structure_count").getAsInt()==2&&components.get("registered_structure_count").getAsInt()==2&&components.get("source_marker_count_mismatches").getAsInt()==0&&components.get("placed_cache_count").getAsInt()==7&&components.get("placed_empty_storage_count").getAsInt()==30&&components.get("placed_spawner_count").getAsInt()==0,"Two actual components are incomplete");
                check(components.get("prior_twenty_three_sha256").getAsString().equals(oldSha)&&components.getAsJsonArray("prior_twenty_three_sites").equals(old.getAsJsonArray("sites")),"Actual component prior23 ancestry differs");
                var rows=prior.getAsJsonArray("sites");var oldRows=old.getAsJsonArray("sites");var componentRows=components.getAsJsonArray("sites");
                check(rows.size()==25&&oldRows.size()==23&&componentRows.size()==2,"Exact25actual rows required");Set<String> unique=new HashSet<>();
                Map<String,int[]> counts=Map.of("jeg:valen/armory",new int[]{5,28,0},"jeg:valen/listening_post",new int[]{2,2,0});
                for(int n=0;n<25;n++){
                    var row=rows.get(n).getAsJsonObject();String id=row.get("id").getAsString();
                    check(unique.add(id)&&!EXPANSION_IDS.contains(id)&&row.equals(n<23?oldRows.get(n):componentRows.get(n-23))&&row.get("status").getAsString().startsWith("actual native start"),"Prior25 rows must be unique and verbatim actual proofs");
                    if(n>=23){var c=counts.get(id);check(c!=null&&row.get("piece_count").getAsInt()==1&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.get("placed_cache_count").getAsInt()==c[0]&&row.get("placed_empty_storage_count").getAsInt()==c[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Actual component row differs");}
                }
                int run=components.get("full_chunk_count").getAsInt(),full=prior.get("full_chunks").getAsInt();
                check(run==3&&components.get("prior_full_chunk_count").getAsInt()==old.get("full_chunks").getAsInt()&&full==old.get("full_chunks").getAsInt()+run&&full==390&&full==components.get("total_full_chunks_including_prior").getAsInt()&&prior.get("loot_containers").getAsInt()==203&&prior.get("ghoul_spawners").getAsInt()==85&&prior.get("known_empty_storage_containers").getAsInt()==304,"Actual prior25 FULL/marker counters differ");
                priorFullChunks=full;var sorted=EXPANSION_IDS.stream().sorted().toList();for(var h:all){String id=h.key().identifier().toString();if(EXPANSION_IDS.contains(id))originalIndices.put(h,25+sorted.indexOf(id));}
                receipt.addProperty("prior_twenty_five_receipt",configured);receipt.addProperty("prior_twenty_five_sha256",expected);receipt.add("prior_twenty_five_sites",rows.deepCopy());receipt.add("prior_twenty_five_source_receipts",bindings.deepCopy());
                receipt.addProperty("prior_full_chunk_count",full);receipt.addProperty("prior_twenty_five_rows_imported_verbatim",true);receipt.addProperty("prior_twenty_five_world_loot_activation_replayed",false);
                receipt.addProperty("search_origin","Only8hangars+2helipads sorted slots25..34; actual prior25 frozen rows retained without world replay;384NEW FULL/run");
                receipt.addProperty("max_grid_trials_each",384);receipt.addProperty("max_placement_eligible_candidates_each",64);
            }catch(java.io.IOException error){throw new IllegalStateException("Missing actual launch-bound prior25",error);}
        }

        void importAirfieldR29Partial(String path,String sha){
            check(path!=null&&!path.isBlank(),"Resume requires actual immutable final R29 native report");
            try{
                check("cfe49fd921de4096489a8c562672dc52f1a68a1a5296ee34824b19fac0011229".equals(sha),"Exact final R29 native partial required");
                var previous=boundReceipt(Path.of(path),sha);
                check(previous.get("world_seed").getAsLong()==level.getSeed()&&!previous.get("forced_fake_starts").getAsBoolean()&&!previous.get("production_world_modified").getAsBoolean(),"R29 copied-world natural provenance differs");
                check(previous.get("status").getAsString().equals("partial native generation/readback; inspect pending sites")&&previous.get("registered_structure_count").getAsInt()==10&&previous.get("generated_structure_count").getAsInt()==6&&previous.get("source_marker_count_mismatches").getAsInt()==2,"R29 must remain explicit partial proof, never ten completed sites");
                check(previous.get("prior_twenty_five_sha256").getAsString().equals(receipt.get("prior_twenty_five_sha256").getAsString())&&previous.getAsJsonArray("prior_twenty_five_sites").equals(receipt.getAsJsonArray("prior_twenty_five_sites"))&&previous.getAsJsonObject("prior_twenty_five_source_receipts").equals(receipt.getAsJsonObject("prior_twenty_five_source_receipts")),"R29 prior25 ancestry differs");
                check(previous.get("prior_full_chunk_count").getAsInt()==390&&previous.get("full_chunk_count").getAsInt()==54&&previous.get("expansion_FULL_chunks_including_imported").getAsInt()==54&&previous.get("total_full_chunks_including_prior").getAsInt()==444,"R29 historical NEW/FULL counters differ");
                Map<String,Integer> unfinished=Map.of("jeg:valen_hangars/bamboo_hangar",86,"jeg:valen_hangars/birch_hangar",92,"jeg:valen_hangars/cherry_hangar",99,"jeg:valen_hangars/dark_oak_hangar",46,"jeg:valen_hangars/jungle_hangar",27,"jeg:valen_helipads/desert_helipad",101);
                Map<String,int[]> completed=Map.of("jeg:valen_hangars/plains_hangar",new int[]{7,0},"jeg:valen_hangars/savannah_hangar",new int[]{6,0},"jeg:valen_hangars/taiga_hangar",new int[]{4,0},"jeg:valen_helipads/forest_helipad",new int[]{8,4});
                JsonArray diagnostics=new JsonArray();Set<String> ids=new HashSet<>();int passed=0,totalLoot=0,totalEmpty=0;
                var rows=previous.getAsJsonArray("sites");check(rows.size()==10,"R29 ten searched rows required");
                for(var element:rows){
                    var row=element.getAsJsonObject();String id=row.get("id").getAsString();
                    check(EXPANSION_IDS.contains(id)&&ids.add(id)&&row.get("search_grid_slot").getAsInt()==25+EXPANSION_IDS.stream().sorted().toList().indexOf(id),"R29 ID/stable grid slot differs");
                    if(completed.containsKey(id)){
                        int[] counts=completed.get(id);
                        check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.has("authored_source_contract")&&row.get("placed_cache_count").getAsInt()==counts[0]&&row.get("placed_empty_storage_count").getAsInt()==counts[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Cannot import a failed/unproven native row");
                        sites.add(row.deepCopy());passed++;totalLoot+=counts[0];totalEmpty+=counts[1];
                    }else{
                        check(unfinished.containsKey(id)&&row.get("candidates_checked").getAsInt()==unfinished.get(id),"R29 unfinished actual grid counter differs");
                        boolean failed=id.equals("jeg:valen_hangars/dark_oak_hangar")||id.equals("jeg:valen_hangars/jungle_hangar");
                        check(failed?row.get("status").getAsString().equals("actual native start placed; source marker count mismatch")&&!row.get("native_marker_counts_match_source").getAsBoolean():row.get("status").getAsString().startsWith("pending:"),"R29 unresolved proof status differs");
                        diagnostics.add(row.deepCopy());resumeAfter.put(id,row.get("candidates_checked").getAsInt());
                    }
                }
                check(ids.equals(EXPANSION_IDS)&&passed==4&&resumeAfter.size()==6&&totalLoot==25&&totalEmpty==4&&previous.get("placed_cache_count").getAsInt()==totalLoot&&previous.get("placed_empty_storage_count").getAsInt()==totalEmpty&&previous.get("placed_spawner_count").getAsInt()==0,"R29 completed marker totals differ");
                structures=structures.stream().filter(h->resumeAfter.containsKey(h.key().identifier().toString())).toList();check(structures.size()==6,"Only six unresolved configured IDs may run");
                generated=4;containers=totalLoot;emptyStorage=totalEmpty;spawners=0;importedExpansionChunks=54;priorFullChunks=444;
                receipt.addProperty("resume_prior_native_receipt",path);receipt.addProperty("resume_prior_native_sha256",sha);
                receipt.addProperty("resume_prior_source_contract_sha256","cc177f59949c1c662498419b7d70bbc881bcefe7bbd4f2643376eb6425545e47");
                receipt.add("prior_r29_all_ten_rows",rows.deepCopy());receipt.add("prior_r29_failed_pending_diagnostics",diagnostics);
                receipt.addProperty("prior_r29_marker_mismatches_retained_as_diagnostics",2);
                receipt.addProperty("imported_completed_sites",4);receipt.addProperty("imported_completed_rows_unchanged",true);receipt.addProperty("prior_twenty_five_full_chunk_count",390);receipt.addProperty("prior_full_chunk_count",444);receipt.addProperty("imported_expansion_FULL_chunks",54);
                receipt.addProperty("completed_four_world_loot_or_activation_replayed",false);receipt.addProperty("failed_old_starts_or_unpacked_caches_reread",false);
                receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));receipt.addProperty("max_grid_trials_total_each",384);receipt.addProperty("max_placement_eligible_candidates_this_run",64);
                receipt.addProperty("search_origin","Only six unresolved IDs continue after frozen R29 counters at unchanged slots25..34. Four actual PASS rows imported verbatim; failed old starts, including already unpacked caches, are diagnostics only.384NEW FULL current-run budget; historical444 informational.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing immutable R29 native partial",error);}
        }


        void importAirfieldR30Partial(String path,String sha){
            check(path!=null&&!path.isBlank(),"Next resume requires frozen actual R30 seven-pass native report");
            try{
                check("46fa1ba15891705a3d15932aa06e6156484740d2d01a255b8fa925213b20e967".equals(sha),"Exact final R30 native partial required");
                var previous=boundReceipt(Path.of(path),sha);
                String r29path=previous.get("resume_prior_native_receipt").getAsString(),r29sha=previous.get("resume_prior_native_sha256").getAsString();
                importAirfieldR29Partial(r29path,r29sha);var r29=boundReceipt(Path.of(r29path),r29sha);
                check(previous.get("world_seed").getAsLong()==level.getSeed()&&!previous.get("forced_fake_starts").getAsBoolean()&&!previous.get("production_world_modified").getAsBoolean(),"R30 natural copied-world provenance differs");
                check(previous.get("status").getAsString().equals("partial native generation/readback; inspect pending sites")&&previous.get("registered_structure_count").getAsInt()==10&&previous.get("generated_structure_count").getAsInt()==7&&previous.get("source_marker_count_mismatches").getAsInt()==0,"R30 is seven actual PASS proofs, never ten");
                check(previous.get("prior_twenty_five_sha256").getAsString().equals(receipt.get("prior_twenty_five_sha256").getAsString())&&previous.getAsJsonArray("prior_twenty_five_sites").equals(receipt.getAsJsonArray("prior_twenty_five_sites"))&&previous.getAsJsonObject("prior_twenty_five_source_receipts").equals(receipt.getAsJsonObject("prior_twenty_five_source_receipts")),"R30 actual prior25 ancestry differs");
                check(previous.getAsJsonArray("prior_r29_all_ten_rows").equals(r29.getAsJsonArray("sites"))&&previous.getAsJsonArray("prior_r29_failed_pending_diagnostics").equals(receipt.getAsJsonArray("prior_r29_failed_pending_diagnostics")),"R29 failed/pending diagnostics were not preserved verbatim");
                check(previous.get("prior_full_chunk_count").getAsInt()==444&&previous.get("full_chunk_count").getAsInt()==27&&previous.get("expansion_FULL_chunks_including_imported").getAsInt()==81&&previous.get("total_full_chunks_including_prior").getAsInt()==471,"R30 historical FULL counters differ");
                Map<String,Integer> unfinished=Map.of("jeg:valen_hangars/cherry_hangar",185,"jeg:valen_hangars/jungle_hangar",118,"jeg:valen_helipads/desert_helipad",199);
                Map<String,int[]> completed=Map.of("jeg:valen_hangars/bamboo_hangar",new int[]{11,0},"jeg:valen_hangars/birch_hangar",new int[]{8,0},"jeg:valen_hangars/dark_oak_hangar",new int[]{2,0},"jeg:valen_hangars/plains_hangar",new int[]{7,0},"jeg:valen_hangars/savannah_hangar",new int[]{6,0},"jeg:valen_hangars/taiga_hangar",new int[]{4,0},"jeg:valen_helipads/forest_helipad",new int[]{8,4});
                Map<String,JsonObject> r29passed=new HashMap<>();for(var element:sites){var row=element.getAsJsonObject();r29passed.put(row.get("id").getAsString(),row);}
                sites.asList().clear();resumeAfter.clear();JsonArray diagnostics=new JsonArray();Set<String> ids=new HashSet<>();int count=0,totalLoot=0,totalEmpty=0;
                var rows=previous.getAsJsonArray("sites");check(rows.size()==10,"Exact R30 ten searched rows required");
                for(var element:rows){
                    var row=element.getAsJsonObject();String id=row.get("id").getAsString();check(EXPANSION_IDS.contains(id)&&ids.add(id)&&row.get("search_grid_slot").getAsInt()==25+EXPANSION_IDS.stream().sorted().toList().indexOf(id),"R30 configured ID/stable grid slot differs");
                    if(completed.containsKey(id)){
                        var expected=completed.get(id);check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.has("authored_source_contract")&&row.get("placed_cache_count").getAsInt()==expected[0]&&row.get("placed_empty_storage_count").getAsInt()==expected[1]&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Unproved row cannot be imported");
                        if(r29passed.containsKey(id))check(row.equals(r29passed.get(id)),"Previous four proven rows changed during R30");
                        sites.add(row.deepCopy());count++;totalLoot+=expected[0];totalEmpty+=expected[1];
                    }else{
                        check(unfinished.containsKey(id)&&row.get("status").getAsString().startsWith("pending:")&&row.get("candidates_checked").getAsInt()==unfinished.get(id),"R30 pending actual status/counter differs");
                        diagnostics.add(row.deepCopy());resumeAfter.put(id,row.get("candidates_checked").getAsInt());
                    }
                }
                check(ids.equals(EXPANSION_IDS)&&count==7&&resumeAfter.size()==3&&totalLoot==46&&totalEmpty==4&&previous.get("placed_cache_count").getAsInt()==46&&previous.get("placed_empty_storage_count").getAsInt()==4&&previous.get("placed_spawner_count").getAsInt()==0,"R30 actual PASS totals differ");
                structures=structures.stream().filter(h->resumeAfter.containsKey(h.key().identifier().toString())).toList();check(structures.size()==3,"Only three unresolved IDs may search");
                generated=7;containers=46;emptyStorage=4;spawners=0;contractMismatches=0;importedExpansionChunks=81;priorFullChunks=471;
                receipt.add("prior_r30_all_ten_rows",rows.deepCopy());receipt.add("prior_r30_pending_diagnostics",diagnostics);receipt.addProperty("prior_r30_native_receipt",path);receipt.addProperty("prior_r30_native_sha256",sha);
                receipt.addProperty("resume_prior_native_receipt",path);receipt.addProperty("resume_prior_native_sha256",sha);receipt.addProperty("imported_completed_sites",7);receipt.addProperty("imported_completed_rows_unchanged",true);receipt.addProperty("prior_full_chunk_count",471);receipt.addProperty("imported_expansion_FULL_chunks",81);
                receipt.addProperty("completed_seven_world_loot_or_activation_replayed",false);receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));
                receipt.addProperty("search_origin","Only unresolved cherry185/jungle118/desert199 at unchanged slots27/29/33. Original natural jungle may be revalidated only after six exact untouched tables preread against immutable snapshot; fallback/new search uses locator. Seven actual PASS rows imported verbatim; allR29/R30 failure/pending diagnostics retained.384NEW FULL/run; historical471 informational. Only native biome-locator-assisted actual configured potential chunks are tried; cumulative ordinals are explicitly distinct from legacy anchor grids.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing frozen actual R30 seven-pass partial",error);}
        }

        void beginOriginalJungle()throws java.io.IOException {
            originalJungleRow=null;for(var row:receipt.getAsJsonArray("prior_r29_all_ten_rows"))if(row.getAsJsonObject().get("id").getAsString().equals("jeg:valen_hangars/jungle_hangar"))originalJungleRow=row.getAsJsonObject();
            check(originalJungleRow!=null&&originalJungleRow.get("candidates_checked").getAsInt()==27&&originalJungleRow.get("start_chunk_x").getAsInt()==22536&&originalJungleRow.get("start_chunk_z").getAsInt()==36617,"Bound original natural jungle row missing");
            Path snapshotPath=Path.of(System.getProperty("valenjeg.evidence.3d890dbc52d5", "VALENJEG_EVIDENCE_NOT_CONFIGURED/saved-native-container-readback.json"));String snapshotSha="69461e356b789895e80e4e3d55a072fedad833a3f0634d6e66d7a79a7c35edf9";var snapshot=boundReceipt(snapshotPath,snapshotSha);
            check(snapshot.get("probe_snapshot_sha256").getAsString().equals("cfe49fd921de4096489a8c562672dc52f1a68a1a5296ee34824b19fac0011229"),"Jungle saved-table snapshot is not bound to original R29");jungleSavedTables=new JsonArray();
            for(var element:snapshot.getAsJsonArray("sites")){var old=element.getAsJsonObject();if(!old.get("id").getAsString().equals("jeg:valen_hangars/jungle_hangar"))continue;
                for(var c:old.getAsJsonArray("containers")){var cell=c.getAsJsonObject();if(!cell.has("actual_saved_BE")||cell.get("actual_saved_BE").isJsonNull())continue;var be=cell.getAsJsonObject("actual_saved_BE");String table=cell.getAsJsonObject("source_nbt").get("LootTable").getAsString();check(be.has("LootTable")&&be.get("LootTable").getAsString().equals(table)&&!be.has("Items"),"Original jungle snapshot cache was already consumed");JsonObject proof=new JsonObject();proof.add("pos",cell.getAsJsonArray("world_pos").deepCopy());proof.addProperty("table",table);jungleSavedTables.add(proof);}
            }
            check(jungleSavedTables.size()==6,"Six original untouched table snapshots required");site.addProperty("historical_jungle_snapshot",snapshotPath.toString());site.addProperty("historical_jungle_snapshot_sha256",snapshotSha);site.add("original_failed_natural_row",originalJungleRow.deepCopy());
            var startChunk=level.getChunkSource().getChunk(22536,36617,ChunkStatus.STRUCTURE_STARTS,true);check(startChunk!=null,"Historical jungle start chunk unavailable");nativeStart=startChunk.getStartForStructure(structure);check(nativeStart!=null&&nativeStart.isValid(),"Original natural jungle start unavailable");bounds=nativeStart.getBoundingBox();check(box(bounds).equals(originalJungleRow.getAsJsonArray("bounding_box")),"Original natural jungle bounds changed");pieceBounds=nativeStart.getPieces().stream().map(StructurePiece::getBoundingBox).toList();chunks=new ArrayList<>();for(int x=bounds.minX()>>4;x<=bounds.maxX()>>4;x++)for(int z=bounds.minZ()>>4;z<=bounds.maxZ()>>4;z++)chunks.add(new ChunkPos(x,z));check(chunks.size()==9,"Exact original nine already-FULL jungle chunks required");chunkIndex=0;revalidatingJungle=true;site.addProperty("candidates_checked",startAttempt);site.addProperty("placement_eligible_candidates",0);site.addProperty("new_structure_generation",false);site.addProperty("historical_natural_start_revalidation_attempted",true);receipt.addProperty("failed_old_starts_or_unpacked_caches_reread",true);receipt.addProperty("previously_unpacked_inventory_contents_replayed",false);phase="historical_jungle";
        }
        void readOriginalJungle(){
            if(chunkIndex<chunks.size()){var cp=chunks.get(chunkIndex++);level.getChunk(cp.x(),cp.z());historicalReadChunks.add(ChunkPos.pack(cp.x(),cp.z()));return;}
            JsonObject preread;
            try{
                preread=HangarHelipadContracts.requireUntouchedJungle(level,nativeStart,originalJungleRow);var actual=preread.getAsJsonArray("all_six_tables_preread_before_any_unpack");check(actual.size()==6,"Six actual source tables required");for(var element:jungleSavedTables){var old=element.getAsJsonObject();check(actual.asList().stream().anyMatch(v->{var now=v.getAsJsonObject();return now.get("pos").equals(old.get("pos"))&&now.get("table").equals(old.get("table"));}),"Actual untouched jungle cache differs from bound saved table snapshot");}
            }catch(AssertionError failure){site.addProperty("historical_jungle_preread_status","failed before any loot unpack; normal new-candidate locator search follows");site.addProperty("historical_jungle_preread_error",failure.toString());revalidatingJungle=false;site.remove("new_structure_generation");phase="candidate";save(receipt);return;}
            site.add("historical_jungle_all_six_preread",preread);site.addProperty("historical_jungle_preread_status","all six exact source/cache tables untouched before any unpack");site.add("bounding_box",box(bounds));site.add("pieces",originalJungleRow.getAsJsonArray("pieces").deepCopy());site.addProperty("piece_count",2);site.addProperty("start_chunk_x",22536);site.addProperty("start_chunk_z",36617);site.addProperty("unadapted_center_surface_y",originalJungleRow.get("unadapted_center_surface_y").getAsInt());site.addProperty("bbox_min_relative_to_center_surface",originalJungleRow.get("bbox_min_relative_to_center_surface").getAsInt());site.add("loot_containers",new JsonArray());site.add("spawners",new JsonArray());chunkIndex=0;phase="read";save(receipt);
        }


        void importR39CherryPass()throws java.io.IOException {
            Path path=Path.of(System.getProperty("valenjeg.evidence.4fe698e05bdd", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-cherry-fresh-trace.json"));
            var prior=boundReceipt(path,"9795d03f02487cfd61fed17e24ee607584da5849848a7a2ea32b14f9ea2c0792");
            check(prior.get("world_seed").getAsLong()==level.getSeed()&&prior.get("source_marker_count_mismatches").getAsInt()==0&&prior.get("generated_structure_count").getAsInt()==9,"R39 cherry pass provenance differs");
            JsonObject cherry=null;for(var element:prior.getAsJsonArray("sites")){var row=element.getAsJsonObject();if(row.get("id").getAsString().equals("jeg:valen_hangars/cherry_hangar"))cherry=row;}
            check(cherry!=null&&cherry.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&cherry.get("native_marker_counts_match_source").getAsBoolean()&&cherry.get("candidates_checked").getAsInt()==228&&cherry.get("start_chunk_x").getAsInt()==13677&&cherry.get("start_chunk_z").getAsInt()==22433&&cherry.get("placed_cache_count").getAsInt()==8,"R39 cherry pass row differs");
            var pieces=cherry.getAsJsonArray("pieces");check(pieces.size()==2&&pieces.get(0).getAsJsonObject().get("template_id").getAsString().equals("jeg:valen_hangars/cherry_hangar/frame/wall1")&&pieces.get(1).getAsJsonObject().get("template_id").getAsString().equals("jeg:valen_hangars/cherry_hangar/frame/inner2"),"R39 cherry pieces differ");
            sites.add(cherry.deepCopy());resumeAfter.put("jeg:valen_hangars/cherry_hangar",228);generated=9;containers=60;
            receipt.add("r39_cherry_pass_row",cherry.deepCopy());receipt.addProperty("r39_cherry_pass_imported_verbatim",true);receipt.addProperty("r39_cherry_loot_replayed",false);receipt.addProperty("r39_passed_site_replayed",false);receipt.addProperty("inner1_search_resume_ordinal",228);
            if(CHERRY_INNER1_CONTINUE)importR43Inner1Exhaustion();
        }
        void importR43Inner1Exhaustion()throws java.io.IOException {
            Path path=Path.of(System.getProperty("valenjeg.evidence.4cf3fa295681", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-cherry-inner1-trace.json"));
            var prior=boundReceipt(path,"479eede5c898b88f1afcc7307b92849aa464ff7bd76d8f9b0c975487b4bd029f");
            check(prior.get("world_seed").getAsLong()==level.getSeed()&&prior.get("new_FULL_chunks_this_run").getAsInt()==0&&prior.get("searched_structure_count_this_run").getAsInt()==1&&!prior.get("forced_fake_starts").getAsBoolean()&&!prior.get("production_world_modified").getAsBoolean(),"R43 inner1 exhaustion provenance differs");
            JsonObject pending=null;for(var element:prior.getAsJsonArray("sites")){var row=element.getAsJsonObject();if(row.get("id").getAsString().equals("jeg:valen_hangars/cherry_hangar")&&row.get("status").getAsString().startsWith("pending:")&&row.get("candidates_checked").getAsInt()==278)pending=row;}
            check(pending!=null&&pending.get("resume_after_grid_attempt").getAsInt()==228&&pending.get("search_grid_slot").getAsInt()==27&&pending.get("placement_eligible_candidates").getAsInt()==31,"R43 inner1 pending row differs");
            var queries=pending.getAsJsonArray("native_biome_locator_queries");check(queries.size()==4,"R43 locator query count differs");
            var third=queries.get(2).getAsJsonObject();var fourth=queries.get(3).getAsJsonObject();
            check(third.get("anchor").getAsInt()==3&&third.get("source_legacy_grid_attempt").getAsInt()==231&&third.get("result").getAsString().equals("native_biome_not_found_in_bound")&&third.get("origin_x").getAsInt()==1179832&&third.get("origin_y").getAsInt()==64&&third.get("origin_z").getAsInt()==2232520,"R43 grid 231 query differs");
            check(fourth.get("anchor").getAsInt()==4&&fourth.get("source_legacy_grid_attempt").getAsInt()==232&&fourth.get("result").getAsString().equals("native_biome_not_found_in_bound")&&fourth.get("origin_x").getAsInt()==1183896&&fourth.get("origin_y").getAsInt()==64&&fourth.get("origin_z").getAsInt()==2240568,"R43 grid 232 query differs");
            check(pending.getAsJsonArray("locator_steps").get(pending.getAsJsonArray("locator_steps").size()-1).getAsJsonObject().get("result").getAsString().equals("bounded_native_locator_origins_exhausted"),"R43 exhaustion step differs");
            JsonArray preserved=new JsonArray();int ordinal=228;for(var element:pending.getAsJsonArray("candidate_diagnostics")){var row=element.getAsJsonObject();check(row.get("candidate_ordinal").getAsInt()==++ordinal&&!row.get("result").getAsString().equals("actual_native_start_valid"),"R43 checked chunk ordinal differs");inner1CheckedChunks.add(ChunkPos.pack(row.get("chunk_x").getAsInt(),row.get("chunk_z").getAsInt()));JsonObject kept=new JsonObject();kept.addProperty("candidate_ordinal",ordinal);kept.addProperty("chunk_x",row.get("chunk_x").getAsInt());kept.addProperty("chunk_z",row.get("chunk_z").getAsInt());preserved.add(kept);}
            check(ordinal==278&&inner1CheckedChunks.size()==50,"R43 preserved chunk count differs");inner1NextGrid=233;resumeAfter.put("jeg:valen_hangars/cherry_hangar",278);
            receipt.add("r43_preserved_checked_chunks",preserved);receipt.addProperty("r43_inner1_receipt_sha256","479eede5c898b88f1afcc7307b92849aa464ff7bd76d8f9b0c975487b4bd029f");receipt.addProperty("inner1_search_resume_ordinal",278);receipt.addProperty("configured_grid_continuation_start",233);receipt.addProperty("recorded_origin_spirals_repeated",false);receipt.addProperty("r43_desert_complex_box_preserved",true);receipt.addProperty("old_loot_replayed",false);
        }
        void addRecordedOrigin(JsonArray rows,String sourceSha,String sourceId,int grid,int cx,int cz,String biome,int exactCount,String target,JsonArray evidence){
            JsonObject source=null;for(var element:rows){var row=element.getAsJsonObject();if(row.get("id").getAsString().equals(sourceId))source=row;}
            check(source!=null,"Recorded biome source row missing");JsonObject trial=null;for(var element:source.getAsJsonArray("candidate_diagnostics")){var row=element.getAsJsonObject();if(row.has("grid_attempt")&&row.get("grid_attempt").getAsInt()==grid)trial=row;}
            check(trial!=null&&trial.get("chunk_x").getAsInt()==cx&&trial.get("chunk_z").getAsInt()==cz&&trial.getAsJsonObject("native_biome_checks").get(biome+" rejected").getAsInt()==exactCount,"Exact immutable native biome observation changed");
            recordedBiomeOrigins.computeIfAbsent(target,k->new ArrayList<>()).add(new BlockPos(cx*16+8,64,cz*16+8));JsonObject binding=new JsonObject();binding.addProperty("target",target);binding.addProperty("source_receipt_sha256",sourceSha);binding.addProperty("source_structure",sourceId);binding.addProperty("source_grid_attempt",grid);binding.addProperty("chunk_x",cx);binding.addProperty("chunk_z",cz);binding.addProperty("actual_observed_biome",biome);binding.addProperty("actual_observed_biome_count",exactCount);binding.addProperty("locator_y","Native WORLD_SURFACE_WG center projection at recordedXY; original callbackY was not logged");evidence.add(binding);
        }
        void importAirfieldPartial(){
            if(!HANGAR_HELIPAD_ANCHORS){importAirfieldR30Partial(System.getProperty("valen.structure.probe.hangarHelipadResumePrior"),System.getProperty("valen.structure.probe.hangarHelipadResumePriorSha"));return;}
            String path=System.getProperty("valen.structure.probe.hangarHelipadAnchorsPrior"),sha=System.getProperty("valen.structure.probe.hangarHelipadAnchorsPriorSha");
            try{
                check(path!=null&&"bdbd97c3ec399c97aed4e17c98d01f6dbea09b44766d0d3eb5091382e4b64605".equals(sha),"Exact immutable R31 eight-pass partial required");var previous=boundReceipt(Path.of(path),sha);String r30path=previous.get("prior_r30_native_receipt").getAsString(),r30sha=previous.get("prior_r30_native_sha256").getAsString();importAirfieldR30Partial(r30path,r30sha);var r30=boundReceipt(Path.of(r30path),r30sha);
                check(previous.get("world_seed").getAsLong()==level.getSeed()&&!previous.get("forced_fake_starts").getAsBoolean()&&!previous.get("production_world_modified").getAsBoolean(),"R31 actual copied-world provenance differs");check(previous.get("generated_structure_count").getAsInt()==8&&previous.get("registered_structure_count").getAsInt()==10&&previous.get("source_marker_count_mismatches").getAsInt()==0&&previous.get("status").getAsString().startsWith("partial native"),"Eight actual PASSs plus two pending required");
                check(previous.get("full_chunk_count").getAsInt()==0&&previous.get("total_full_chunks_including_prior").getAsInt()==471&&previous.get("expansion_FULL_chunks_including_imported").getAsInt()==81&&previous.get("historical_natural_starts_revalidated").getAsInt()==1&&previous.get("historical_FULL_chunks_reloaded").getAsInt()==9,"R31 historical/new/revalidated counters differ");check(previous.getAsJsonArray("prior_r30_all_ten_rows").equals(r30.getAsJsonArray("sites"))&&previous.getAsJsonArray("prior_r29_all_ten_rows").equals(receipt.getAsJsonArray("prior_r29_all_ten_rows")),"Recorded-origin ancestry differs");
                Map<String,JsonObject> passed7=new HashMap<>();for(var element:sites){var row=element.getAsJsonObject();passed7.put(row.get("id").getAsString(),row);}sites.asList().clear();resumeAfter.clear();JsonArray pending=new JsonArray();Set<String> ids=new HashSet<>();int passed=0,loot=0,empty=0;
                for(var element:previous.getAsJsonArray("sites")){var row=element.getAsJsonObject();String id=row.get("id").getAsString();check(EXPANSION_IDS.contains(id)&&ids.add(id),"R31 duplicate/unexpected ID");
                    if(id.equals("jeg:valen_hangars/cherry_hangar")||id.equals("jeg:valen_helipads/desert_helipad")){int count=id.contains("cherry")?185:224;check(row.get("status").getAsString().startsWith("pending:")&&row.get("candidates_checked").getAsInt()==count,"Actual pending cumulative ordinal differs");pending.add(row.deepCopy());resumeAfter.put(id,count);}
                    else{check(row.get("status").getAsString().equals("actual native start and full-chunk placement read back")&&row.get("native_marker_counts_match_source").getAsBoolean()&&row.has("authored_source_contract")&&row.get("placed_ghoul_spawner_count").getAsInt()==0,"Cannot import unproven R31 row");if(passed7.containsKey(id))check(row.equals(passed7.get(id)),"Prior seven PASS rows changed");else check(id.equals("jeg:valen_hangars/jungle_hangar")&&row.get("historical_natural_start_revalidated").getAsBoolean()&&row.getAsJsonObject("historical_jungle_all_six_preread").getAsJsonArray("all_six_tables_preread_before_any_unpack").size()==6&&row.get("placed_cache_count").getAsInt()==6,"Original jungle six-cache revalidation proof missing");sites.add(row.deepCopy());passed++;loot+=row.get("placed_cache_count").getAsInt();empty+=row.get("placed_empty_storage_count").getAsInt();}
                }
                check(ids.equals(EXPANSION_IDS)&&passed==8&&loot==52&&empty==4&&resumeAfter.size()==2,"Exact eight actual PASS totals differ");structures=structures.stream().filter(h->resumeAfter.containsKey(h.key().identifier().toString())).toList();check(structures.size()==2,"Only cherry/desert may search");if(CHERRY_FRESH){structures=structures.stream().filter(h->h.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar")).toList();check(structures.size()==1,"Fresh trace searches only configured cherry");receipt.addProperty("cherry_fresh_trace",true);receipt.addProperty("desert_search_deferred",true);receipt.addProperty("r32_failed_cherry_not_replayed",true);}generated=8;containers=52;emptyStorage=4;spawners=0;contractMismatches=0;priorFullChunks=471;importedExpansionChunks=81;if(CHERRY_INNER1)importR39CherryPass();
                JsonArray anchors=new JsonArray();var r29rows=receipt.getAsJsonArray("prior_r29_all_ten_rows");var r30rows=r30.getAsJsonArray("sites");String r29sha="cfe49fd921de4096489a8c562672dc52f1a68a1a5296ee34824b19fac0011229";
                addRecordedOrigin(r29rows,r29sha,"jeg:valen_hangars/bamboo_hangar",57,28165,48907,"minecraft:cherry_grove",54,"jeg:valen_hangars/cherry_hangar",anchors);
                addRecordedOrigin(previous.getAsJsonArray("prior_eighteen_sites"),r29sha,"jeg:valen/oil_rig",19,13759,22414,"minecraft:cherry_grove",1,"jeg:valen_hangars/cherry_hangar",anchors);
                addRecordedOrigin(r29rows,r29sha,"jeg:valen_hangars/plains_hangar",3,16901,25099,"minecraft:desert",77,"jeg:valen_helipads/desert_helipad",anchors);
                addRecordedOrigin(r30rows,r30sha,"jeg:valen_hangars/birch_hangar",106,41216,74762,"minecraft:desert",74,"jeg:valen_helipads/desert_helipad",anchors);
                addRecordedOrigin(r30rows,r30sha,"jeg:valen_hangars/cherry_hangar",167,57352,106755,"minecraft:desert",69,"jeg:valen_helipads/desert_helipad",anchors);
                check(recordedBiomeOrigins.get("jeg:valen_hangars/cherry_hangar").size()==2&&recordedBiomeOrigins.get("jeg:valen_helipads/desert_helipad").size()==3,"Only recorded finite origins allowed");receipt.add("immutable_native_biome_observation_origins",anchors);receipt.add("prior_r31_all_ten_rows",previous.getAsJsonArray("sites").deepCopy());receipt.add("prior_r31_pending_diagnostics",pending);receipt.addProperty("prior_r31_native_receipt",path);receipt.addProperty("prior_r31_native_sha256",sha);receipt.addProperty("imported_completed_sites",8);receipt.addProperty("imported_completed_rows_unchanged",true);receipt.addProperty("completed_eight_world_loot_or_activation_replayed",false);receipt.add("resume_after_grid_trials",GSON.toJsonTree(resumeAfter));receipt.addProperty("search_origin","Only cherry185/desert224: immutable actually observed exact-biome XY origins, native surface-height locator seed, then unchanged native configured-placement candidates. Eight actual PASS rows imported verbatim; no jungle/loot reread; same all caps.");
            }catch(java.io.IOException error){throw new IllegalStateException("Missing exact R31 eight-pass source evidence",error);}
        }

        void nextSite(String status){site.addProperty("status",status);index++;phase="select";save(receipt);}
        void step(){
            try {
                check(++ticks<=18000,"Incremental structure probe exceeded18000server ticks");
                switch(phase){
                    case "select"->{
                        if(index==structures.size()){finish();return;}
                        holder=structures.get(index);structure=holder.value();site=new JsonObject();sites.add(site);site.addProperty("id",holder.key().identifier().toString());site.addProperty("terrain_adaptation",structure.terrainAdaptation().toString());if(EXPANSION){List<String> allowed=new ArrayList<>();for(var biome:structure.biomes())allowed.add(biome.unwrapKey().map(k->k.identifier().toString()).orElse("unkeyed"));Collections.sort(allowed);site.add("actual_registered_allowed_biomes",GSON.toJsonTree(allowed));site.addProperty("actual_registered_allowed_biome_count",allowed.size());}
                        check(!level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).isEmpty(),"No configured structure-set placement for "+holder.key());if(EXPANSION)check(level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).size()==1,"Expansion expects exactly one configured placement");
                        startAttempt=HANGAR_HELIPAD_RESUME||SCORCHED_RESUME||HBM_NEXT_RESUME||EXPANSION_RESUME||INDUSTRIAL_RESUME?resumeAfter.get(holder.key().identifier().toString()):EXPANSION||INDUSTRIAL||DESERT||BUNKER?0:RESUME?resumeAfter.get(holder.key().identifier().toString()):EXTENSION?24:0;attempt=startAttempt;eligibleTrials=0;site.addProperty("candidates_checked",attempt);site.addProperty("placement_eligible_candidates",0);siteContainers=siteSpawners=siteEmptyStorage=0;if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER){site.add("candidate_diagnostics",new JsonArray());site.addProperty(EXPANSION||INDUSTRIAL||DESERT||BUNKER?"search_grid_slot":"original_registry_index",originalIndices.get(holder));site.addProperty("resume_after_grid_attempt",startAttempt);}if(HANGAR_HELIPAD_RESUME){airfieldSearch=HANGAR_HELIPAD_ANCHORS?new AirfieldNativeSearch(level,holder,originalIndices.get(holder),startAttempt,site,Objects.requireNonNull(recordedBiomeOrigins.get(holder.key().identifier().toString()))):new AirfieldNativeSearch(level,holder,originalIndices.get(holder),startAttempt,site);site.addProperty("candidate_counter_semantics","Cumulative candidate ordinal continues actual R30 counter; locator anchor legacy grids recorded separately, ordinal is not a legacy-coordinate formula");if(CHERRY_INNER1_CONTINUE&&holder.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar")){check(inner1NextGrid==233&&inner1CheckedChunks.size()==50,"R43 checked-chunk continuation differs");airfieldSearch.continueFromConfiguredGrid(inner1NextGrid,inner1CheckedChunks);site.addProperty("configured_grid_continuation_start",inner1NextGrid);site.addProperty("already_checked_chunks_preserved",inner1CheckedChunks.size());site.addProperty("recorded_origin_spirals_repeated",false);}}if(DESERT_LOCATOR){desertSearch=new AirfieldNativeSearch(level,holder,38,67,site,Objects.requireNonNull(recordedBiomeOrigins.get(holder.key().identifier().toString())),4);site.addProperty("candidate_counter_semantics","This run counts locator candidates from zero. Immutable actually observed desert XY origins seed findClosestBiome3d; no repeated R39 structure chunks.");}if(MINER){
                            var prior=boundReceipt(Path.of(System.getProperty("valenjeg.evidence.a582b8dac416", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-structure-probe-miner-outpost.json")),"03beaab433f56769ce1dd887a43a0ce52f3fed0101eb1037c1bc6c4147ea5d8b");
                            check(prior.get("world_seed").getAsLong()==level.getSeed()&&prior.get("generated_structure_count").getAsInt()==0&&prior.get("full_chunk_count").getAsInt()==0,"Prior bounded mining search identity differs");
                            minerSearch=new AirfieldNativeSearch(level,holder,40,64,site,List.of(new BlockPos(340064,120,599968)),4);
                            site.addProperty("candidate_counter_semantics","New native biome-assisted candidates from zero; previous64linear biome rejections preserved, not replayed");
                            site.addProperty("native_mountain_locator","Native console located #minecraft:is_mountain biomesoplenty:crag at340064,120,599968; original biome rules retained");
                        }revalidatingJungle=false;phase="candidate";if(HANGAR_HELIPAD_RESUME&&holder.key().identifier().toString().equals("jeg:valen_hangars/jungle_hangar"))beginOriginalJungle();
                    }
                    case "historical_jungle"->readOriginalJungle();
                    case "candidate"->findCandidate();
                    case "place"->{
                        if(chunkIndex==chunks.size()){chunkIndex=0;phase="read";return;}
                        ChunkPos cp=chunks.get(chunkIndex++);level.getChunk(cp.x(),cp.z());fullChunks.add(ChunkPos.pack(cp.x(),cp.z()));
                    }
                    case "read"->{
                        if(chunkIndex==chunks.size()){
                            if(MINER && minerEntityTicks++<60){
                                if(minerEntityTicks==1)for(ChunkPos cp:chunks)if(!level.getForceLoadedChunks().contains(cp.pack())){check(level.setChunkForced(cp.x(),cp.z(),true),"Could not request temporary mining entity ticking");minerTickets.add(cp.pack());}
                                return;
                            }
                            site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_ghoul_spawner_count",siteSpawners);
                            boolean matches=true;if(BUNKER){
                                try{var evidence=BunkerRoomsContracts.readPlaced(level,nativeStart,holder.key().identifier().toString());siteContainers=evidence.get("packed_loot").getAsInt();siteSpawners=0;containers+=siteContainers;site.add("authored_source_contract",evidence);site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_ghoul_spawner_count",0);site.addProperty("native_marker_counts_match_source",true);site.addProperty("placement_equivalence_claimed",false);site.addProperty("old_inventory_replayed",false);}
                                catch(AssertionError failure){matches=false;contractMismatches++;site.addProperty("native_marker_counts_match_source",false);site.addProperty("authored_source_contract_error",failure.toString());}
                                terrainReadback();
                            }else if(DESERT){
                                try{var evidence=DesertComplexContracts.readPlaced(level,nativeStart,holder.key().identifier().toString());siteContainers=evidence.get("placed_loot").getAsInt();siteSpawners=0;containers+=siteContainers;site.add("authored_source_contract",evidence);site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_ghoul_spawner_count",0);site.addProperty("native_marker_counts_match_source",true);site.addProperty("placement_equivalence_claimed",false);}
                                catch(AssertionError failure){matches=false;contractMismatches++;site.addProperty("native_marker_counts_match_source",false);site.addProperty("authored_source_contract_error",failure.toString());}
                                terrainReadback();
                            }else if(INDUSTRIAL){
                                try{var evidence=IndustrialSourceContracts.readPlaced(level,nativeStart,holder.key().identifier().toString());siteContainers=evidence.get("placed_loot").getAsInt();siteEmptyStorage=evidence.get("placed_empty_minecraft_native_containers").getAsInt();siteSpawners=evidence.getAsJsonObject("spawners").entrySet().stream().mapToInt(e->e.getValue().getAsInt()).sum();containers+=siteContainers;emptyStorage+=siteEmptyStorage;spawners+=siteSpawners;site.add("authored_source_contract",evidence);site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_empty_storage_count",siteEmptyStorage);site.addProperty("placed_empty_create_native_containers",evidence.get("placed_empty_create_native_containers").getAsInt());site.addProperty("placed_spawner_count",siteSpawners);site.addProperty("native_marker_counts_match_source",true);}
                                catch(AssertionError failure){matches=false;contractMismatches++;site.addProperty("native_marker_counts_match_source",false);site.addProperty("authored_source_contract_error",failure.toString());}
                                terrainReadback();
                            }else if(MINER){
                                try{var evidence=MinerOutpostContracts.readPlaced(level,nativeStart,holder.key().identifier().toString());siteContainers=evidence.get("placed_loot").getAsInt();siteSpawners=0;containers+=siteContainers;site.add("authored_source_contract",evidence);site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_ghoul_spawner_count",0);site.addProperty("native_marker_counts_match_source",true);site.addProperty("placement_equivalence_claimed",false);site.addProperty("old_inventory_replayed",false);site.add("scene_readback",evidence.get("scene_readback"));}
                                catch(AssertionError failure){matches=false;contractMismatches++;site.addProperty("native_marker_counts_match_source",false);site.addProperty("authored_source_contract_error",failure.toString());}
                                finally{for(long packed:minerTickets){ChunkPos cp=ChunkPos.unpack(packed);check(level.setChunkForced(cp.x(),cp.z(),false),"Mining temporary ticket removal failed");}site.addProperty("temporary_entity_ticking_chunks",minerTickets.size());site.addProperty("temporary_tickets_removed",true);minerTickets.clear();}
                                terrainReadback();
                            }else if(HANGAR_HELIPAD){
                                if(CHERRY_FRESH&&holder.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar"))site.add("cherry_fresh_trace",CherryFreshTrace.finish(level));
                                try{var result=HangarHelipadContracts.validate(level,nativeStart,holder.key().identifier().toString());siteContainers=result.loot();siteEmptyStorage=result.emptyBarrels();siteSpawners=0;containers+=siteContainers;emptyStorage+=siteEmptyStorage;lootKeys.addAll(result.lootKeys());site.add("loot_containers",result.lootRows());site.add("authored_source_contract",result.evidence());site.addProperty("placed_cache_count",siteContainers);site.addProperty("placed_empty_storage_count",siteEmptyStorage);site.addProperty("placed_ghoul_spawner_count",0);site.addProperty("native_marker_counts_match_source",true);}
                                catch(AssertionError failure){matches=false;contractMismatches++;site.addProperty("native_marker_counts_match_source",false);site.addProperty("authored_source_contract_error",failure.toString());}
                                terrainReadback();
                            }else if(EXPANSION){site.addProperty("placed_empty_storage_count",siteEmptyStorage);int[] expected=EXPANSION_COUNTS.get(holder.key().identifier().toString());
                                site.add("source_expected_cache_empty_spawner_counts",GSON.toJsonTree(expected));matches=siteContainers==expected[0]&&siteEmptyStorage==expected[1]&&siteSpawners==expected[2];site.addProperty("native_marker_counts_match_source",matches);if(!matches)contractMismatches++;
                                terrainReadback();}
                            if(revalidatingJungle&&matches){revalidatedNaturalStarts++;site.addProperty("historical_natural_start_revalidated",true);site.addProperty("new_structure_generation",false);}generated++;nextSite(matches?"actual native start and full-chunk placement read back":"actual native start placed; source marker count mismatch");return;
                        }
                        ChunkPos cp=chunks.get(chunkIndex++);readChunk(level.getChunk(cp.x(),cp.z()));
                    }
                }
            }catch(Throwable failure){done=true;receipt.addProperty("status","fail");receipt.addProperty("error",failure.toString());if(CherryFreshTrace.armed())receipt.add("cherry_fresh_trace",CherryFreshTrace.finish(level));failure.printStackTrace();save(receipt);}
        }
        void findCandidate() throws java.io.IOException{
            if(RETRY_KNOWN&&knownIndex>=knownCandidates.size()){nextSite("pending: known current-configured candidates exhausted");return;}
            if(!RETRY_KNOWN&&(((EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER)&&(attempt>=(HANGAR_HELIPAD_RESUME||SCORCHED_RESUME||HBM_NEXT_RESUME||INDUSTRIAL_RESUME?384:startAttempt+384)||eligibleTrials>=64))||(!(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER)&&attempt>=maxCandidates))){nextSite("pending: no eligible native start within bounded candidate search");return;}
            ChunkPos located=null;
            if(MINER){
                if(attempt>0){nextSite("pending: exact native-located mining start did not qualify");return;}
                var nativeLocated=level.getChunkSource().getGenerator().findNearestMapStructure(level,HolderSet.direct(holder),new BlockPos(365000,120,610000),100,false);
                check(nativeLocated!=null,"Native mining locate returned no candidate");
                located=ChunkPos.containing(nativeLocated.getFirst());
                site.addProperty("candidate_source","Native findNearestMapStructure with actual configured structure holder; no forced starts");
                site.add("native_locator_origin",GSON.toJsonTree(new int[]{365000,120,610000}));
                site.addProperty("native_locator_chunk_x",located.x());site.addProperty("native_locator_chunk_z",located.z());
            }else if(DESERT_LOCATOR){
                var next=desertSearch.next();if(next.diagnostic()!=null){if(!site.has("locator_steps"))site.add("locator_steps",new JsonArray());site.getAsJsonArray("locator_steps").add(next.diagnostic().deepCopy());}
                if(next.done()){nextSite("pending: native biome-locator candidate bounds exhausted");return;}
                if(next.chunk()==null){save(receipt);return;}located=next.chunk();
            }else if(HANGAR_HELIPAD_RESUME){
                var next=airfieldSearch.next();if(next.diagnostic()!=null){if(!site.has("locator_steps"))site.add("locator_steps",new JsonArray());site.getAsJsonArray("locator_steps").add(next.diagnostic().deepCopy());}
                if(next.done()){nextSite("pending: native biome-locator candidate bounds exhausted");return;}
                if(next.chunk()==null){save(receipt);return;}located=next.chunk();
            }
            var source=level.getChunkSource();var state=source.getGeneratorState();JsonObject known=RETRY_KNOWN?knownCandidates.get(knownIndex++):null;int gridAttempt=RETRY_KNOWN?known.get("grid_attempt").getAsInt():++attempt;site.addProperty("candidates_checked",attempt);if(RETRY_KNOWN){site.addProperty("known_candidates_checked_this_run",knownIndex);site.addProperty("linear_search_trials_previously_checked",startAttempt);}
            for(var placement:state.getPlacementsForStructure(holder)){
                if(!(placement instanceof RandomSpreadStructurePlacement spread))continue;
                int gridIndex=originalIndices.get(holder);
                ChunkPos cp=located!=null?located:spread.getPotentialStructureChunk(level.getSeed(),1024+gridIndex*512+(gridAttempt-1)*256,1024+gridIndex*768+(gridAttempt-1)*512);
                if(HANGAR_HELIPAD_RESUME||DESERT_LOCATOR||MINER)check(spread.getPotentialStructureChunk(level.getSeed(),cp.x(),cp.z()).equals(cp),"Locator candidate is not an actual configured random-spread potential chunk");
                JsonObject trial=new JsonObject();if(RETRY_KNOWN){check(cp.x()==known.get("chunk_x").getAsInt()&&cp.z()==known.get("chunk_z").getAsInt(),"Current placement differs from known native configured candidate");trial.addProperty("known_candidate_revisited_after_tag_fix",true);trial.addProperty("known_retry_index",knownIndex);trial.add("prior_rejection_context",known.deepCopy());}
                if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER){if(HANGAR_HELIPAD_RESUME){trial.addProperty("candidate_ordinal",gridAttempt);trial.addProperty("legacy_grid_coordinates_used",false);}else if(DESERT_LOCATOR){trial.addProperty("this_run_candidate",gridAttempt);trial.addProperty("legacy_grid_coordinates_used",false);trial.addProperty("r39_linear_attempt_repeated",false);}else trial.addProperty("grid_attempt",gridAttempt);trial.addProperty("chunk_x",cp.x());trial.addProperty("chunk_z",cp.z());site.getAsJsonArray("candidate_diagnostics").add(trial);if(INDUSTRIAL_RESUME&&reservedIndustrialChunk(cp.x(),cp.z())){trial.addProperty("result","existing_start_not_revisited");trial.addProperty("world_region_replayed",false);save(receipt);continue;}if(DESERT_LOCATOR&&(r39Chunks.contains(ChunkPos.pack(cp.x(),cp.z()))||protectedHistoricalChunk(cp.x(),cp.z())||cp.x()>=28163&&cp.x()<=28165&&cp.z()>=48905&&cp.z()<=48907)){trial.addProperty("result",r39Chunks.contains(ChunkPos.pack(cp.x(),cp.z()))?"r39_generated_chunk_not_repeated":"r32_cherry_nine_chunks_not_loaded");trial.addProperty("chunk_loaded",false);save(receipt);continue;}if(BUNKER&&BunkerRoomsContracts.reservedChunk(cp.x(),cp.z())){trial.addProperty("result","reserved_historical_chunk_not_loaded");trial.addProperty("chunk_loaded",false);trial.addProperty("old_inventory_replayed",false);save(receipt);continue;}if(MINER&&MinerOutpostContracts.reservedChunk(cp.x(),cp.z())){trial.addProperty("result","reserved_historical_chunk_not_loaded");trial.addProperty("chunk_loaded",false);trial.addProperty("old_inventory_replayed",false);save(receipt);continue;}}
                if(!placement.isStructureChunk(state,cp.x(),cp.z())){
                    if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER){String reason=!spread.applyAdditionalChunkRestrictions(cp.x(),cp.z(),level.getSeed())?"native_frequency_reject":!spread.applyInteractionsWithOtherStructures(state,cp.x(),cp.z())?"native_exclusion_reject":"native_other_placement_reject";trial.addProperty("result",reason);save(receipt);}continue;
                }
                eligibleTrials++;if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER){site.addProperty("placement_eligible_candidates",eligibleTrials);
                    Map<String,Integer> biomeChecks=new TreeMap<>();int[] accepted={0};
                    var generator=source.getGenerator();
                    var context=new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),source.randomState().createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build()),source.randomState(),level.getStructureTemplateManager(),level.getSeed(),cp,level,b->{
                        boolean allowed=structure.biomes().contains(b);String id=b.unwrapKey().map(k->k.identifier().toString()).orElse("unkeyed");biomeChecks.merge(id+(allowed?" allowed":" rejected"),1,Integer::sum);if(allowed)accepted[0]++;return allowed;});
                    var point=structure.findValidGenerationPoint(context);trial.add("native_biome_checks",GSON.toJsonTree(biomeChecks));
                    if(point.isEmpty()){trial.addProperty("result",!biomeChecks.isEmpty()&&accepted[0]==0?"native_biome_filter_reject":"native_generation_point_reject");save(receipt);continue;}
                    trial.addProperty("native_generation_point_valid",true);
                }
                if(CHERRY_FRESH&&holder.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar")&&(cp.x()>=28163&&cp.x()<=28165&&cp.z()>=48905&&cp.z()<=48907)){trial.addProperty("result","reserved_r32_cherry_chunk_not_loaded");trial.addProperty("r32_cell_untouched",true);save(receipt);continue;}if(CHERRY_INNER1&&holder.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar")&&cp.x()>=13675&&cp.x()<=13677&&cp.z()>=22432&&cp.z()<=22434){trial.addProperty("result","r39_passed_cherry_chunk_not_loaded");trial.addProperty("not_a_defect",true);trial.addProperty("r39_site_untouched",true);save(receipt);continue;}ChunkAccess starts=source.getChunk(cp.x(),cp.z(),ChunkStatus.STRUCTURE_STARTS,true);check(starts!=null,"Native candidate chunk missing");
                StructureStart start=starts.getStartForStructure(structure);if(start==null||!start.isValid()){if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER){JsonArray siblings=new JsonArray();for(var nativeStart:starts.getAllStarts().entrySet())if(nativeStart.getValue()!=null&&nativeStart.getValue().isValid()){var key=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(nativeStart.getKey());if(key!=null)siblings.add(key.toString());}trial.add("actual_valid_structure_start_ids",siblings);trial.addProperty("result","target_absent_after_native_structure_set_arbitration");save(receipt);}continue;}if(EXTENSION||INDUSTRIAL||DESERT||BUNKER||MINER)trial.addProperty("result","actual_native_start_valid");
                if(BUNKER)check(!start.getPieces().isEmpty(),"Rejecting an empty bunker start");if(COMPONENTS||TOWER||SCORCHED||INDUSTRIAL||DESERT||MINER)check(start.getPieces().size()==1,"Authored single-root pool must not add jigsaw children");
                nativeStart=start;bounds=start.getBoundingBox();if(BUNKER&&BunkerRoomsContracts.reserved(bounds)){trial.addProperty("result","reserved_historical_box_not_placed");trial.addProperty("world_region_replayed",false);trial.addProperty("old_inventory_replayed",false);save(receipt);continue;}if(MINER&&MinerOutpostContracts.reserved(bounds)){trial.addProperty("result","reserved_historical_box_not_placed");trial.addProperty("world_region_replayed",false);trial.addProperty("old_inventory_replayed",false);save(receipt);continue;}if(INDUSTRIAL_RESUME&&reservedIndustrialBox(bounds)){trial.addProperty("result","reserved_native_box_not_replayed");trial.addProperty("world_region_replayed",false);save(receipt);continue;}pieceBounds=start.getPieces().stream().map(StructurePiece::getBoundingBox).toList();site.add("bounding_box",box(bounds));if(INDUSTRIAL_RESUME){site.addProperty("fresh_native_placement",true);site.addProperty("r35_region_replayed",false);site.addProperty("old_inventory_replayed",false);}if(EXPANSION){JsonArray pieces=new JsonArray();for(var piece:start.getPieces()){JsonObject row=new JsonObject();row.add("bounds",box(piece.getBoundingBox()));row.addProperty("rotation",piece.getRotation().toString());if(piece instanceof PoolElementStructurePiece pool){row.add("origin",xyz(pool.getPosition()));if(pool.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement single)row.addProperty("template_id",single.getTemplateLocation().toString());}pieces.add(row);check(piece.getBoundingBox().minY()>=level.getMinY()&&piece.getBoundingBox().maxY()<level.getMaxY(),"Expansion piece outside native build height");}site.add("pieces",pieces);}site.addProperty("piece_count",start.getPieces().size());site.addProperty("start_chunk_x",cp.x());site.addProperty("start_chunk_z",cp.z());
                int base=source.getGenerator().getBaseHeight(cp.getMiddleBlockX(),cp.getMiddleBlockZ(),Heightmap.Types.WORLD_SURFACE_WG,level,source.randomState());
                site.addProperty("unadapted_center_surface_y",base);site.addProperty("bbox_min_relative_to_center_surface",bounds.minY()-base);
                chunks=new ArrayList<>();int additional=0;
                for(int x=Math.floorDiv(bounds.minX(),16);x<=Math.floorDiv(bounds.maxX(),16);x++)for(int z=Math.floorDiv(bounds.minZ(),16);z<=Math.floorDiv(bounds.maxZ(),16);z++){
                    chunks.add(new ChunkPos(x,z));if(!fullChunks.contains(ChunkPos.pack(x,z)))additional++;
                }
                if(fullChunks.size()+additional>maxChunks){site.addProperty("chunks_needed",chunks.size());nextSite("pending: native start valid, placement chunk budget reached");return;}
                if(DESERT_LOCATOR&&(protectedHistoricalBox(bounds)||reservedIndustrialBox(bounds)||bounds.intersects(450610,782486,450652,782524))){trial.addProperty("result","reserved_cherry_bunker_or_industrial_box_not_placed");trial.addProperty("full_chunks_generated",false);save(receipt);continue;}if(CHERRY_FRESH&&holder.key().identifier().toString().equals("jeg:valen_hangars/cherry_hangar")){if(CHERRY_INNER1){String selected=null;for(var piece:nativeStart.getPieces())if(piece instanceof PoolElementStructurePiece pool&&pool.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement single&&single.getTemplateLocation().toString().contains("/frame/inner"))selected=single.getTemplateLocation().toString();if(!"jeg:valen_hangars/plains_hangar/frame/inner1".equals(selected)){trial.addProperty("result","native_inner_variant_not_inner1");trial.addProperty("not_a_defect",true);trial.addProperty("selected_inner",selected==null?"missing":selected);trial.addProperty("full_promotion",false);save(receipt);continue;}}if(CHERRY_INNER1_CONTINUE&&(reservedIndustrialBox(bounds)||protectedHistoricalBox(bounds)||bounds.intersects(919468,59,1706947,919532,107,1707004))){trial.addProperty("result","reserved_prior_box_not_placed");trial.addProperty("full_promotion",false);save(receipt);continue;}boolean reserved=bounds.intersects(450610,782486,450652,782524)||CHERRY_INNER1&&bounds.intersects(218806,358916,218844,358958);for(ChunkPos boxChunk:chunks)if((boxChunk.x()==28165&&boxChunk.z()==48907)||(boxChunk.x()==28163&&boxChunk.z()==48906))reserved=true;if(reserved){trial.addProperty("result","reserved_r32_cherry_box_not_promoted");trial.addProperty("r32_cell_untouched",true);save(receipt);continue;}JsonObject armed=CherryFreshTrace.arm(level,nativeStart);site.add("cherry_fresh_trace_targets",armed);if(armed.has("block_full")&&armed.get("block_full").getAsBoolean()){trial.addProperty("result",armed.has("reason")?armed.get("reason").getAsString():"fresh_trace_blocked");save(receipt);continue;}if(!armed.get("armed").getAsBoolean())site.addProperty("cherry_fresh_trace_coverage","unavailable");}site.add("loot_containers",new JsonArray());site.add("spawners",new JsonArray());chunkIndex=0;phase="place";save(receipt);return;
            }
        }
        void readChunk(LevelChunk chunk){
            if(HANGAR_HELIPAD||INDUSTRIAL||DESERT||BUNKER||MINER)return; // Industrial, desert, and bunker marker counts are read once after FULL. Bunker loot stays packed.
            for(var entry:List.copyOf(chunk.getBlockEntities().entrySet())){
                BlockPos pos=entry.getKey();if(!bounds.isInside(pos)||EXPANSION&&pieceBounds.stream().noneMatch(b->b.isInside(pos)))continue;BlockEntity be=entry.getValue();boolean readLoot=false;
                if(be instanceof RandomizableContainerBlockEntity cache&&cache.getLootTable()!=null&&cache.getLootTable().identifier().getNamespace().equals("jeg")){
                    readLoot=true;ResourceKey<LootTable> key=cache.getLootTable();lootKeys.add(key);JsonObject row=new JsonObject();row.add("pos",xyz(pos));row.addProperty("table",key.identifier().toString());cache.unpackLootTable(null);JsonArray items=new JsonArray();
                    for(int slot=0;slot<cache.getContainerSize();slot++){
                        ItemStack stack=cache.getItem(slot);if(stack.isEmpty())continue;JsonObject item=new JsonObject();item.addProperty("id",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());item.addProperty("count",stack.getCount());String blueprint=stack.get(ModDataComponents.BLUEPRINT_GUN.get());if(blueprint!=null)item.addProperty("blueprint_gun",blueprint);items.add(item);
                    }
                    row.add("actual_generated_items",items);row.addProperty("loot_key_consumed",cache.getLootTable()==null);check(cache.getLootTable()==null,"Actual placed barrel did not generate loot");site.getAsJsonArray("loot_containers").add(row);siteContainers++;containers++;
                }
                if(EXPANSION&&!readLoot&&be instanceof BarrelBlockEntity storage&&storage.getLootTable()==null){
                    for(int slot=0;slot<storage.getContainerSize();slot++)check(storage.getItem(slot).isEmpty(),"Authored empty storage unexpectedly contains items");siteEmptyStorage++;emptyStorage++;
                }
                if(be instanceof SpawnerBlockEntity spawner){
                    CompoundTag nbt=spawner.saveWithFullMetadata(level.registryAccess());CompoundTag spawn=nbt.getCompoundOrEmpty("SpawnData");String entity=spawn.getCompoundOrEmpty("entity").getStringOr("id","");if(!entity.equals("jeg:ghoul"))continue;
                    if(EXPANSION){check(nbt.getShortOr("SpawnCount",(short)-1)==1&&nbt.getShortOr("MaxNearbyEntities",(short)-1)==3&&nbt.getShortOr("RequiredPlayerRange",(short)-1)==12,"Expansion native spawner parameters changed");if(holder.key().identifier().toString().endsWith("aircraft_carrier"))check(pos.getY()>level.getSeaLevel(),"Carrier bridge encounter submerged");}
                    var parsed=SpawnData.CODEC.parse(NbtOps.INSTANCE,spawn).result().orElseThrow();check(parsed.getCustomSpawnRules().isPresent(),"Placed Ghoul spawner lost native light rules");JsonObject row=new JsonObject();row.add("pos",xyz(pos));row.addProperty("entity",entity);row.addProperty("spawn_count",nbt.getShortOr("SpawnCount",(short)-1));row.addProperty("max_nearby",nbt.getShortOr("MaxNearbyEntities",(short)-1));row.addProperty("player_range",nbt.getShortOr("RequiredPlayerRange",(short)-1));row.addProperty("activation_status","pending nearby actual fixture player");site.getAsJsonArray("spawners").add(row);trackedSpawners.put(pos.immutable(),row);siteSpawners++;spawners++;
                }
            }
        }
        void terrainReadback(){
            JsonArray samples=new JsonArray();site.add("native_terrain_samples",samples);BoundingBox b=pieceBounds.get(0);int cx=(b.minX()+b.maxX())/2,cz=(b.minZ()+b.maxZ())/2;
            int sea=level.getSeaLevel(),water=0,maxFloor=Integer.MIN_VALUE;
            for(int[] point:new int[][]{{cx,cz},{b.minX(),b.minZ()},{b.minX(),b.maxZ()},{b.maxX(),b.minZ()},{b.maxX(),b.maxZ()},{cx,b.minZ()},{cx,b.maxZ()},{b.minX(),cz},{b.maxX(),cz}}){
                int x=point[0],z=point[1];check(fullChunks.contains(ChunkPos.pack(x>>4,z>>4))||revalidatingJungle&&historicalReadChunks.contains(ChunkPos.pack(x>>4,z>>4)),"Terrain sample outside current NEW or exact bound historical FULL chunks");var chunk=level.getChunk(x>>4,z>>4);var source=level.getChunkSource();
                JsonObject row=new JsonObject();row.addProperty("x",x);row.addProperty("z",z);row.addProperty("actual_WORLD_SURFACE_native_height",chunk.getHeight(Heightmap.Types.WORLD_SURFACE,x&15,z&15));row.addProperty("unadapted_surface_y",source.getGenerator().getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,level,source.randomState()));samples.add(row);
                if(holder.key().identifier().toString().equals("jeg:valen/aircraft_carrier")){
                    int floor=source.getGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,level,source.randomState());maxFloor=Math.max(maxFloor,floor);row.addProperty("unadapted_ocean_floor_y",floor);var fluid=chunk.getFluidState(new BlockPos(x,sea-1,z));boolean wet=fluid.is(net.minecraft.tags.FluidTags.WATER);if(wet)water++;row.addProperty("water_at_sea_minus_one",wet);row.addProperty("sea_minus_one_block",BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(new BlockPos(x,sea-1,z)).getBlock()).toString());
                }
            }
            if(holder.key().identifier().toString().equals("jeg:valen/aircraft_carrier")){
                site.addProperty("native_sea_level",sea);site.addProperty("native_ship_base_y",b.minY());site.addProperty("expected_original_clamped_base_y",42);site.addProperty("water_samples_at_sea_minus_one",water);site.addProperty("max_sampled_unadapted_ocean_floor_y",maxFloor);
                check(b.minY()==42&&sea==63,"Carrier native source base/waterline changed");site.addProperty("carrier_waterline_observed",water>0);site.addProperty("carrier_sampled_seafloor_below_hull",maxFloor<b.minY());site.addProperty("waterline_visual_acceptance","pending native camera inspection; exact base/water/terrain samples recorded");
            }
        }
        void finish(){
            JsonArray sampling=new JsonArray();Set<String> sampledItems=new TreeSet<>();
            for(var key:lootKeys){
                LootTable table=level.getServer().reloadableRegistries().getLootTable(key);check(table!=LootTable.EMPTY,"Registered site loot missing: "+key);
                var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,new Vec3(0,220,0)).create(LootContextParamSets.CHEST);
                for(int seed=0;seed<1024;seed++)for(ItemStack stack:table.getRandomItems(params,(long)seed)){
                    String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();sampledItems.add(id);check(!id.equals("jeg:advanced_firearm_blueprint"),"Restricted blueprint leaked into ordinary site loot: "+key);
                }
                JsonObject row=new JsonObject();row.addProperty("table",key.identifier().toString());row.addProperty("seeds",1024);row.addProperty("advanced_blueprints",0);sampling.add(row);
            }
            // Preserve original earned boss/barter/raid gates. This samples native registered
            // boss loot with a boss context, and does not claim a boss fight was played.
            if(!EXPANSION&&!INDUSTRIAL&&!MINER){
            var key=ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("minecraft:entities/warden"));LootTable table=level.getServer().reloadableRegistries().getLootTable(key);
            var boss=net.minecraft.world.entity.EntityTypes.WARDEN.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);check(boss!=null,"Isolated boss loot context unavailable");
            var params=new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY,boss).withParameter(LootContextParams.ORIGIN,new Vec3(0,220,0)).withParameter(LootContextParams.DAMAGE_SOURCE,level.damageSources().generic()).create(LootContextParamSets.ENTITY);
            Set<String> targets=new TreeSet<>();int earned=0;
            for(int seed=0;seed<32;seed++)for(ItemStack stack:table.getRandomItems(params,(long)seed)){
                if(!BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("jeg:advanced_firearm_blueprint"))continue;String target=stack.get(ModDataComponents.BLUEPRINT_GUN.get());check(target!=null&&Set.of("jeg:subsonic_rifle","jeg:supersonic_shotgun","jeg:hypersonic_cannon").contains(target),"Earned Warden blueprint lost original target component");targets.add(target);earned++;
            }
            check(earned>0,"Original earned Warden blueprint pool missing");receipt.add("actual_registered_loot_sampling",sampling);receipt.add("sampled_item_ids",GSON.toJsonTree(sampledItems));receipt.addProperty("advanced_blueprints_in_structure_loot",false);receipt.addProperty("original_warden_context_samples",32);receipt.addProperty("earned_warden_blueprints",earned);receipt.add("earned_warden_targets",GSON.toJsonTree(targets));receipt.addProperty("earned_gate_proof_limit","Registered native boss loot/context sampled; no boss kill or raid gameplay claimed");
            }else{receipt.add("actual_registered_loot_sampling",sampling);receipt.add("sampled_item_ids",GSON.toJsonTree(sampledItems));receipt.addProperty("advanced_blueprints_in_structure_loot",false);receipt.addProperty("earned_gate_proof_limit","Original earned boss/barter/raid proofs retained, not replayed for this appended structure phase");}

            if(HANGAR_HELIPAD_RESUME){receipt.addProperty("newly_generated_structure_count",generated-(HANGAR_HELIPAD_ANCHORS?8:7)-revalidatedNaturalStarts);receipt.addProperty("newly_validated_structure_count",generated-(HANGAR_HELIPAD_ANCHORS?8:7));receipt.addProperty("historical_natural_starts_revalidated",revalidatedNaturalStarts);receipt.addProperty("historical_FULL_chunks_reloaded",historicalReadChunks.size());receipt.addProperty("new_FULL_chunks_this_run",fullChunks.size());}
            if(HBM_NEXT_RESUME||SCORCHED_RESUME){receipt.addProperty("newly_generated_structure_count",generated-3);receipt.addProperty("new_FULL_chunks_this_run",fullChunks.size());}
            receipt.addProperty("generated_structure_count",generated);receipt.addProperty("registered_structure_count",EXPANSION?EXPECTED_EXPANSION_COUNT:structures.size());receipt.addProperty("searched_structure_count_this_run",structures.size());receipt.addProperty("full_chunk_count",fullChunks.size());receipt.addProperty("expansion_FULL_chunks_including_imported",importedExpansionChunks+fullChunks.size());receipt.addProperty("total_full_chunks_including_prior",priorFullChunks+fullChunks.size());receipt.addProperty("placed_cache_count",containers);receipt.addProperty("placed_spawner_count",spawners);receipt.addProperty("placed_empty_storage_count",emptyStorage);receipt.addProperty("source_marker_count_mismatches",contractMismatches);receipt.addProperty("status",generated==(EXPANSION?EXPECTED_EXPANSION_COUNT:structures.size())&&contractMismatches==0?"native generation and placed entity readback passed; visual/activation review pending":"partial native generation/readback; inspect pending sites");done=true;save(receipt);System.out.println("VALEN_STRUCTURE_FIXTURE "+receipt);
        }
    }
    private static void monitor(ServerLevel level){
        if(activeReceipt==null||++monitorTicks%10!=0)return;boolean changed=false;
        for(var entry:trackedSpawners.entrySet()){
            BlockPos pos=entry.getKey();JsonObject row=entry.getValue();if(row.has("observed_new_ghoul_and_native_delay_reset"))continue;
            if(level.players().stream().noneMatch(p->p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=144))continue;
            if(!(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner))continue;int delay=spawner.saveWithFullMetadata(level.registryAccess()).getShortOr("Delay",(short)-1);Set<UUID> current=new HashSet<>();
            for(Entity e:level.getEntitiesOfClass(Entity.class,new AABB(pos).inflate(8),e->BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("jeg:ghoul")))current.add(e.getUUID());
            Integer before=priorDelays.put(pos,delay);Set<UUID> previous=priorGhouls.put(pos,current);row.addProperty("nearby_player_observed",true);row.addProperty("last_delay",delay);row.addProperty("nearby_ghouls",current.size());
            if(before!=null&&previous!=null&&delay>before&&current.stream().anyMatch(id->!previous.contains(id))){row.addProperty("observed_new_ghoul_and_native_delay_reset",true);row.addProperty("activation_status","new nearby Ghoul and native delay reset with actual nearby player");row.addProperty("observed_game_time",level.getGameTime());changed=true;}
        }
        if(changed||monitorTicks%200==0)save(activeReceipt);
    }
}
