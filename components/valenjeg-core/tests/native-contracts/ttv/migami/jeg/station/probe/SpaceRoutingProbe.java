package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import net.minecraft.core.Holder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

/** Fixture-only qualification for the two space layouts. It selects the installed dimension and does not rewrite overworld builds. */
public final class SpaceRoutingProbe {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FIXTURE = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
    private static final Path INDEX = Path.of(System.getProperty("valenjeg.evidence.43229ed11454", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index-39.json"));
    private static final String INDEX_SHA = "e6c1ff9a685a9b8f04e8b9dc6c84eedb33f0137c52bfab3879fa8c05d2966cf0";
    private static final Path R35 = Path.of(System.getProperty("valenjeg.evidence.f0fb38c54d87", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-bunker-probe.json"));
    private static final String R35_SHA = "cc99a6f8739b7a8e762689d4cadbe0a7c8edc4f225eb31beac4ac1db9174f5bf";
    private static final Path R36_RAW = Path.of(System.getProperty("valenjeg.evidence.1eb200680930", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-bunker-raw-preflight.json"));
    private static final String R36_RAW_SHA = "0fa3d4022b045870758e32c0d4fadaeb08224f3f27b6587e5bc005526f155ade";
    private static final Path R36_READBACK = Path.of(System.getProperty("valenjeg.evidence.36df2498e856", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-bunker-readback-only.json"));
    private static final String R36_READBACK_SHA = "84f0d711decc441a5cc75d1aa6c723bde29af05db9e598265c922e2ed4edcdfc";
    private static final long WORLD_SEED = -4056840922791553232L;
    private static final int ORBIT_Y = 128;
    private static final Set<String> OVERWORLD_REJECTED = Set.of("minecraft:plains", "minecraft:desert", "minecraft:savanna", "minecraft:snowy_plains");
    private static boolean passed;

    private SpaceRoutingProbe() {}

    public static void register() {
        if (moon() || orbit()) ServerLifecycleEvents.SERVER_STARTED.register(SpaceRoutingProbe::preflight);
    }

    public static void run(MinecraftServer server) {
        check(passed, "Space routing preflight has not passed");
        Target target = target();
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(target.dimension())));
        check(level != null, "Target dimension is not loaded: " + target.dimension());
        check(!level.dimension().identifier().toString().equals("minecraft:overworld"), "Refusing to qualify space layouts in the overworld");
        check(level.dimension().identifier().toString().equals(target.dimension()), "Selected level is not the installed target dimension");
        Job job = new Job(level, target);
        ServerTickEvents.END_SERVER_TICK.register(tick -> { if (!job.done) job.step(); });
    }

    private static void preflight(MinecraftServer server) {
        JsonObject report = new JsonObject();
        JsonArray failures = new JsonArray();
        report.add("strict_failures", failures);
        report.addProperty("strict_required", 0);
        report.addProperty("pending_accepted", false);
        boolean guarded = false;
        try {
            check(Boolean.getBoolean("valen.station.probe") && Boolean.getBoolean("valen.structure.probe") && server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath()), "Refusing space routing outside the staged fixture");
            check(!(moon() && orbit()), "Moon and orbit qualification run one dimension at a time");
            guarded = true;
            Target target = target();
            report.addProperty("target", target.structure());
            report.addProperty("dimension", target.dimension());
            long seed = server.overworld().getSeed();
            report.addProperty("world_seed", seed);
            if (seed != WORLD_SEED) fail(failures, "world_seed", "fixture seed differs");
            verifyFrozen(report, failures);
            var lookup = server.registryAccess();
            var key = ResourceKey.create(Registries.STRUCTURE, Identifier.parse(target.structure()));
            var structure = lookup.lookupOrThrow(Registries.STRUCTURE).getOrThrow(key).value();
            if (!(structure instanceof JigsawStructure jigsaw)) fail(failures, "structure", "not a jigsaw");
            else {
                JsonObject encoded = JigsawStructure.CODEC.codec().encodeStart(net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, lookup), jigsaw).getOrThrow().getAsJsonObject();
                report.add("live_structure", encoded);
                List<String> biomes = new ArrayList<>();
                for (var biome : structure.biomes()) biomes.add(biome.unwrapKey().map(k -> k.identifier().toString()).orElse("unkeyed"));
                report.add("live_biomes", GSON.toJsonTree(biomes));
                if (biomes.size() != 1 || !biomes.getFirst().equals(target.biome())) fail(failures, "biomes", "live biomes are not only " + target.biome());
                for (String overworld : OVERWORLD_REJECTED) if (biomes.contains(overworld)) fail(failures, "biomes", "overworld biome still bound: " + overworld);
                if (target.orbit()) {
                    if (encoded.has("project_start_to_heightmap")) fail(failures, "height", "orbit station still projects a heightmap");
                    if (!encoded.has("start_height") || encoded.getAsJsonObject("start_height").get("absolute").getAsInt() != ORBIT_Y) fail(failures, "height", "orbit absolute Y is not 128");
                    if (!structure.terrainAdaptation().getSerializedName().equals("none")) fail(failures, "terrain", "orbit terrain adaptation is not none");
                    if (encoded.get("size").getAsInt() != 0) fail(failures, "size", "station pool size changed");
                } else if (!encoded.has("project_start_to_heightmap") || !encoded.get("project_start_to_heightmap").getAsString().equals("WORLD_SURFACE_WG") || encoded.get("size").getAsInt() != 20) {
                    fail(failures, "moon", "moon dungeon heightmap or jigsaw size changed");
                }
            }
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(target.dimension())));
            if (level == null) fail(failures, "dimension", "installed dimension is not loaded");
            else inspectGenerator(level, target, report, failures);
            boolean strict = failures.isEmpty();
            report.addProperty("status", strict ? "pass" : "fail");
            passed = strict;
            if (!strict) throw new IllegalStateException("Space routing preflight fail");
        } catch (Throwable error) {
            if (!report.has("status")) report.addProperty("status", "fail");
            report.addProperty("error", error.toString());
            passed = false;
            if (guarded) throw new IllegalStateException("Space routing preflight failed", error);
            throw error instanceof RuntimeException runtime ? runtime : new IllegalStateException(error);
        } finally {
            if (guarded) try { Files.writeString(server.getServerDirectory().resolve("valen-space-routing-raw-preflight.json"), GSON.toJson(report) + "\n"); }
            catch (IOException error) { throw new IllegalStateException(error); }
        }
    }

    private static void inspectGenerator(ServerLevel level, Target target, JsonObject report, JsonArray failures) {
        report.addProperty("generator", level.getChunkSource().getGenerator().getClass().getName());
        report.addProperty("min_y", level.getMinY());
        report.addProperty("height", level.getHeight());
        report.addProperty("sea_level", level.getSeaLevel());
        if (target.orbit()) {
            if (!(level.getChunkSource().getGenerator() instanceof FlatLevelSource flat)) { fail(failures, "generator", "earth orbit is not the installed flat generator"); return; }
            var settings = flat.settings();
            String biome = settings.getBiome().unwrapKey().map(k -> k.identifier().toString()).orElse("");
            report.addProperty("flat_biome", biome);
            report.addProperty("flat_layers", settings.getLayers().size());
            if (!biome.equals(target.biome()) || !settings.getLayers().isEmpty()) fail(failures, "generator", "orbit biome or layers differ from the installed void");
            var overrides = settings.structureOverrides();
            JsonArray sets = new JsonArray();
            if (overrides.isEmpty()) fail(failures, "structure_overrides", "absent overrides are not the installed empty list");
            else {
                for (var set : overrides.get()) sets.add(set.unwrapKey().map(k -> k.identifier().toString()).orElse("unkeyed"));
                report.add("structure_overrides", sets);
                if (sets.size() != 1 || !sets.get(0).getAsString().equals(target.structure())) fail(failures, "structure_overrides", "orbit must allow only the station structure set; an empty list disables native structures");
            }
            if (level.getMinY() != 0 || level.getHeight() != 384) fail(failures, "vertical", "orbit vertical range is not min_y 0 height 384");
        } else {
            if (!(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator noise)) { fail(failures, "generator", "moon is not the installed noise generator"); return; }
            if (level.getMinY() != 0 || level.getHeight() != 256 || level.getSeaLevel() != 0) fail(failures, "vertical", "moon vertical range or sea level differs");
            report.addProperty("noise_settings", noise.generatorSettings().unwrapKey().map(k -> k.identifier().toString()).orElse("unkeyed"));
            if (!report.get("noise_settings").getAsString().equals("spacereloaded:moon")) fail(failures, "noise", "moon noise settings differ");
        }
    }

    private static void verifyFrozen(JsonObject report, JsonArray failures) {
        if (!sha(read(INDEX)).equals(INDEX_SHA)) fail(failures, "index39", "frozen index hash differs");
        JsonObject index = JsonParser.parseString(new String(read(INDEX), StandardCharsets.UTF_8)).getAsJsonObject();
        if (index.get("entry_count").getAsInt() != 39 || index.get("world_seed").getAsLong() != WORLD_SEED || index.get("forced_starts").getAsBoolean() || index.get("old_rows_replayed").getAsBoolean()) fail(failures, "index39", "header differs");
        report.addProperty("index_39_sha256", INDEX_SHA);
        report.addProperty("index_39_rows", index.getAsJsonArray("sites").size());
        if (!sha(read(R35)).equals(R35_SHA)) fail(failures, "r35", "large shelter receipt hash differs");
        if (!sha(read(R36_RAW)).equals(R36_RAW_SHA) || !sha(read(R36_READBACK)).equals(R36_READBACK_SHA)) fail(failures, "r36", "large shelter raw or readback hash differs");
        JsonObject r35 = JsonParser.parseString(new String(read(R35), StandardCharsets.UTF_8)).getAsJsonObject();
        if (!r35.get("id").getAsString().equals("jeg:valen_minipurge/underground_shelter") || r35.get("start_chunk_x").getAsInt() != 23999 || r35.get("start_chunk_z").getAsInt() != 38090) fail(failures, "r35", "shelter identity differs");
        JsonObject r36 = JsonParser.parseString(new String(read(R36_READBACK), StandardCharsets.UTF_8)).getAsJsonObject();
        if (r36.get("generation").getAsBoolean() || r36.get("inventory_replay").getAsBoolean()) fail(failures, "r36", "readback is not the no-generation receipt");
        report.addProperty("r35_sha256", R35_SHA);
        report.addProperty("r36_raw_sha256", R36_RAW_SHA);
        report.addProperty("r36_readback_sha256", R36_READBACK_SHA);
        report.addProperty("shelter_or_overworld_replayed", false);
    }

    private static final class Job {
        final ServerLevel level;
        final Target target;
        final JsonObject receipt = new JsonObject();
        final JsonArray trials = new JsonArray();
        final Set<Long> full = new HashSet<>();
        Holder.Reference<Structure> holder;
        Structure structure;
        StructureStart start;
        BoundingBox bounds;
        List<ChunkPos> chunks = List.of();
        int attempt, eligible, chunkIndex, ticks, generated;
        boolean done;
        String phase = "select";

        Job(ServerLevel level, Target target) {
            this.level = level;
            this.target = target;
            receipt.addProperty("id", target.structure());
            receipt.addProperty("dimension", level.dimension().identifier().toString());
            receipt.addProperty("world_seed", level.getSeed());
            receipt.addProperty("forced_fake_starts", false);
            receipt.addProperty("overworld_builds_moved", false);
            receipt.addProperty("old_inventory_replayed", false);
            receipt.addProperty("search_grid_slot", target.grid());
            receipt.addProperty("max_eligible", 64);
            receipt.addProperty("max_new_full", 384);
            receipt.add("candidate_diagnostics", trials);
            var all = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().filter(h -> h.key().identifier().toString().equals(target.structure())).toList();
            check(all.size() == 1, "Expected one registered target");
            holder = all.getFirst();
            structure = holder.value();
            save();
        }

        void step() {
            try {
                check(++ticks <= 18000, "Space routing probe exceeded 18000 server ticks");
                switch (phase) {
                    case "select" -> phase = "candidate";
                    case "candidate" -> find();
                    case "place" -> {
                        if (chunkIndex == chunks.size()) { chunkIndex = 0; phase = "read"; return; }
                        ChunkPos cp = chunks.get(chunkIndex++);
                        level.getChunk(cp.x(), cp.z());
                        full.add(ChunkPos.pack(cp.x(), cp.z()));
                    }
                    case "read" -> read();
                    default -> throw new IllegalStateException(phase);
                }
            } catch (Throwable error) {
                done = true;
                receipt.addProperty("status", "fail");
                receipt.addProperty("error", error.toString());
                save();
            }
        }

        void find() {
            if (attempt >= 384 || eligible >= 64) { finish("pending: no eligible native start within bounded candidate search"); return; }
            var source = level.getChunkSource();
            var state = source.getGeneratorState();
            int gridAttempt = ++attempt;
            for (var placement : state.getPlacementsForStructure(holder)) {
                if (!(placement instanceof RandomSpreadStructurePlacement spread)) continue;
                ChunkPos cp = spread.getPotentialStructureChunk(level.getSeed(), 1024 + target.grid() * 512 + (gridAttempt - 1) * 256, 1024 + target.grid() * 768 + (gridAttempt - 1) * 512);
                JsonObject trial = new JsonObject();
                trial.addProperty("grid_attempt", gridAttempt);
                trial.addProperty("chunk_x", cp.x());
                trial.addProperty("chunk_z", cp.z());
                trials.add(trial);
                if (!placement.isStructureChunk(state, cp.x(), cp.z())) { trial.addProperty("result", "native_placement_reject"); save(); continue; }
                eligible++;
                Map<String, Integer> biomes = new TreeMap<>();
                var generator = source.getGenerator();
                var context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(), source.randomState().createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build()), source.randomState(), level.getStructureTemplateManager(), level.getSeed(), cp, level, biome -> {
                    String id = biome.unwrapKey().map(k -> k.identifier().toString()).orElse("unkeyed");
                    biomes.merge(id, 1, Integer::sum);
                    check(!OVERWORLD_REJECTED.contains(id), "Overworld biome entered the space qualification: " + id);
                    return structure.biomes().contains(biome) && id.equals(target.biome());
                });
                trial.add("native_biome_checks", GSON.toJsonTree(biomes));
                if (structure.findValidGenerationPoint(context).isEmpty()) { trial.addProperty("result", "biome_or_height_reject"); save(); continue; }
                ChunkAccess starts = source.getChunk(cp.x(), cp.z(), ChunkStatus.STRUCTURE_STARTS, true);
                check(starts != null, "Native candidate chunk missing");
                StructureStart found = starts.getStartForStructure(structure);
                if (found == null || !found.isValid()) { trial.addProperty("result", "target_absent_after_native_structure_set_arbitration"); save(); continue; }
                trial.addProperty("result", "actual_native_start_valid");
                start = found;
                bounds = found.getBoundingBox();
                check(bounds.minY() >= level.getMinY() && bounds.maxY() < level.getMinY() + level.getHeight(), "Start is outside the dimension vertical range");
                if (target.orbit()) check(bounds.minY() >= 96 && bounds.maxY() <= 191, "Station did not float in the orbit band; void heightmap projection is rejected");
                chunks = new ArrayList<>();
                int additional = 0;
                for (int x = Math.floorDiv(bounds.minX(), 16); x <= Math.floorDiv(bounds.maxX(), 16); x++) for (int z = Math.floorDiv(bounds.minZ(), 16); z <= Math.floorDiv(bounds.maxZ(), 16); z++) {
                    chunks.add(new ChunkPos(x, z));
                    if (!full.contains(ChunkPos.pack(x, z))) additional++;
                }
                if (full.size() + additional > 384) { finish("pending: native start valid, placement chunk budget reached"); return; }
                receipt.add("bounding_box", box(bounds));
                receipt.addProperty("piece_count", start.getPieces().size());
                receipt.addProperty("start_chunk_x", cp.x());
                receipt.addProperty("start_chunk_z", cp.z());
                phase = "place";
                save();
                return;
            }
            save();
        }

        void read() {
            if (chunkIndex != chunks.size()) { chunkIndex++; return; }
            var piece = start.getPieces().getFirst();
            check(piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single, "Start piece is not a template piece");
            String template = ((SinglePoolElement) ((PoolElementStructurePiece) piece).getElement()).getTemplateLocation().toString();
            receipt.addProperty("start_template", template);
            if (target.orbit()) check(start.getPieces().size() == 1 && template.equals(target.structure()), "Orbit station is not the one authored template");
            else check(template.equals("jeg:valen_ad_astra/dungeon/moon/entrance"), "Moon start is not the authored entrance");
            BlockPos center = new BlockPos((bounds.minX() + bounds.maxX()) / 2, Math.max(level.getMinY(), Math.min(bounds.minY(), level.getMaxY() - 1)), (bounds.minZ() + bounds.maxZ()) / 2);
            var chunk = level.getChunk(center.getX() >> 4, center.getZ() >> 4);
            String biome = chunk.getNoiseBiome(center.getX() >> 2, center.getY() >> 2, center.getZ() >> 2).unwrapKey().map(k -> k.identifier().toString()).orElse("unkeyed");
            receipt.addProperty("placed_biome", biome);
            check(biome.equals(target.biome()), "Placed biome is not the installed dimension biome");
            int surface = level.getChunkSource().getGenerator().getBaseHeight(center.getX(), center.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState());
            receipt.addProperty("unadapted_surface_y", surface);
            receipt.addProperty("new_full_chunks", full.size());
            receipt.addProperty("placement_eligible_candidates", eligible);
            generated++;
            finish("actual native start and full-chunk placement read back");
        }

        void finish(String status) {
            receipt.addProperty("status", status);
            receipt.addProperty("generated_structure_count", generated);
            receipt.addProperty("photos", "not taken by this fixture; root photographs the real placed build");
            done = true;
            save();
        }

        void save() { try { Files.writeString(level.getServer().getServerDirectory().resolve(target.report()), GSON.toJson(receipt) + "\n"); } catch (IOException error) { throw new IllegalStateException(error); } }
    }

