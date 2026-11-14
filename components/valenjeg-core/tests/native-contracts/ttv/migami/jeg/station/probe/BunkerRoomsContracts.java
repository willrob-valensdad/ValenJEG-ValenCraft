package ttv.migami.jeg.station.probe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import ttv.migami.jeg.structure.mesh.HbmQuarterTurn;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly;
import ttv.migami.jeg.worldgen.structure.BunkerRoomPainter;
import ttv.migami.jeg.worldgen.structure.BunkerRoomPiece;
import ttv.migami.jeg.worldgen.structure.BunkerRoomsStructure;

/**
 * Native bunker proof. The painter hash stays unbound until root records the
 * canonical file. Loot is saved metadata and getLootTable only.
 */
public final class BunkerRoomsContracts {
    public static final String INTEGRATED_PAINTER_SHA = "1965b339269bb1566b75320341fa112cbf175bcdc2b2a74831c470b336f54744";
    public static final String ASSEMBLY_CONTRACT_SHA = "ad04587327fb490e81f6bb169e837fcafb2fe55ba389f858c95dfdbe525b793c";
    public static final String ROOMS_CONTRACT_SHA = "eb0efc4354ed285d0ec4f7730aed1638b40b9c5a7d5f49b54b7392b8595c2577";
    public static final String SOURCE_REFERENCE_SHA = "5d37a1b48e7a26e06658b8d824b6459906c801d0d62c9972ca2a592dd35a816b";
    public static final int REFERENCE_SURFACE_Y = 80;
    private static final Path ASSEMBLY = Path.of(System.getProperty("valenjeg.coreSourceRoot", "VALENJEG_CORE_SOURCE_NOT_CONFIGURED") + "/structures/hbm-bunker-rooms/bunker-assembly-contract.json");
    private static final Path ROOMS = Path.of(System.getProperty("valenjeg.coreSourceRoot", "VALENJEG_CORE_SOURCE_NOT_CONFIGURED") + "/structures/hbm-bunker-rooms/bunker-rooms-contract.json");
    private static final Path REFERENCE = Path.of(System.getProperty("valenjeg.coreSourceRoot", "VALENJEG_CORE_SOURCE_NOT_CONFIGURED") + "/structures/hbm-bunker-rooms/source-reference-hbm-geometry.json");
    private static final Path PAINTER = Path.of(System.getProperty("valenjeg.coreSourceRoot", "VALENJEG_CORE_SOURCE_NOT_CONFIGURED") + "/src/main/java/ttv/migami/jeg/worldgen/structure/BunkerRoomPainter.java");
    private static final int[][] RESERVED = {
        {327748, 50, 491476, 327788, 83, 491518},
        {319329, 51, 471027, 319372, 102, 471068},
        {377064, 55, 593960, 377096, 80, 593992},
        {383992, 76, 609416, 384024, 101, 609448},
        {450610, -64, 782486, 450652, 319, 782524},
        {218806, 114, 358916, 218844, 148, 358958},
        {271660, 54, 402115, 271724, 102, 402172},
        {268052, 46, 404188, 268109, 94, 404252},
        {919468, 59, 1706947, 919532, 107, 1707004},
        {336001, -36, 496101, 336083, 64, 496147},
        {341064, -11, 504069, 341099, 87, 504138}
    };
    private static final String[] PROPS = {
        "hbm:barrel_corroded",
        "hbm:barrel_plastic",
        "hbm:block_copper",
        "hbm:block_lead",
        "hbm:brick_concrete",
        "hbm:brick_concrete_broken",
        "hbm:brick_concrete_cracked",
        "hbm:brick_concrete_mossy",
        "hbm:cable_detector",
        "hbm:cable_diode",
        "hbm:cable_switch",
        "hbm:capacitor_copper",
        "hbm:concrete_lime",
        "hbm:concrete_pillar",
        "hbm:concrete_slab",
        "hbm:concrete_smooth_stairs",
        "hbm:deco_beryllium",
        "hbm:deco_computer",
        "hbm:deco_lead",
        "hbm:deco_pipe_framed_rusted",
        "hbm:deco_pipe_quad_rusted",
        "hbm:deco_pipe_rim",
        "hbm:deco_pipe_rim_rusted",
        "hbm:deco_red_copper",
        "hbm:deco_steel",
        "hbm:deco_tungsten",
        "hbm:door_bunker",
        "hbm:fan",
        "hbm:filing_cabinet_green",
        "hbm:fluid_duct_gauge",
        "hbm:ladder_sturdy",
        "hbm:machine_condenser",
        "hbm:machine_diesel",
        "hbm:machine_electric_furnace",
        "hbm:machine_microwave",
        "hbm:machine_transformer",
        "hbm:pwr_channel",
        "hbm:pwr_control",
        "hbm:pwr_fuel",
        "hbm:radiorec",
        "hbm:reinforced_glass",
        "hbm:reinforced_lamp",
        "hbm:reinforced_stone",
        "hbm:steel_beam",
        "hbm:steel_corner",
        "hbm:steel_grate",
        "hbm:steel_scaffold",
        "hbm:steel_wall",
        "hbm:tape_recorder",
        "hbm:trapdoor_steel",
        "hbm:vinyl_tile_large",
        "hbm:vinyl_tile_small",
        "minecraft:chest"
    };    private static boolean passed;
    private static int referenceMismatches;
    private static final Map<String, Integer> referenceErrors = new java.util.LinkedHashMap<>();

