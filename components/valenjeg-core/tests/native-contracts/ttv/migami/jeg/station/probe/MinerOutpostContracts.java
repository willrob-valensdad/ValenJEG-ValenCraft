package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

/** Fixture-only raw and placed checks for the one miner outpost. It does not generate a start. */
public final class MinerOutpostContracts {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FIXTURE = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
    private static final Path INDEX = Path.of(System.getProperty("valenjeg.evidence.43229ed11454", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-39.json"));
    private static final String ID = "jeg:valen_mining/miner_hut";
    private static final String TEMPLATE_SHA = "d8d55a69bd1e2af61a4a349043d667b367bad403c4c3dd621b67fa8159fd1e70";
    private static final String INDEX_SHA = "e6c1ff9a685a9b8f04e8b9dc6c84eedb33f0137c52bfab3879fa8c05d2966cf0";
    private static final String LOOT = "jeg:chests/valen_mining/miner_house";
    private static final long WORLD_SEED = -4056840922791553232L;
    private static final int CELLS = 2250;
    private static final int PALETTE = 61;
    private static final int SOURCE_BLOCK_ENTITIES = 10;
    private static final int[] MARKER = {0, 1, 0};
    private static final int[] LOWER_DOOR = {6, 1, 9};
    private static final int[] UPPER_DOOR = {6, 2, 9};
    private static final Map<Integer, String> TOOLBOX = Map.of(
            4, "create:hand_crank/3",
            16, "create:mechanical_drill/2",
            24, "minecraft:raw_iron_block/1",
            28, "minecraft:raw_copper_block/1");
    private static final String[] FILTERS = {"", "create:hand_crank/1", "", "", "create:mechanical_drill/1", "", "minecraft:raw_iron_block/1", "minecraft:raw_copper_block/1"};
    private static final int[] UNIQUE_ID = {-1463397049, 222905403, -1133440860, 788166780};
    private static final String[] INDEX_IDS = {
            "jeg:valen/field_lab", "jeg:valen/military_base", "jeg:valen/missile_silo", "jeg:valen/research_lab", "jeg:valen/satellite_station",
            "jeg:valen_ad_astra/space_station", "jeg:valen_ad_astra/moon_dungeon", "jeg:valen/vertibird_wreck", "jeg:valen/abandoned_factory", "jeg:valen/aircraft_carrier",
            "jeg:valen/broadcasting_tower", "jeg:valen/forest_outpost", "jeg:valen/radio_house", "jeg:valen/arctic_station", "jeg:valen/civil_defense_shelter",
            "jeg:valen/oil_rig", "jeg:valen/submarine_wreck", "jeg:valen/beached_patrol", "jeg:valen_scorched/asgharian_citadel", "jeg:valen_scorched/osgood_lab_outpost",
            "jeg:valen_scorched/trench", "jeg:valen_scorched/osgood_lab", "jeg:valen/watchtower", "jeg:valen/armory", "jeg:valen/listening_post",
            "jeg:valen_hangars/plains_hangar", "jeg:valen_hangars/savannah_hangar", "jeg:valen_hangars/taiga_hangar", "jeg:valen_helipads/forest_helipad", "jeg:valen_hangars/bamboo_hangar",
            "jeg:valen_hangars/birch_hangar", "jeg:valen_hangars/dark_oak_hangar", "jeg:valen_hangars/jungle_hangar", "jeg:valen_helipads/desert_helipad", "jeg:valen_industrial/train_station",
            "jeg:valen_industrial/warehouse", "jeg:valen_hangars/cherry_hangar", "jeg:valen/desert_atom_complex", "jeg:bunker_rooms"
    };
    private static final Set<String> DECOR = Set.of("minecraft:armor_stand", "minecraft:painting", "minecraft:item_frame", "minecraft:glow_item_frame");
    private static boolean passed;
    private static List<int[]> history = List.of();

    private MinerOutpostContracts() {}

    public static void register() {
        if (Boolean.getBoolean("valen.structure.probe.minerOutpost")) ServerLifecycleEvents.SERVER_STARTED.register(MinerOutpostContracts::preflight);
    }

    public static void requirePassed() {
        check(passed, "Miner outpost raw preflight has not passed");
    }

    public static boolean reservedChunk(int x, int z) {
        if (BunkerRoomsContracts.reservedChunk(x, z)) return true;
        for (int[] box : history) {
            int minX = Math.floorDiv(box[0], 16), maxX = Math.floorDiv(box[3], 16);
            int minZ = Math.floorDiv(box[2], 16), maxZ = Math.floorDiv(box[5], 16);
            if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) return true;
        }
        return false;
    }

    public static boolean reserved(BoundingBox box) {
        if (BunkerRoomsContracts.reserved(box)) return true;
        for (int[] row : history) if (box.intersects(row[0], row[1], row[2], row[3], row[4], row[5])) return true;
        return false;
    }

    public static void importThirtyNine(JsonObject receipt) {
        verifiedIndex();
        JsonObject index = JsonParser.parseString(new String(read(INDEX), StandardCharsets.UTF_8)).getAsJsonObject();
        receipt.add("prior_index_39_sites", index.getAsJsonArray("sites").deepCopy());
        receipt.addProperty("imported_index_39_rows", 39);
        receipt.addProperty("imported_rows_unchanged", true);
        receipt.addProperty("index_39_sha256", INDEX_SHA);
        receipt.addProperty("old_world_replayed", false);
        receipt.addProperty("old_inventory_replayed", false);
        receipt.addProperty("forced_starts", false);
    }

    private record AirRoles(Set<BlockPos> exterior, Set<BlockPos> walkAndDoorClearance) {}
    private static AirRoles airRoles(CompoundTag template, List<BlockState> palette) {
        Map<BlockPos,BlockState> source=new HashMap<>();
        Set<BlockPos> air=new HashSet<>(),exterior=new HashSet<>(),clearance=new HashSet<>();
        var size=template.getListOrEmpty("size");
        for(Tag entry:template.getListOrEmpty("blocks")){
            CompoundTag block=(CompoundTag)entry;var xyz=block.getListOrEmpty("pos");
            BlockPos pos=new BlockPos(((NumericTag)xyz.get(0)).intValue(),((NumericTag)xyz.get(1)).intValue(),((NumericTag)xyz.get(2)).intValue());
            BlockState state=palette.get(block.getIntOr("state",-1));source.put(pos,state);if(state.isAir())air.add(pos);
        }
        java.util.ArrayDeque<BlockPos> queue=new java.util.ArrayDeque<>();
        for(BlockPos pos:air)if(pos.getX()==0||pos.getY()==0||pos.getZ()==0||pos.getX()==14||pos.getY()==9||pos.getZ()==14){exterior.add(pos);queue.add(pos);}
        while(!queue.isEmpty()){BlockPos current=queue.removeFirst();for(Direction direction:Direction.values()){BlockPos next=current.relative(direction);if(air.contains(next)&&exterior.add(next))queue.add(next);}}
        for(BlockPos pos:air)if(!exterior.contains(pos)&&source.get(pos.below())!=null&&!source.get(pos.below()).isAir()&&air.contains(pos.above())){clearance.add(pos);clearance.add(pos.above());}
        for(int side:new int[]{-1,1})for(int dy:new int[]{0,1}){BlockPos pos=new BlockPos(LOWER_DOOR[0]+side,LOWER_DOOR[1]+dy,LOWER_DOOR[2]);if(air.contains(pos))clearance.add(pos);}
        check(clearance.size()>=90,"Authored walkable room/door clearance classification lost cells");
        return new AirRoles(exterior,clearance);
    }

    static void readNearby(MinecraftServer server, net.minecraft.server.level.ServerPlayer player, JsonObject receipt) throws java.io.IOException {
        requirePassed();
        check(server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath()), "Refusing non-fixture miner readback");
        check(player.getUUID().toString().equals("08d8976e-e59a-3751-ac2c-140b28fb5309") && player.level()==server.overworld(), "Miner readback requires actual synthetic Overworld player");
        check(player.distanceToSqr(350128.0,120.0,601632.0)<128.0*128.0, "Synthetic player must actually load the fresh mining area");
        var structure=server.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.parse(ID))).value();
        StructureStart start=server.overworld().getChunk(21883,37602).getStartForStructure(structure);
        check(start!=null&&start.isValid(), "Missing original natural mining start");
        JsonObject result=readPlaced(server.overworld(),start,ID);
        result.addProperty("status","pass");result.addProperty("read_only",true);result.addProperty("world_or_loot_replayed",false);
        receipt.add("miner_native_readback",result);
        Files.writeString(FIXTURE.resolve("valen-miner-outpost-nearby-readback.json"),GSON.toJson(result)+"\n");
    }

    public static JsonObject readPlaced(ServerLevel level, StructureStart start, String id) {
        requirePassed();
        check(id.equals(ID), "Miner readback received another structure");
        check(start.getPieces().size() == 1 && start.getPieces().getFirst() instanceof PoolElementStructurePiece, "Miner start is not the one authored piece");
        PoolElementStructurePiece piece = (PoolElementStructurePiece) start.getPieces().getFirst();
        check(piece.getElement() instanceof SinglePoolElement single && single.getTemplateLocation().toString().equals(ID), "Placed template is not the miner hut");
        byte[] bytes = resourceBytes(level.getServer(), Identifier.parse("jeg:structure/valen_mining/miner_hut.nbt"));
        check(sha(bytes).equals(TEMPLATE_SHA), "Placed miner template hash differs from the staged source");
        CompoundTag tag = readNbt(bytes);
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : tag.getListOrEmpty("palette")) {
            check(entry instanceof CompoundTag, "Malformed placed palette");
            BlockState resolved = resolve((CompoundTag) entry, new JsonObject(), new JsonArray());
            check(resolved != null, "Placed miner palette state is quarantined, not air");
            palette.add(resolved);
        }
        AirRoles roles=airRoles(tag,palette);
        int solidVerified=0,clearanceVerified=0;JsonArray environmentalChanges=new JsonArray();
        int compared = 0, ignored = 0, blockEntities = 0, barrels = 0, toolbox = 0;
        JsonArray mismatches = new JsonArray(), lootRows = new JsonArray(), entities = new JsonArray();
        BlockPos door = null;
        Direction doorFacing = null;
        int floorMinX = Integer.MAX_VALUE, floorMinZ = Integer.MAX_VALUE, floorMaxX = Integer.MIN_VALUE, floorMaxZ = Integer.MIN_VALUE, floorAir = 0;
        for (Tag entry : tag.getListOrEmpty("blocks")) {
            CompoundTag block = (CompoundTag) entry;
            var local = block.getListOrEmpty("pos");
            int x = ((NumericTag) local.get(0)).intValue(), y = ((NumericTag) local.get(1)).intValue(), z = ((NumericTag) local.get(2)).intValue();
            BlockState expected = palette.get(block.getIntOr("state", -1));
            BlockState rotated = expected.rotate(piece.getRotation());
            BlockPos world = StructureTemplate.transform(new BlockPos(x, y, z), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
            CompoundTag nbt = block.getCompoundOrEmpty("nbt");
            if (x == MARKER[0] && y == MARKER[1] && z == MARKER[2]) {
                ignored++;
                check(expected.is(Blocks.STRUCTURE_BLOCK) && nbt.getStringOr("id", "").equals("minecraft:structure_block"), "Authoring marker is not the source structure block");
                check(!level.getBlockState(world).is(Blocks.STRUCTURE_BLOCK), "Authoring marker was placed; the loaded ignore processor did not drop it");
                continue;
            }
            compared++;
            BlockState actual = level.getBlockState(world);
            BlockPos sourcePos=new BlockPos(x,y,z);
            if(!expected.isAir())solidVerified++;
            if(roles.walkAndDoorClearance().contains(sourcePos))clearanceVerified++;
            boolean wallUpdate=!actual.equals(rotated)&&rotated.getBlock() instanceof net.minecraft.world.level.block.WallBlock&&actual.getBlock()==rotated.getBlock()&&net.minecraft.world.level.block.Block.updateFromNeighbourShapes(rotated,level,world).equals(actual);
            boolean foliage=!actual.equals(rotated)&&expected.isAir()&&!roles.walkAndDoorClearance().contains(sourcePos)&&(actual.is(net.minecraft.tags.BlockTags.LEAVES)||(roles.exterior().contains(sourcePos)&&(actual.is(net.minecraft.tags.BlockTags.LOGS)||actual.is(net.minecraft.tags.BlockTags.FLOWERS)||actual.is(Blocks.SHORT_GRASS)||actual.is(Blocks.TALL_GRASS)||actual.is(Blocks.FERN)||actual.is(Blocks.LARGE_FERN))));
            if(wallUpdate||foliage){JsonObject change=new JsonObject();change.addProperty("source",x+","+y+","+z);change.addProperty("world",world.toString());change.addProperty("actual",actual.toString());change.addProperty("reason",wallUpdate?"exact native neighbour-shape reconciliation":roles.exterior().contains(sourcePos)?"ambient outdoor vegetation":"leaf foliage in non-walkable roof space");environmentalChanges.add(change);}
            if (!actual.equals(rotated)&&!wallUpdate&&!foliage) {
                if (mismatches.size() < 32) {
                    JsonObject error = new JsonObject();
                    error.addProperty("world", world.toString());
                    error.addProperty("source", x + "," + y + "," + z);
                    error.addProperty("expected", rotated.toString());
                    error.addProperty("actual", actual.toString());
                    error.addProperty("rotation", piece.getRotation().toString());
                    error.addProperty("mirror", "none");
                    mismatches.add(error);
                }
                continue;
            }
            if (x == LOWER_DOOR[0] && y == LOWER_DOOR[1] && z == LOWER_DOOR[2]) check(expected.getBlock() instanceof DoorBlock && expected.getValue(DoorBlock.FACING) == Direction.EAST && expected.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER, "Source lower door is not the east lower half");
            if (y == 1 && actual.isAir()) {
                floorAir++;
                floorMinX = Math.min(floorMinX, x); floorMinZ = Math.min(floorMinZ, z);
                floorMaxX = Math.max(floorMaxX, x); floorMaxZ = Math.max(floorMaxZ, z);
            }
            if (actual.getBlock() instanceof DoorBlock && actual.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                door = world;
                doorFacing = actual.getValue(DoorBlock.FACING);
            }
            if (nbt.isEmpty()) continue;
            String rawId = nbt.getStringOr("id", "");
            BlockEntity be = level.getBlockEntity(world);
            String actualBe = be == null ? "absent" : BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString();
            check(be != null && actualBe.equals(rawId), "Placed miner block entity at " + world + " expected " + rawId + " actual " + actualBe);
            blockEntities++;
            CompoundTag saved = be.saveWithFullMetadata(level.registryAccess());
            if (rawId.equals("minecraft:barrel")) {
                check(saved.getStringOr("LootTable", "").equals(LOOT) && !saved.contains("Items"), "Placed barrel loot is not packed metadata");
                barrels++;
                JsonObject row = new JsonObject();
                row.addProperty("source", x + "," + y + "," + z);
                row.addProperty("table", LOOT);
                row.addProperty("opened_with_getItem", false);
                lootRows.add(row);
            } else if (rawId.equals("create:toolbox")) {
                check(slots(saved).equals(TOOLBOX), "Placed toolbox slots differ from the saved source metadata");
                toolbox++;
            }
        }
        check(mismatches.isEmpty(), "Placed miner source-state mismatches: " + mismatches);
        check(compared == CELLS - 1 && ignored == 1 && blockEntities == SOURCE_BLOCK_ENTITIES - 1 && barrels == 2 && toolbox == 1, "Placed miner cell, marker, block entity, barrel, or toolbox counts differ");
        check(mismatches.isEmpty() && floorAir > 0, "Placed hut floor has no interior air");
        check(door != null && doorFacing != null, "Placed lower door is missing");
        Vec3 localStand = new Vec3(LOWER_DOOR[0] + 0.5, LOWER_DOOR[1], LOWER_DOOR[2] + 0.5).relative(Direction.EAST, -1);
        Vec3 stand = StructureTemplate.transform(localStand, Mirror.NONE, piece.getRotation(), BlockPos.ZERO).add(piece.getPosition().getX(), piece.getPosition().getY(), piece.getPosition().getZ());
        JsonObject evidence = new JsonObject();
        check(solidVerified==645&&clearanceVerified==roles.walkAndDoorClearance().size(),"Original architecture or protected room/door coverage differs");
        evidence.addProperty("placed_source_solid_cells_verified",solidVerified);evidence.addProperty("walkable_room_and_door_air_cells_verified",clearanceVerified);evidence.add("ambient_foliage_and_native_connections",environmentalChanges);evidence.addProperty("acceptance_scope","Every authored solid block and native state, all derived walkable room/door air, all BE/loot/decor payloads; outdoor/unused roof foliage recorded separately; native wall connections reconciled read-only");
        evidence.addProperty("template_sha256", TEMPLATE_SHA);
        evidence.addProperty("compared_source_cells", compared);
        evidence.addProperty("ignored_authoring_marker", ignored);
        evidence.addProperty("placed_block_entities", blockEntities);
        evidence.addProperty("placed_loot", barrels);
        evidence.add("placed_loot_rows", lootRows);
        evidence.addProperty("loot_unpacked", false);
        evidence.addProperty("containers_opened_with_getItem", false);
        evidence.addProperty("piece_rotation", piece.getRotation().toString());
        evidence.addProperty("mirror", Mirror.NONE.getSerializedName());
        evidence.add("state_mismatch_diagnostics", mismatches);
        evidence.add("scene_readback", scene(piece, stand, door, doorFacing, floorAir, floorMinX, floorMinZ, floorMaxX, floorMaxZ));
        evidence.add("source_entities", matchEntities(level, start, piece, tag, entities));
        evidence.addProperty("source_entity_count", 4);
        evidence.addProperty("visual_acceptance", "Entrance and room coordinates are a readback for later access. This fixture does not claim a visual pass.");
        return evidence;
    }

    private static void preflight(MinecraftServer server) {
        JsonObject report = new JsonObject();
        JsonArray paletteRows = new JsonArray(), blockEntities = new JsonArray(), mismatches = new JsonArray(), pending = new JsonArray(), failures = new JsonArray();
        report.addProperty("source", ID);
        report.addProperty("template_sha256", TEMPLATE_SHA);
        report.addProperty("strict_required", 0);
        report.add("palette", paletteRows);
        report.add("block_entities", blockEntities);
        report.add("state_mismatch_diagnostics", mismatches);
        report.add("pending", pending);
        report.add("strict_failures", failures);
        boolean guarded = false;
        try {
            guard(server);
            guarded = true;
            long seed = server.overworld().getSeed();
            report.addProperty("world_seed", seed);
            if (seed != WORLD_SEED) fail(failures, "world_seed", "fixture seed differs");
            verifiedIndex();
            byte[] bytes = resourceBytes(server, Identifier.parse("jeg:structure/valen_mining/miner_hut.nbt"));
            String actualSha = sha(bytes);
            report.addProperty("template_sha256", actualSha);
            if (!actualSha.equals(TEMPLATE_SHA)) fail(failures, "template_sha256", "staged template hash differs");
            CompoundTag tag = readNbt(bytes);
            if (tag.getIntOr("DataVersion", -1) != 5023) fail(failures, "DataVersion", "expected 5023");
            var size = tag.getListOrEmpty("size");
            if (size.size() != 3 || ((NumericTag) size.get(0)).intValue() != 15 || ((NumericTag) size.get(1)).intValue() != 10 || ((NumericTag) size.get(2)).intValue() != 15) fail(failures, "size", "template dimensions differ");
            List<BlockState> palette = new ArrayList<>();
            int quarantined = 0;
            for (Tag entry : tag.getListOrEmpty("palette")) {
                JsonObject row = new JsonObject();
                paletteRows.add(row);
                if (!(entry instanceof CompoundTag raw)) {
                    quarantined++;
                    palette.add(null);
                    row.addProperty("state", "null");
                    mismatches.add(note("palette", "entry is not a compound"));
                    continue;
                }
                BlockState state = resolve(raw, row, mismatches);
                palette.add(state);
                if (state == null) quarantined++;
            }
            if (palette.size() != PALETTE || quarantined != 0) fail(failures, "palette", "count or quarantine differs");
            int cells = 0, decoded = 0, skipped = 0, doors = 0;
            Set<String> positions = new HashSet<>();
            Map<String, Integer> beCounts = new LinkedHashMap<>();
            for (Tag entry : tag.getListOrEmpty("blocks")) {
                if (!(entry instanceof CompoundTag block)) { fail(failures, "cell", "malformed block"); continue; }
                cells++;
                var local = block.getListOrEmpty("pos");
                int x = ((NumericTag) local.get(0)).intValue(), y = ((NumericTag) local.get(1)).intValue(), z = ((NumericTag) local.get(2)).intValue();
                if (!positions.add(x + "," + y + "," + z)) fail(failures, "cell", "duplicate position");
                int index = block.getIntOr("state", -1);
                if (index < 0 || index >= palette.size() || palette.get(index) == null) { fail(failures, "cell", "unresolved palette index"); continue; }
                BlockState state = palette.get(index);
                if (state.getBlock() instanceof DoorBlock) doors++;
                CompoundTag nbt = block.getCompoundOrEmpty("nbt");
                if (nbt.isEmpty()) continue;
                JsonObject row = decodeBlockEntity(server, state, nbt, x, y, z, failures);
                blockEntities.add(row);
                if (!row.get("lossless").getAsBoolean()) skipped++;
                else { decoded++; beCounts.merge(nbt.getStringOr("id", ""), 1, Integer::sum); }
            }
            report.addProperty("palette_count", palette.size());
            report.addProperty("source_positions", cells);
            report.addProperty("quarantined_palette_states", quarantined);
            report.addProperty("decoded_block_entities", decoded);
            report.addProperty("skipped_block_entity_decodes", skipped);
            if (cells != CELLS || doors != 2 || decoded != SOURCE_BLOCK_ENTITIES || skipped != 0) fail(failures, "counts", "positions, doors, or block entities differ");
            if (beCounts.getOrDefault("minecraft:structure_block", 0) != 1 || beCounts.getOrDefault("create:hand_crank", 0) != 2 || beCounts.getOrDefault("create:drill", 0) != 2 || beCounts.getOrDefault("create:sliding_door", 0) != 1 || beCounts.getOrDefault("minecraft:barrel", 0) != 2 || beCounts.getOrDefault("minecraft:blast_furnace", 0) != 1 || beCounts.getOrDefault("create:toolbox", 0) != 1) fail(failures, "block_entities", "block entity type counts differ");
            decodeEntities(server, tag, failures);
            loot(server, failures);
            configured(server, failures);
            report.addProperty("pending_accepted", false);
            report.addProperty("skipped_accepted", false);
            boolean strict = failures.isEmpty() && mismatches.isEmpty() && pending.isEmpty() && skipped == 0 && quarantined == 0;
            report.addProperty("status", strict ? "pass" : "fail");
            passed = strict;
            if (!strict) throw new IllegalStateException("Miner raw preflight fail; skipped and pending are not a pass");
        } catch (Throwable error) {
            if (!report.has("status")) report.addProperty("status", "fail");
            if (!report.has("world_seed")) report.addProperty("world_seed", "unavailable");
            report.addProperty("error", error.toString());
            passed = false;
            if (guarded) throw new IllegalStateException("Miner raw preflight failed", error);
            throw error instanceof RuntimeException runtime ? runtime : new IllegalStateException(error);
        } finally {
            if (guarded) try { Files.writeString(server.getServerDirectory().resolve("valen-miner-outpost-raw-preflight.json"), GSON.toJson(report) + "\n"); }
            catch (IOException error) { throw new IllegalStateException(error); }
        }
    }

    private static JsonObject decodeBlockEntity(MinecraftServer server, BlockState state, CompoundTag nbt, int x, int y, int z, JsonArray failures) {
        JsonObject row = new JsonObject();
        row.addProperty("source", x + "," + y + "," + z);
        String rawId = nbt.getStringOr("id", "");
        row.addProperty("id", rawId);
        row.addProperty("lossless", false);
        try {
            Identifier beId = Identifier.parse(rawId);
            if (!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(beId)) { fail(failures, rawId, "absent from BLOCK_ENTITY_TYPE"); return row; }
            var type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(beId);
            if (!type.isValid(state)) { fail(failures, rawId, "not valid for " + BuiltInRegistries.BLOCK.getKey(state.getBlock())); return row; }
            BlockEntity loaded = type.create(new BlockPos(x, y, z), state);
            if (loaded == null) { fail(failures, rawId, "type.create returned null"); return row; }
            try (var reporter = new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(rawId), LoggerFactory.getLogger(MinerOutpostContracts.class))) {
                loaded.loadWithComponents(TagValueInput.create(reporter, server.registryAccess(), nbt));
                if (!reporter.isEmpty()) { fail(failures, rawId, "ProblemReporter loss: " + reporter.getReport()); return row; }
            }
            CompoundTag saved = loaded.saveWithFullMetadata(server.registryAccess());
            if (rawId.equals("create:toolbox") && !slots(saved).equals(TOOLBOX)) { fail(failures, rawId, "toolbox metadata slots differ"); return row; }
            if (rawId.equals("minecraft:barrel") && (!saved.getStringOr("LootTable", "").equals(LOOT) || saved.contains("Items"))) { fail(failures, rawId, "barrel metadata is not the packed loot id"); return row; }
            if (rawId.equals("minecraft:structure_block") && !(x == 0 && y == 1 && z == 0 && saved.getStringOr("name", "").equals(ID) && saved.getStringOr("mode", "").equals("SAVE"))) { fail(failures, rawId, "authoring structure block name, mode, or position differs"); return row; }
            row.addProperty("lossless", true);
            row.addProperty("saved_metadata", true);
            row.addProperty("opened_with_getItem", false);
        } catch (Throwable error) {
            fail(failures, rawId, error.toString());
        }
        return row;
    }

    private static void decodeEntities(MinecraftServer server, CompoundTag tag, JsonArray failures) {
        CompoundTag resolvedTemplate = server.getStructureTemplateManager().get(Identifier.parse(ID)).orElseThrow().save(new CompoundTag());
        check(resolvedTemplate.getListOrEmpty("entities").size() == 4, "Native resource/datafix template lost mining entities: " + resolvedTemplate.getListOrEmpty("entities"));
        List<Tag> entities = resolvedTemplate.getListOrEmpty("entities");
        if (entities.size() != 4) { fail(failures, "entities", "expected four decorations"); return; }
        int paintings = 0, stands = 0;
        for (Tag entry : entities) {
            CompoundTag entity = (CompoundTag) entry;
            CompoundTag nbt = entity.getCompoundOrEmpty("nbt");
            String rawId = nbt.getStringOr("id", "");
            if (!DECOR.contains(rawId)) { fail(failures, rawId, "decoration is outside the minipurge decor set"); continue; }
            try (var reporter = new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(rawId), LoggerFactory.getLogger(MinerOutpostContracts.class))) {
                var loaded = EntityType.create(TagValueInput.create(reporter, server.registryAccess(), nbt), server.overworld(), new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.STRUCTURE, false));
                if (!reporter.isEmpty() || loaded.isEmpty()) { fail(failures, rawId, reporter.isEmpty() ? "entity codec returned empty" : reporter.getReport()); continue; }
                if (loaded.get() instanceof Painting painting) {
                    Direction facing = Direction.LEGACY_ID_CODEC_2D.parse(NbtOps.INSTANCE, nbt.get("facing")).getOrThrow();
                    if (facing != Direction.WEST || painting.getDirection() != Direction.WEST) fail(failures, rawId, "painting facing is not west");
                    String variant = nbt.getStringOr("variant", "");
                    var key = painting.getVariant().unwrapKey().map(k -> k.identifier().toString()).orElse("");
                    if (!variant.equals(key) || !(variant.equals("minecraft:wanderer") || variant.equals("minecraft:graham"))) fail(failures, rawId, "painting variant differs");
                    paintings++;
                } else if (loaded.get() instanceof ArmorStand) {
                    check(Math.abs(loaded.get().getYRot() + 45.0F) < 0.01F && Math.abs(loaded.get().getXRot()) < 0.01F, "Raw armor stand source Rotation[-45,0] was not preserved");
                    CompoundTag saved = saveEntity(server, loaded.get());
                    for (String key : List.of("Invisible", "Small", "ShowArms", "DisabledSlots", "NoBasePlate")) if (number(nbt, key) != number(saved, key)) fail(failures, rawId, key + " differs");
                    stands++;
                }
            } catch (Throwable error) { fail(failures, rawId, error.toString()); }
        }
        if (paintings != 3 || stands != 1) fail(failures, "entities", "painting or armor stand count differs");
    }

    private static JsonArray matchEntities(ServerLevel level, StructureStart start, PoolElementStructurePiece piece, CompoundTag tag, JsonArray rows) {
        List<Entity> present = level.getEntitiesOfClass(Entity.class, AABB.of(start.getBoundingBox()).inflate(2));
        int matched = 0;
        for (Tag entryTag : tag.getListOrEmpty("entities")) {
            CompoundTag entry = (CompoundTag) entryTag;
            CompoundTag nbt = entry.getCompoundOrEmpty("nbt");
            String entityId = nbt.getStringOr("id", "");
            ListTag pos = entry.getListOrEmpty("pos");
            Vec3 local = new Vec3(pos.getDoubleOr(0, 0), pos.getDoubleOr(1, 0), pos.getDoubleOr(2, 0));
            Vec3 transformed = StructureTemplate.transform(local, Mirror.NONE, piece.getRotation(), BlockPos.ZERO).add(piece.getPosition().getX(), piece.getPosition().getY(), piece.getPosition().getZ());
            JsonObject row = new JsonObject();
            row.addProperty("id", entityId);
            Vec3 expected = transformed;
            BlockPos anchor = null;
            AABB box = null;
            if (entityId.equals("minecraft:painting")) {
                Direction facing = Direction.LEGACY_ID_CODEC_2D.parse(NbtOps.INSTANCE, nbt.get("facing")).getOrThrow();
                check(facing == Direction.WEST, "Source painting facing is not west");
                Direction rotated = BunkerAccessServerReadback.rotateHorizontal(facing, piece.getRotation());
                ListTag blockPos = entry.getListOrEmpty("blockPos");
                anchor = StructureTemplate.transform(new BlockPos(blockPos.getIntOr(0, 0), blockPos.getIntOr(1, 0), blockPos.getIntOr(2, 0)), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
                var variant = level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(ResourceKey.create(Registries.PAINTING_VARIANT, Identifier.parse(nbt.getStringOr("variant", "")))).value();
                expected = BunkerAccessServerReadback.paintingCenter(anchor, rotated, variant.width(), variant.height());
                box = BunkerAccessServerReadback.paintingBox(expected, rotated, variant.width(), variant.height());
                row.add("authored_anchor", xyz(anchor.getX(), anchor.getY(), anchor.getZ()));
                row.addProperty("facing", rotated.getSerializedName());
                row.addProperty("variant", nbt.getStringOr("variant", ""));
                row.add("authored_box", box(box));
            }
            row.add("expected_pos", xyz(expected.x, expected.y, expected.z));
            Entity found = nearest(present, entityId, expected);
            row.addProperty("matched_saved_entity", found != null);
            if (found instanceof Painting painting && anchor != null && box != null) {
                check(painting.getDirection() == BunkerAccessServerReadback.rotateHorizontal(Direction.WEST, piece.getRotation()), "Placed painting facing differs");
                check(painting.getPos().equals(anchor), "Placed painting anchor differs");
                check(sameBox(painting.getBoundingBox(), box), "Placed painting box differs");
                check(painting.getVariant().unwrapKey().map(k -> k.identifier().toString()).orElse("").equals(nbt.getStringOr("variant", "")), "Placed painting variant differs");
            }
            if (found instanceof ArmorStand stand) {
                float expectedYaw = -45.0F + switch (piece.getRotation()) { case NONE -> 0.0F; case CLOCKWISE_90 -> 90.0F; case CLOCKWISE_180 -> 180.0F; case COUNTERCLOCKWISE_90 -> -90.0F; };
                check(Math.abs(net.minecraft.util.Mth.wrapDegrees(stand.getYRot() - expectedYaw)) < 0.01F && Math.abs(stand.getXRot()) < 0.01F, "Placed armor stand source/quarter-turn rotation differs");
                row.addProperty("expected_yaw", expectedYaw);
                row.addProperty("actual_yaw", stand.getYRot());
                CompoundTag saved = saveEntity(level.getServer(), stand);
                for (String key : List.of("Invisible", "Small", "ShowArms", "DisabledSlots", "NoBasePlate")) check(number(nbt, key) == number(saved, key), "Placed armor stand " + key + " differs");
            }
            if (found != null) matched++;
            rows.add(row);
        }
        check(matched == 4, "Placed decorations do not match the four source entities");
        return rows;
    }

    private static void loot(MinecraftServer server, JsonArray failures) {
        try {
            Identifier parsed = Identifier.parse(LOOT);
            Identifier resource = Identifier.fromNamespaceAndPath(parsed.getNamespace(), "loot_table/" + parsed.getPath() + ".json");
            String json;
            try (var in = server.getResourceManager().getResource(resource).orElseThrow().open()) { json = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            LootTable.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), JsonParser.parseString(json)).getOrThrow();
            if (server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, parsed)) == LootTable.EMPTY) fail(failures, LOOT, "reloadable loot registry missed the table");
            for (String item : List.of("minecraft:coal", "minecraft:raw_iron", "minecraft:rail", "minecraft:raw_gold", "create:crushed_raw_iron", "minecraft:tnt", "minecraft:diamond")) if (!json.contains(item)) fail(failures, LOOT, "missing " + item);
            if (!json.contains("\"random_sequence\": \"jeg:chests/valen_mining/miner_house\"")) fail(failures, LOOT, "random_sequence binding differs");
        } catch (Throwable error) { fail(failures, LOOT, error.toString()); }
    }

    private static void configured(MinecraftServer server, JsonArray failures) {
        try {
            var lookup = server.registryAccess();
            var key = ResourceKey.create(Registries.STRUCTURE, Identifier.parse(ID));
            var structure = lookup.lookupOrThrow(Registries.STRUCTURE).getOrThrow(key).value();
            check(structure instanceof JigsawStructure, "Miner structure is not a jigsaw");
            JsonObject encoded = JigsawStructure.CODEC.codec().encodeStart(RegistryOps.create(JsonOps.INSTANCE, lookup), (JigsawStructure) structure).getOrThrow().getAsJsonObject();
            if (!encoded.get("step").getAsString().equals("top_layer_modification")) fail(failures, "generation_step", "miner must preserve authored cells after vegetal decoration");
            if (!encoded.has("size") || encoded.get("size").getAsInt() != 4) fail(failures, "jigsaw_size", "live size is not the source size 4");
            JsonElement biomes = encoded.get("biomes");
            if (biomes == null || !biomes.isJsonPrimitive() || !biomes.getAsString().equals("#minecraft:is_mountain")) fail(failures, "biomes", "live biome source is not #minecraft:is_mountain: " + biomes);
            String height = encoded.has("project_start_to_heightmap") ? encoded.get("project_start_to_heightmap").getAsString() : "";
            if (!height.equals("WORLD_SURFACE_WG")) fail(failures, "heightmap", "live heightmap is not WORLD_SURFACE_WG");
            if (!structure.terrainAdaptation().getSerializedName().equals("beard_thin")) fail(failures, "terrain_adaptation", "live terrain adaptation is not beard_thin");
            StructureSet set = null;
            for (var holder : lookup.lookupOrThrow(Registries.STRUCTURE_SET).listElements().toList()) for (var entry : holder.value().structures()) if (entry.structure().is(key)) set = holder.value();
            if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement spread)) { fail(failures, "placement", "live random-spread placement is missing"); return; }
            JsonObject placement = RandomSpreadStructurePlacement.CODEC.codec().encodeStart(JsonOps.INSTANCE, spread).getOrThrow().getAsJsonObject();
            if (spread.spacing() != 30 || spread.separation() != 6 || placement.get("salt").getAsInt() != 1728102408 || !placement.get("spread_type").getAsString().equals("triangular")) fail(failures, "placement", "spacing, separation, salt, or spread type differs");
            var templates = ((JigsawStructure) structure).getStartPool().value().getTemplates();
            if (templates.size() != 1 || !(templates.getFirst().getFirst() instanceof SinglePoolElement element) || !element.getTemplateLocation().toString().equals(ID)) { fail(failures, "pool", "start pool is not the one rigid miner template"); return; }
            JsonObject elementJson = StructurePoolElement.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, lookup), element).getOrThrow().getAsJsonObject();
            JsonObject processors = elementJson.getAsJsonObject("processors");
            if (processors == null || !processors.has("processors")) { fail(failures, "processors", "loaded element has no inline processor list"); return; }
            JsonObject processor = processors.getAsJsonArray("processors").get(0).getAsJsonObject();
            var loaded = BlockIgnoreProcessor.MAP_CODEC.codec().parse(JsonOps.INSTANCE, processor).getOrThrow();
            var info = new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.STRUCTURE_BLOCK.defaultBlockState(), new CompoundTag());
            if (loaded.processBlock(server.overworld(), BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, info, new StructurePlaceSettings()) != null) fail(failures, "processors", "loaded processor did not drop a structure block");
            var kept = new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), new CompoundTag());
            if (loaded.processBlock(server.overworld(), BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, kept, new StructurePlaceSettings()) == null) fail(failures, "processors", "loaded processor dropped a non-structure block");
        } catch (Throwable error) { fail(failures, "configured", error.toString()); }
    }

    private static JsonObject scene(PoolElementStructurePiece piece, Vec3 stand, BlockPos door, Direction doorFacing, int floorAir, int minX, int minZ, int maxX, int maxZ) {
        JsonObject scene = new JsonObject();
        JsonArray rooms = new JsonArray();
        JsonObject room = new JsonObject();
        room.addProperty("name", "hut");
        room.addProperty("floor_air_cells", floorAir);
        room.add("source_floor_min", xyz(minX, 1, minZ));
        room.add("source_floor_max", xyz(maxX, 1, maxZ));
        BlockPos min = StructureTemplate.transform(new BlockPos(minX, 1, minZ), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
        BlockPos max = StructureTemplate.transform(new BlockPos(maxX, 1, maxZ), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
        room.add("world_floor_corner_a", xyz(min.getX(), min.getY(), min.getZ()));
        room.add("world_floor_corner_b", xyz(max.getX(), max.getY(), max.getZ()));
        rooms.add(room);
        JsonObject entrance = new JsonObject();
        entrance.add("lower_door_world", xyz(door.getX(), door.getY(), door.getZ()));
        entrance.add("stand_world", xyz(stand.x, stand.y, stand.z));
        entrance.addProperty("door_facing", doorFacing.getSerializedName());
        BlockPos upper = StructureTemplate.transform(new BlockPos(UPPER_DOOR[0], UPPER_DOOR[1], UPPER_DOOR[2]), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
        entrance.add("upper_door_world", xyz(upper.getX(), upper.getY(), upper.getZ()));
        entrance.addProperty("route_use", "Stand at stand_world, facing the lower door. The hut is one room. These coordinates do not teleport.");
        scene.add("rooms", rooms);
        scene.add("entrance", entrance);
        return scene;
    }

    private static JsonObject verifiedIndex() {
        byte[] bytes = read(INDEX);
        check(sha(bytes).equals(INDEX_SHA), "Frozen index 39 hash differs");
        JsonObject index = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        check(index.get("entry_count").getAsInt() == 39 && index.get("world_seed").getAsLong() == WORLD_SEED && !index.get("forced_starts").getAsBoolean() && !index.get("old_rows_replayed").getAsBoolean(), "Index 39 header differs");
        JsonArray sites = index.getAsJsonArray("sites");
        check(sites.size() == INDEX_IDS.length, "Index 39 row count differs");
        List<int[]> boxes = new ArrayList<>();
        for (int n = 0; n < sites.size(); n++) {
            JsonObject row = sites.get(n).getAsJsonObject();
            check(row.get("id").getAsString().equals(INDEX_IDS[n]) && row.get("status").getAsString().startsWith("actual native start"), "Index 39 row is not the verbatim passed source");
            JsonArray box = row.getAsJsonArray("bounding_box");
            boxes.add(new int[]{box.get(0).getAsInt(), box.get(1).getAsInt(), box.get(2).getAsInt(), box.get(3).getAsInt(), box.get(4).getAsInt(), box.get(5).getAsInt()});
        }
        history = List.copyOf(boxes);
        return index;
    }

    private static Map<Integer, String> slots(CompoundTag saved) {
        CompoundTag inventory = saved.getCompoundOrEmpty("Inventory");
        Tag itemsField = inventory.get("Items");
        check(itemsField instanceof CompoundTag, "Toolbox Items is not the ItemSlots compound");
        CompoundTag items = (CompoundTag) itemsField;
        check(items.getIntOr("size", -1) == 32, "Toolbox ItemSlots size is not the 32-slot capacity");
        Tag mapField = items.get("items");
        check(mapField instanceof CompoundTag, "Toolbox ItemSlots items is not a string-key map");
        CompoundTag map = (CompoundTag) mapField;
        check(map.keySet().size() == TOOLBOX.size(), "Toolbox item map is not the four stacks");
        Map<Integer, String> found = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            int slot;
            try { slot = Integer.parseInt(key); }
            catch (NumberFormatException error) { check(false, "Toolbox item key is not an integer: " + key); return found; }
            Tag stackField = map.get(key);
            check(stackField instanceof CompoundTag, "Toolbox stack " + key + " is not a compound");
            CompoundTag stack = (CompoundTag) stackField;
            check(!stack.contains("slot") && !stack.contains("Slot"), "Toolbox stack " + key + " still carries a slot field");
            found.put(slot, stack.getStringOr("id", "") + "/" + stack.getIntOr("count", -1));
        }
        List<Tag> compartments = inventory.getListOrEmpty("Compartments");
        check(compartments.size() == FILTERS.length, "Toolbox compartments are not the eight filters");
        for (int i = 0; i < FILTERS.length; i++) {
            check(compartments.get(i) instanceof CompoundTag, "Toolbox filter " + i + " is not a compound");
            CompoundTag filter = (CompoundTag) compartments.get(i);
            String actual = filter.isEmpty() ? "" : filter.getStringOr("id", "") + "/" + filter.getIntOr("count", -1);
            check(actual.equals(FILTERS[i]), "Toolbox filter " + i + " differs: " + actual);
        }
        Tag unique = saved.get("UniqueId");
        check(unique instanceof IntArrayTag, "Toolbox UniqueId is not four ints");
        check(Arrays.equals(((IntArrayTag) unique).getAsIntArray(), UNIQUE_ID), "Toolbox UniqueId differs");
        return found;
    }

    private static CompoundTag saveEntity(MinecraftServer server, Entity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(entity.getType().toString()), LoggerFactory.getLogger(MinerOutpostContracts.class))) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, server.registryAccess());
            entity.saveWithoutId(output);
            check(reporter.isEmpty(), "Entity save reported field loss");
            return output.buildResult();
        }
    }

    private static int number(CompoundTag tag, String key) {
        if (!tag.contains(key)) return 0;
        Tag value = tag.get(key);
        return value instanceof NumericTag numeric ? numeric.intValue() : -1;
    }

    private static BlockState resolve(CompoundTag raw, JsonObject row, JsonArray diagnostics) {
        String text = raw.getStringOr("id", "");
        row.addProperty("id", text);
        if (text.isEmpty() || raw.contains("Name") || raw.contains("Properties")) { diagnostics.add(note(text, "missing id or legacy Name/Properties; quarantined, not air")); row.addProperty("state", "null"); return null; }
        Identifier id;
        try { id = Identifier.parse(text); }
        catch (RuntimeException error) { diagnostics.add(note(text, error.toString())); row.addProperty("state", "null"); return null; }
        if (!BuiltInRegistries.BLOCK.containsKey(id)) { diagnostics.add(note(text, "block id absent from the live registry; quarantined, not air")); row.addProperty("state", "null"); return null; }
        var block = BuiltInRegistries.BLOCK.getValue(id);
        BlockState state = block.defaultBlockState();
        CompoundTag props = raw.getCompoundOrEmpty("properties");
        for (String key : props.keySet()) {
            String value = props.getStringOr(key, "");
            Property<?> property = block.getStateDefinition().getProperty(key);
            BlockState next = property == null ? null : apply(state, property, value);
            if (next == null) { diagnostics.add(note(text + "." + key, "property absent or value " + value + " is not in the live schema; quarantined, not air")); row.addProperty("state", "null"); return null; }
            state = next;
        }
        row.addProperty("state", "resolved");
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState apply(BlockState state, Property property, String value) {
        var parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : null;
    }

    private static void guard(MinecraftServer server) throws IOException {
        check(Boolean.getBoolean("valen.structure.probe.minerOutpost") && Boolean.getBoolean("valen.station.probe") && server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath()) && Files.exists(FIXTURE.resolve("../preparation.json")), "Refusing miner preflight outside the staged fixture");
        check(!Boolean.getBoolean("valen.industrial.probe") && !Boolean.getBoolean("valen.desertComplex.probe") && !Boolean.getBoolean("valen.structure.probe.bunkerRooms"), "Miner preflight stays exclusive of industrial, desert, and bunker");
    }

    private static Entity nearest(List<Entity> present, String id, Vec3 pos) {
        Entity found = null;
        double best = 0.05 * 0.05;
        for (Entity entity : present) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(id)) continue;
            double dx = entity.getX() - pos.x, dy = entity.getY() - pos.y, dz = entity.getZ() - pos.z, distance = dx * dx + dy * dy + dz * dz;
            if (distance <= best) { best = distance; found = entity; }
        }
        return found;
    }

    private static boolean sameBox(AABB saved, AABB authored) {
        return saved.minX == authored.minX && saved.minY == authored.minY && saved.minZ == authored.minZ && saved.maxX == authored.maxX && saved.maxY == authored.maxY && saved.maxZ == authored.maxZ;
    }

    private static JsonArray xyz(double x, double y, double z) { JsonArray array = new JsonArray(); array.add(x); array.add(y); array.add(z); return array; }
    private static JsonArray box(AABB box) { JsonArray array = new JsonArray(); array.add(box.minX); array.add(box.minY); array.add(box.minZ); array.add(box.maxX); array.add(box.maxY); array.add(box.maxZ); return array; }
    private static JsonObject note(String subject, String problem) { JsonObject row = new JsonObject(); row.addProperty("subject", subject); row.addProperty("problem", problem); row.addProperty("state", "null"); row.addProperty("air_substitution", false); return row; }
    private static void fail(JsonArray failures, String subject, String problem) { JsonObject row = new JsonObject(); row.addProperty("subject", subject); row.addProperty("problem", problem); failures.add(row); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static byte[] read(Path path) { try { return Files.readAllBytes(path); } catch (IOException error) { throw new IllegalStateException(error); } }
    private static byte[] resourceBytes(MinecraftServer server, Identifier resource) { try (var in = server.getResourceManager().getResource(resource).orElseThrow(() -> new AssertionError("Missing resource " + resource)).open()) { return in.readAllBytes(); } catch (IOException error) { throw new IllegalStateException(error); } }
    private static CompoundTag readNbt(byte[] bytes) { try (var in = new ByteArrayInputStream(bytes)) { return NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()); } catch (IOException error) { throw new IllegalStateException(error); } }
    private static String sha(byte[] data) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
}