    private record Target(String structure, String dimension, String biome, int grid, boolean orbit, String report) {}

    private static Target target() {
        if (orbit()) return new Target("jeg:valen_ad_astra/space_station", "spacereloaded:earth_orbit", "spacereloaded:orbit_void", 42, true, "valen-structure-probe-space-orbit.json");
        return new Target("jeg:valen_ad_astra/moon_dungeon", "spacereloaded:moon", "spacereloaded:moon_plains", 41, false, "valen-structure-probe-space-moon.json");
    }

    private static boolean moon() { return Boolean.getBoolean("valen.structure.probe.spaceRoutingMoon"); }
    private static boolean orbit() { return Boolean.getBoolean("valen.structure.probe.spaceRoutingOrbit"); }
    private static JsonArray box(BoundingBox bounds) { JsonArray array = new JsonArray(); for (int value : new int[]{bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ()}) array.add(value); return array; }
    private static void fail(JsonArray failures, String subject, String problem) { JsonObject row = new JsonObject(); row.addProperty("subject", subject); row.addProperty("problem", problem); failures.add(row); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static byte[] read(Path path) { try { return Files.readAllBytes(path); } catch (IOException error) { throw new IllegalStateException(error); } }
    private static String sha(byte[] data) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); } catch (Exception error) { throw new IllegalStateException(error); } }
}