    private BunkerRoomsContracts() {}

    public static void register() {
        if (Boolean.getBoolean("valen.structure.probe.bunkerRooms")) ServerLifecycleEvents.SERVER_STARTED.register(BunkerRoomsContracts::preflight);
    }

    public static void requirePassed() {
        check(passed, "Bunker rooms preflight has not passed");
    }

    public static boolean reservedChunk(int x, int z) {
        if (x >= 28163 && x <= 28165 && z >= 48905 && z <= 48907) return true;
        if (x >= 57466 && x <= 57470 && z >= 106684 && z <= 106687) return true;
        if (x >= 21000 && x <= 21005 && z >= 31006 && z <= 31009) return true;
        if (x >= 21316 && x <= 21318 && z >= 31504 && z <= 31508) return true;
        return x == 13677 && z == 22433 || x == 16982 && z == 25135 || x == 16754 && z == 25265 || x == 20485 && z == 30718 || x == 19960 && z == 29441 || x == 23568 && z == 37124 || x == 23999 && z == 38090;
    }

    public static boolean reserved(BoundingBox box) {
        for (int[] row : RESERVED) if (box.intersects(row[0], row[1], row[2], row[3], row[4], row[5])) return true;
        return false;
    }

    public static void preflight(MinecraftServer server) {
        check(!"UNBOUND".equals(INTEGRATED_PAINTER_SHA), "Integrated BunkerRoomPainter hash is UNBOUND. Root sets INTEGRATED_PAINTER_SHA to the canonical painter file before launch. The check is not skipped.");
        check(sha(PAINTER).equals(INTEGRATED_PAINTER_SHA), "Canonical painter file hash differs");
        check(sha(ASSEMBLY).equals(ASSEMBLY_CONTRACT_SHA), "Assembly contract hash differs");
        check(sha(ROOMS).equals(ROOMS_CONTRACT_SHA), "Rooms contract hash differs");
        check(sha(REFERENCE).equals(SOURCE_REFERENCE_SHA), "Independent source-reference geometry hash differs");
        check(BunkerRoomsStructure.TYPE != null && BunkerRoomPiece.TYPE != null, "Bunker structure or piece type is missing");
        check(BunkerRoomPainter.HATCH_STATEMENTS == 14, "Producer hatch statement count is not the 14 source calls");
        auditCoordMode();
        var assembly = JsonParser.parseString(read(ASSEMBLY)).getAsJsonObject();
        var rooms = JsonParser.parseString(read(ROOMS)).getAsJsonObject();
        var reference = JsonParser.parseString(read(REFERENCE)).getAsJsonObject();
        check(reference.get("hatch_statements").getAsInt() == 14 && reference.get("surface_y").getAsInt() == REFERENCE_SURFACE_Y, "Source reference is not the 14-call hatch at surface 80");
        for (var element : assembly.getAsJsonArray("graphs")) {
            var graph = element.getAsJsonObject();
            var actual = BunkerProceduralAssembly.assemble(new Random(graph.get("seed").getAsLong()), graph.getAsJsonArray("chunk").get(0).getAsInt(), graph.getAsJsonArray("chunk").get(1).getAsInt());
            check(actual.sizeLimit == graph.get("size_limit").getAsInt() && actual.pieces.size() == graph.get("piece_count").getAsInt(), "Assembly piece count differs");
            var expected = graph.getAsJsonArray("pieces");
            for (int i = 0; i < expected.size(); i++) comparePiece(actual.pieces.get(i), expected.get(i).getAsJsonObject());
        }
        var context = StructurePieceSerializationContext.fromLevel(server.overworld());
        for (var element : rooms.getAsJsonArray("graphs")) {
            var graph = element.getAsJsonObject();
            long seed = graph.get("seed").getAsLong();
            check(graph.get("total_cells").getAsInt() == (seed == 1L ? 5352 : 6168), "Omitted-hatch room contract totals changed");
            var actual = BunkerProceduralAssembly.assemble(new Random(seed), graph.getAsJsonArray("chunk").get(0).getAsInt(), graph.getAsJsonArray("chunk").get(1).getAsInt());
            var expectedRooms = graph.getAsJsonArray("rooms");
            check(actual.pieces.size() == expectedRooms.size(), "Painter graph length differs");
            for (int i = 0; i < actual.pieces.size(); i++) {
                var source = actual.pieces.get(i);
                var room = expectedRooms.get(i).getAsJsonObject();
                var flags = room.getAsJsonArray("paths");
                check(source.paths.length == flags.size(), "Exit flag width differs");
                for (int p = 0; p < flags.size(); p++) check(source.paths[p] == flags.get(p).getAsBoolean(), "exitPlaced path differs for " + room.get("kind").getAsString());
                var painted = BunkerRoomPainter.paint(source, seed, REFERENCE_SURFACE_Y);
                if (painted.isEmpty()) throw new AssertionError("Placeholder empty room " + room.get("kind").getAsString());
                for (var cell : painted) {
                    check(cell.state() != null, "Painter returned a null state");
                    check(BuiltInRegistries.BLOCK.getKey(cell.state().getBlock()) != null, "Unknown painter state");
                    turn(cell.state());
                }
                roundTrip(context, source, seed);
            }
        }
        for (var element : reference.getAsJsonArray("graphs")) compareReference(element.getAsJsonObject());
        check(referenceMismatches == 0, "Strict source-reference state mismatches=" + referenceMismatches + "; up to64distinct diagnostics=" + referenceErrors);
        for (String id : PROPS) check(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(id)), "Missing static prop " + id);
        var key = ResourceKey.create(Registries.STRUCTURE, Identifier.parse("jeg:bunker_rooms"));
        var holder = server.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(key);
        check(holder.isPresent(), "jeg:bunker_rooms is not registered");
        check(holder.get().value().step() == net.minecraft.world.level.levelgen.GenerationStep.Decoration.TOP_LAYER_MODIFICATION, "Loaded bunker step must follow underground, surface, ore and vegetation decoration");
        var placements = server.overworld().getChunkSource().getGeneratorState().getPlacementsForStructure(holder.get());
        check(placements.size() == 1 && placements.get(0) instanceof RandomSpreadStructurePlacement, "Bunker placement is not one random spread");
        var spread = (RandomSpreadStructurePlacement) placements.get(0);
        check(spread.spacing() == 128 && spread.separation() == 42, "Bunker placement is not random spread 128/42");
        JsonObject encoded = StructurePlacement.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), spread).getOrThrow().getAsJsonObject();
        check(encoded.get("spacing").getAsInt() == 128 && encoded.get("separation").getAsInt() == 42 && encoded.get("salt").getAsInt() == 1728102406, "Loaded bunker spacing, separation, or salt changed");
        float frequency = encoded.has("frequency") ? encoded.get("frequency").getAsFloat() : 1.0F;
        String reduction = encoded.has("frequency_reduction_method") ? encoded.get("frequency_reduction_method").getAsString() : "default";
        check("linear".equals(encoded.has("spread_type") ? encoded.get("spread_type").getAsString() : "linear") && frequency == 1.0F && "default".equals(reduction), "Loaded bunker spread or density changed");
        var zoneElement = encoded.get("exclusion_zone");
        check(zoneElement != null && zoneElement.isJsonObject(), "Loaded bunker placement has no exclusion zone");
        JsonObject zone = zoneElement.getAsJsonObject();
        check(zone.get("chunk_count").getAsInt() == 3 && "buildcraftenergy:water_spring".equals(zone.get("other_set").getAsString()), "Loaded bunker exclusion is not buildcraftenergy:water_spring at chunk_count 3");
        check(server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(ResourceKey.create(Registries.STRUCTURE_SET, Identifier.parse("buildcraftenergy:water_spring"))).isPresent(), "Friends pack registry has no buildcraftenergy:water_spring; the bunker exclusion reference cannot resolve");
        check(!spread.isStructureChunk(server.overworld().getChunkSource().getGeneratorState(), 21002, 31007), "Native placement predicate still allows the proven R46 spring collision chunk");
        check(BunkerRoomsStructure.SOURCE_HEIGHT_MARGIN == 20, "Height margin is not the source 20");
        JsonObject rawReceipt = new JsonObject();
        rawReceipt.addProperty("status", "pass");
        rawReceipt.addProperty("world_seed", server.overworld().getSeed());
        rawReceipt.addProperty("painter_sha256", INTEGRATED_PAINTER_SHA);
        rawReceipt.addProperty("assembly_contract_sha256", ASSEMBLY_CONTRACT_SHA);
        rawReceipt.addProperty("source_reference_sha256", SOURCE_REFERENCE_SHA);
        rawReceipt.addProperty("reference_cells", 5455 + 6271);
        rawReceipt.addProperty("all_reference_state_mismatches", referenceMismatches);
        rawReceipt.add("loaded_placement", encoded);
        rawReceipt.addProperty("loaded_step", holder.get().value().step().getSerializedName());
        rawReceipt.addProperty("world_placement_proven", false);
        rawReceipt.addProperty("old_loot_replayed", false);
        try { Files.writeString(Path.of("valen-bunker-rooms-raw-preflight.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rawReceipt)); }
        catch (Exception failure) { throw new IllegalStateException("Cannot preserve strict native raw receipt", failure); }
        passed = true;
    }

    public static JsonObject readPlaced(ServerLevel level, StructureStart start, String id) {
        check(id.equals("jeg:bunker_rooms"), "Bunker readback received another structure");
        check(!start.getPieces().isEmpty(), "Rejecting an empty generated bunker");
        var context = StructurePieceSerializationContext.fromLevel(level);
        int compared = 0, loot = 0, hubs = 0, mismatchCount = 0;
        JsonArray roomSummaries = new JsonArray();
        JsonArray mismatches = new JsonArray();
        for (StructurePiece raw : start.getPieces()) {
            check(raw instanceof BunkerRoomPiece, "Bunker start contains a piece that is not a source room");
            BunkerRoomPiece piece = (BunkerRoomPiece) raw;
            CompoundTag tag = piece.createTag(context);
            BunkerRoomPiece loaded = new BunkerRoomPiece(context, tag);
            check(loaded.getBoundingBox().equals(piece.getBoundingBox()), "Piece serialization did not restore the moved bounds");
            check(tag.contains("surface"), "Placed bunker room has no persisted surface");
            var source = sourcePiece(tag);
            check(sameBox(piece.roomBox(), source.box) && sameBox(loaded.roomBox(), source.box), "Serialized room box is not the source room");
            check(piece.getBoundingBox().minX() == source.box.minX && piece.getBoundingBox().minY() == source.box.minY && piece.getBoundingBox().minZ() == source.box.minZ, "Extended piece box moved the room origin");
            if (source.kind == BunkerProceduralAssembly.Kind.HUB) hubs++;
            int surfaceY = tag.getIntOr("surface", Integer.MIN_VALUE);
            var painted = BunkerRoomPainter.paint(source, tag.getLongOr("decor", Long.MIN_VALUE), surfaceY);
            JsonObject roomSummary = new JsonObject();
            roomSummary.addProperty("kind", source.kind.name());
            roomSummary.addProperty("coord_mode", source.coordMode);
            roomSummary.addProperty("surface_y", surfaceY);
            JsonArray roomBox = new JsonArray();
            for (int value : new int[]{source.box.minX,source.box.minY,source.box.minZ,source.box.maxX,source.box.maxY,source.box.maxZ}) roomBox.add(value);
            roomSummary.add("room_box", roomBox);
            if (source.kind == BunkerProceduralAssembly.Kind.HUB) {
                JsonArray hatch = new JsonArray();
                hatch.add(wx(source.coordMode,2,5,source.box.minX,source.box.maxX)); hatch.add(surfaceY); hatch.add(wz(source.coordMode,2,5,source.box.minZ,source.box.maxZ));
                roomSummary.add("source_hatch_world", hatch);
            }
            for (var candidate : painted) {
                BlockPos pos = candidate.pos();
                if (pos.getY()==source.box.minY+1 && candidate.state().isAir() && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.below()).getBlock()).toString().equals("jeg:hbm_vinyl_tile")) {
                    JsonArray stand = new JsonArray(); stand.add(pos.getX()+0.5); stand.add(pos.getY()); stand.add(pos.getZ()+0.5);
                    roomSummary.add("native_air_over_vinyl_stand", stand); break;
                }
            }
            roomSummaries.add(roomSummary);
            if (painted.isEmpty()) throw new AssertionError("Placed room painted no cells");
            for (var cell : painted) {
                check(piece.getBoundingBox().isInside(cell.pos()), "Extended piece box does not contain " + cell.pos());
                compared++;
                BlockState actual = level.getBlockState(cell.pos());
                if (!actual.equals(cell.state())) {
                    mismatchCount++;
                    if (mismatches.size() < 64) {
                        JsonObject row = new JsonObject();
                        JsonArray pos = new JsonArray();
                        pos.add(cell.pos().getX());
                        pos.add(cell.pos().getY());
                        pos.add(cell.pos().getZ());
                        row.add("pos", pos);
                        row.addProperty("expected", describe(cell.state()));
                        row.addProperty("actual", describe(actual));
                        mismatches.add(row);
                    }
                    continue;
                }
                if (cell.lootTable() == null) continue;
                BlockEntity entity = level.getBlockEntity(cell.pos());
                check(entity instanceof RandomizableContainer, "Packed loot block entity missing at " + cell.pos());
                var container = (RandomizableContainer) entity;
                var table = container.getLootTable();
                check(table != null && table.identifier().toString().equals(cell.lootTable()), "Packed loot table differs at " + cell.pos());
                check(entity.saveWithFullMetadata(level.registryAccess()).getStringOr("LootTable", "").equals(cell.lootTable()), "Saved loot metadata differs at " + cell.pos());
                loot++;
            }
        }
        check(hubs == 1 && compared > 0, "Bunker placement did not compare one hub and its source cells");
        JsonObject evidence = new JsonObject();
        evidence.add("rooms", roomSummaries);
        evidence.add("world_cell_mismatches", mismatches);
        evidence.addProperty("world_cell_mismatch_count", mismatchCount);
        check(mismatchCount == 0, "Placed bunker world cells differ from the painter: count=" + mismatchCount + " " + mismatches);
        evidence.addProperty("compared_source_cells", compared);
        evidence.addProperty("hub_hatch_source_calls", 14);
        evidence.addProperty("omitted_hatch_contract_cells", "5352/6168");
        evidence.addProperty("packed_loot", loot);
        evidence.addProperty("loot_unpacked", false);
        evidence.addProperty("container_get_item_called", false);
        evidence.addProperty("old_inventory_replayed", false);
        evidence.addProperty("ghoul_activation", false);
        return evidence;
    }

    private static void compareReference(JsonObject graph) {
        long seed = graph.get("seed").getAsLong();
        var actual = BunkerProceduralAssembly.assemble(new Random(seed), graph.getAsJsonArray("chunk").get(0).getAsInt(), graph.getAsJsonArray("chunk").get(1).getAsInt());
        var pieces = graph.getAsJsonArray("pieces");
        check(actual.pieces.size() == pieces.size(), "Reference piece count differs");
        int cells = 0, doors = 0;
        for (int i = 0; i < pieces.size(); i++) {
            var expected = pieces.get(i).getAsJsonObject();
            var source = actual.pieces.get(i);
            check(source.kind.name().equalsIgnoreCase(expected.get("kind").getAsString()), "Reference kind differs");
            var painted = BunkerRoomPainter.paint(source, seed, REFERENCE_SURFACE_Y);
            var geometry = expected.getAsJsonArray("geometry");
            check(painted.size() == geometry.size(), "Painter cell count differs from the source reference for " + expected.get("kind").getAsString());
            Map<BlockPos, BlockState> byPos = new HashMap<>();
            for (var cell : painted) {
                check(byPos.put(cell.pos(), cell.state()) == null, "Painter repeated a cell");
                if (cell.lootTable() != null) check(cell.lootTable().equals("jeg:chests/valen/field_supplies") || cell.lootTable().equals("jeg:chests/valen/research_cache"), "Unexpected loot table");
            }
            for (var element : geometry) {
                var row = element.getAsJsonObject();
                BlockPos pos = new BlockPos(row.get("x").getAsInt(), row.get("y").getAsInt(), row.get("z").getAsInt());
                BlockState state = byPos.get(pos);
                check(state != null, "Source reference cell missing from the painter at " + pos);
                try { expectState(state, row.get("state").getAsString()); }
                catch (AssertionError mismatch) {
                    referenceMismatches++;
                    String message = mismatch.getMessage();
                    if (referenceErrors.containsKey(message) || referenceErrors.size() < 64) referenceErrors.merge(message, 1, Integer::sum);
                }
            }
            cells += painted.size();
            doors += checkDoorHalves(geometry);
        }
        check(doors > 0, "Source reference placed no native bunker doors");
        check(cells == graph.get("total_cells").getAsInt() && cells > graph.get("omitted_hatch_total_cells").getAsInt(), "Hatch-inclusive total did not exceed the omitted-hatch contract");
    }

    private static int checkDoorHalves(com.google.gson.JsonArray geometry) {
        Map<String, String> halves = new HashMap<>();
        int found = 0;
        for (var element : geometry) {
            var row = element.getAsJsonObject();
            String encoded = row.get("state").getAsString();
            if (!encoded.startsWith("hbm:door_bunker[")) continue;
            found++;
            String body = encoded.substring(encoded.indexOf('[') + 1, encoded.length() - 1);
            String half = null, open = null, facing = null, hinge = null, powered = null;
            for (String field : body.split(",")) {
                int eq = field.indexOf('=');
                String name = field.substring(0, eq);
                String value = field.substring(eq + 1);
                switch (name) {
                    case "half" -> half = value;
                    case "open" -> open = value;
                    case "facing" -> facing = value;
                    case "hinge" -> hinge = value;
                    case "powered" -> powered = value;
                    default -> { }
                }
            }
            check(half != null && open != null && facing != null && hinge != null && powered != null, "Bunker door is missing facing, hinge, open, powered, or half");
            int lowerY = half.equals("upper") ? row.get("y").getAsInt() - 1 : row.get("y").getAsInt();
            String key = row.get("x").getAsInt() + "," + lowerY + "," + row.get("z").getAsInt();
            String signature = facing + "," + hinge + "," + open + "," + powered;
            check(halves.put(key + half, signature) == null, "Repeated bunker door half");
            String other = halves.get(key + (half.equals("lower") ? "upper" : "lower"));
            if (other != null) check(other.equals(signature), "Bunker door halves do not share facing, hinge, open, and powered");
        }
        if (found > 0) {
            for (var entry : halves.entrySet()) {
                String key = entry.getKey();
                String stem = key.endsWith("lower") ? key.substring(0, key.length() - 5) : key.substring(0, key.length() - 5);
                check(halves.containsKey(stem + "lower") && halves.containsKey(stem + "upper"), "Bunker door is missing one half");
            }
        }
        return found;
    }

    private static void expectState(BlockState state, String encoded) {
        int bracket = encoded.indexOf('[');
        String id = bracket < 0 ? encoded : encoded.substring(0, bracket);
        check(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(id), "Block id differs from the source reference: " + encoded);
        if (bracket < 0) return;
        for (String field : encoded.substring(bracket + 1, encoded.length() - 1).split(",")) {
            int eq = field.indexOf('=');
            String name = field.substring(0, eq);
            String value = field.substring(eq + 1);
            String actual = text(state, name);
            check(value.equals(actual), "Property " + name + " actual=" + actual + " differs for expected " + encoded);
        }
    }

    private static void auditCoordMode() {
        check(wx(0, 2, 3, 10, 20) == 12 && wz(0, 2, 3, 20, 30) == 23, "coordMode 0 is not the source south offset");
        check(wx(1, 2, 3, 10, 20) == 17 && wz(1, 2, 3, 20, 30) == 22, "coordMode 1 is not the source west offset");
        check(wx(2, 2, 3, 10, 20) == 18 && wz(2, 2, 3, 20, 30) == 27, "coordMode 2 is not the source north offset");
        check(wx(3, 2, 3, 10, 20) == 13 && wz(3, 2, 3, 20, 30) == 28, "coordMode 3 is not the source east offset");
        check(doorMeta(1, 0) == 1 && doorMeta(2, 0) == 2 && doorMeta(3, 0) == 3, "Door facing coordMode does not match placeDoor");
        check(stairMeta(1, 0) == 2 && stairMeta(3, 1) == 2, "Stair metadata does not match getStairMeta");
    }

    private static int wx(int mode, int x, int z, int minX, int maxX) {
        return switch (mode) { case 1 -> maxX - z; case 2 -> maxX - x; case 3 -> minX + z; default -> minX + x; };
    }

    private static int wz(int mode, int x, int z, int minZ, int maxZ) {
        return switch (mode) { case 1 -> minZ + x; case 2 -> maxZ - z; case 3 -> maxZ - x; default -> minZ + z; };
    }

    private static int doorMeta(int mode, int meta) {
        if (mode == 1) return Math.floorMod(meta + 1, 4);
        if (mode == 2) return meta ^ 2;
        if (mode == 3) return Math.floorMod(meta + 3, 4);
        return meta;
    }

    private static int stairMeta(int mode, int meta) {
        if (mode == 1) return (meta & 3) < 2 ? meta ^ 2 : meta ^ 3;
        if (mode == 2) return meta ^ 1;
        if (mode == 3) return (meta & 3) < 2 ? meta ^ 3 : meta ^ 2;
        return meta;
    }

    private static void roundTrip(StructurePieceSerializationContext context, BunkerProceduralAssembly.Piece source, long seed) {
        BunkerRoomPiece piece = new BunkerRoomPiece(source, seed, REFERENCE_SURFACE_Y);
        if (source.kind == BunkerProceduralAssembly.Kind.HUB) piece.includeHatch();
        CompoundTag tag = piece.createTag(context);
        BunkerRoomPiece loaded = new BunkerRoomPiece(context, tag);
        check(loaded.getBoundingBox().equals(piece.getBoundingBox()), "Piece serialization did not restore the extended bounds");
        check(tag.getIntOr("surface", Integer.MIN_VALUE) == REFERENCE_SURFACE_Y, "Piece tag dropped the persisted surface");
        var restored = sourcePiece(tag);
        check(sameBox(restored.box, source.box) && sameBox(loaded.roomBox(), source.box), "Piece tag replaced the source room with the extended box");
        check(piece.getBoundingBox().minX() == source.box.minX && piece.getBoundingBox().minY() == source.box.minY && piece.getBoundingBox().minZ() == source.box.minZ, "Hatch extension moved the room origin");
        check(restored.kind == source.kind && restored.coordMode == source.coordMode && restored.componentType == source.componentType, "Piece tag dropped kind, mode, or component");
        check(tag.getLongOr("decor", Long.MIN_VALUE) == seed, "Piece tag dropped decor");
        check(restored.paths.length == source.paths.length, "Piece tag dropped paths");
        for (int i = 0; i < source.paths.length; i++) check(restored.paths[i] == source.paths[i], "Piece tag changed an exit flag");
    }

    private static boolean sameBox(BunkerProceduralAssembly.Box left, BunkerProceduralAssembly.Box right) {
        return left.minX == right.minX && left.minY == right.minY && left.minZ == right.minZ && left.maxX == right.maxX && left.maxY == right.maxY && left.maxZ == right.maxZ;
    }

    private static BunkerProceduralAssembly.Piece sourcePiece(CompoundTag tag) {
        String kindName = tag.getStringOr("kind", "");
        int type = tag.getIntOr("component", -1);
        int mode = tag.getIntOr("mode", -1);
        int[] rolls = tag.getIntArray("rolls").orElse(null);
        int[] flags = tag.getIntArray("paths").orElse(null);
        int[] room = tag.getIntArray("room").orElse(null);
        check(!kindName.isEmpty() && type >= 0 && mode >= 0 && mode <= 3 && rolls != null && flags != null && room != null && room.length == 6, "Piece tag is missing kind, component, mode, rolls, paths, or room");
        boolean[] paths = new boolean[flags.length];
        for (int i = 0; i < flags.length; i++) paths[i] = flags[i] != 0;
        var box = new BunkerProceduralAssembly.Box(room[0], room[1], room[2], room[3], room[4], room[5]);
        return new BunkerProceduralAssembly.Piece(BunkerProceduralAssembly.Kind.valueOf(kindName.toUpperCase(Locale.ROOT)), type, mode, box, rolls, paths);
    }

    private static void comparePiece(BunkerProceduralAssembly.Piece piece, JsonObject expected) {
        check(piece.kind.name().equalsIgnoreCase(expected.get("kind").getAsString()), "Room kind differs");
        check(piece.coordMode == expected.get("mode").getAsInt(), "Room rotation mode differs");
        var box = expected.getAsJsonArray("box");
        check(piece.box.minX == box.get(0).getAsInt() && piece.box.minY == box.get(1).getAsInt() && piece.box.minZ == box.get(2).getAsInt() && piece.box.maxX == box.get(3).getAsInt() && piece.box.maxY == box.get(4).getAsInt() && piece.box.maxZ == box.get(5).getAsInt(), "Room box differs");
    }

    private static void turn(BlockState state) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        Integer meta = integer(state, "meta");
        Integer quarter = integer(state, "rotation");
        String hinge = text(state, "hinge");
        String facing = text(state, "facing");
        boolean hbmQuarter = id.startsWith("jeg:hbm_") && quarter != null && quarter >= 0 && quarter <= 3;
        for (Rotation rotation : Rotation.values()) {
            BlockState turned = state.rotate(rotation);
            check(integer(turned, "meta") == null ? meta == null : integer(turned, "meta").equals(meta), "Meta changed under " + rotation);
            if (hbmQuarter) check(integer(turned, "rotation") == HbmQuarterTurn.compose(quarter, rotation), "HBM quarter turn differs under " + rotation);
            if (hinge != null) check(hinge.equals(text(turned, "hinge")), "Door hinge changed under " + rotation);
            Direction face = facing == null ? null : direction(facing);
            if (face != null && face.getAxis().isHorizontal()) check(rotation.rotate(face).getSerializedName().equals(text(turned, "facing")), "Facing did not follow " + rotation);
        }
    }

    private static Integer integer(BlockState state, String name) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) return null;
        Object value = state.getValue(property);
        return value instanceof Integer number ? number : null;
    }

    private static String describe(BlockState state) {
        return state.toString();
    }

    private static String text(BlockState state, String name) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? null : read(property, state.getValue(property));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String read(Property<?> property, Object value) {
        return ((Property<T>) property).getName((T) value);
    }

    private static Direction direction(String name) {
        for (Direction direction : Direction.values()) if (direction.getSerializedName().equals(name)) return direction;
        return null;
    }

    private static String sha(Path path) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private static String read(Path path) {
        try { return Files.readString(path); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
