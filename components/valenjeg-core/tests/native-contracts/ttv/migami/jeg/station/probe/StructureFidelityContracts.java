package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.LoggerFactory;

/** Opt-in, fixture-only actual registry/codec audit. Never places blocks or loads chunks. */
public final class StructureFidelityContracts {
    static final Path FIXTURE = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED"));
    static final Path EVIDENCE = Path.of(System.getProperty("valenjeg.evidence.96ff2f85e9dc", "VALENJEG_EVIDENCE_NOT_CONFIGURED/structure-repair-r61"));
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private StructureFidelityContracts() {}
    public static void register() {
        if (Boolean.getBoolean("valen.structure.fidelity"))
            ServerLifecycleEvents.SERVER_STARTED.register(StructureFidelityContracts::audit);
    }
    static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    static JsonObject manifest() throws Exception {
        Path path = Path.of(System.getProperty("valen.structure.fidelity.manifest")).toRealPath();
        if (!path.startsWith(EVIDENCE.toRealPath())) throw new IllegalStateException("Manifest outside authorized repair evidence");
        byte[] bytes = Files.readAllBytes(path);
        if (!sha(bytes).equals(System.getProperty("valen.structure.fidelity.manifestSha")))
            throw new IllegalStateException("Exact frozen fidelity manifest hash required");
        return JsonParser.parseString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    static BlockState resolve(JsonObject raw) {
        Identifier id = Identifier.parse(raw.get("id").getAsString());
        if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalArgumentException("Unregistered block " + id);
        BlockState state = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        if (raw.has("properties")) for (var entry : raw.getAsJsonObject("properties").entrySet()) {
            Property property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) throw new IllegalArgumentException("Unknown property " + id + "." + entry.getKey());
            var value = property.getValue(entry.getValue().getAsString());
            if (value.isEmpty()) throw new IllegalArgumentException("Invalid property value " + id + "." + entry.getKey());
            state = state.setValue(property, (Comparable)value.get());
        }
        return state;
    }
    private static JsonObject rawState(CompoundTag raw) {
        if (raw.contains("Name") || raw.contains("Properties") || !raw.contains("id"))
            throw new IllegalArgumentException("Legacy palette schema is forbidden");
        JsonObject state = new JsonObject();state.addProperty("id",raw.getStringOr("id",""));
        JsonObject props = new JsonObject();
        for (String key : raw.getCompoundOrEmpty("properties").keySet())
            props.addProperty(key,raw.getCompoundOrEmpty("properties").getStringOr(key,""));
        if (!props.isEmpty()) state.add("properties",props);
        return state;
    }
    private static void audit(MinecraftServer server) {
        JsonObject result = new JsonObject();JsonArray failures = new JsonArray(), palettes = new JsonArray(), resources = new JsonArray();
        int entities = 0;
        try {
            if (!Boolean.getBoolean("valen.station.readbackOnly") ||
                !server.getServerDirectory().toRealPath().equals(FIXTURE.resolve("server").toRealPath()))
                throw new IllegalStateException("Refusing non-fixture fidelity audit");
            JsonObject manifest = manifest();JsonArray mods = new JsonArray();
            for (JsonElement value : manifest.getAsJsonArray("required_mods")) {
                String id = value.getAsString();
                var mod = FabricLoader.getInstance().getModContainer(id).orElseThrow(() -> new IllegalStateException("Required actual donor mod absent: " + id));
                JsonObject row = new JsonObject();row.addProperty("id",id);row.addProperty("version",mod.getMetadata().getVersion().getFriendlyString());mods.add(row);
            }
            result.add("actual_loaded_mods",mods);
            Set<String> seen = new HashSet<>();
            for (JsonElement entry : manifest.getAsJsonArray("resources")) {
                JsonObject spec = entry.getAsJsonObject();Identifier id = Identifier.parse(spec.get("id").getAsString());
                Identifier resource = Identifier.fromNamespaceAndPath(id.getNamespace(),"structure/"+id.getPath()+".nbt");
                byte[] bytes;
                try (var input = server.getResourceManager().getResource(resource).orElseThrow().open()) { bytes=input.readAllBytes(); }
                if (!sha(bytes).equals(spec.get("sha256").getAsString())) throw new IllegalStateException("Resource drift: "+id);
                CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(bytes),NbtAccounter.unlimitedHeap());
                List<BlockState> states = new ArrayList<>();
                for (Tag tag : root.getListOrEmpty("palette")) {
                    JsonObject raw = rawState((CompoundTag)tag);
                    BlockState state = resolve(raw);states.add(state);
                    if (seen.add(raw.toString())) {
                        JsonObject row = new JsonObject();row.add("state",raw);row.addProperty("native_state",state.toString());
                        row.addProperty("block_class",state.getBlock().getClass().getName());row.addProperty("render_shape",state.getRenderShape().toString());
                        palettes.add(row);
                    }
                }
                int blockEntities = 0;
                for (Tag tag : root.getListOrEmpty("blocks")) {
                    CompoundTag block=(CompoundTag)tag;
                    if (!block.contains("nbt")) continue;
                    CompoundTag nbt=block.getCompoundOrEmpty("nbt");BlockState state=states.get(block.getIntOr("state",-1));
                    if (!(state.getBlock() instanceof EntityBlock factory)) throw new IllegalStateException(id+" NBT on non-entity block "+state);
                    BlockEntity nativeEntity=factory.newBlockEntity(BlockPos.ZERO,state);
                    if (nativeEntity==null) throw new IllegalStateException("Native entity factory returned null: "+state);
                    String expected=BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(nativeEntity.getType()).toString();
                    if (!expected.equals(nbt.getStringOr("id",""))) throw new IllegalStateException(id+" entity type mismatch "+nbt.getStringOr("id","")+" != "+expected);
                    try (var reporter=new ProblemReporter.ScopedCollector(new ProblemReporter.RootFieldPathElement(id.toString()),LoggerFactory.getLogger(StructureFidelityContracts.class))) {
                        nativeEntity.loadWithComponents(TagValueInput.create(reporter,server.registryAccess(),nbt));
                        if (!reporter.isEmpty()) throw new IllegalStateException(id+" field decode loss: "+reporter.getReport());
                    }
                    CompoundTag saved=nativeEntity.saveWithFullMetadata(server.registryAccess());
                    JsonArray lost=new JsonArray();
                    for (String key : nbt.keySet()) {
                        if (Set.of("id","x","y","z","keepPacked").contains(key)) continue;
                        Tag before=nbt.get(key);
                        if (before instanceof CompoundTag compound && compound.isEmpty() || before instanceof ListTag list && list.isEmpty()) continue;
                        if (!Objects.equals(before,saved.get(key))) lost.add(key);
                    }
                    if (!lost.isEmpty()) failures.add(id+" source fields require explicit semantic readback: "+lost);
                    if (nativeEntity instanceof Container container && container.getContainerSize()<1) throw new IllegalStateException("Empty native container schema "+expected);
                    blockEntities++;
                }
                entities+=blockEntities;JsonObject row=new JsonObject();row.addProperty("id",id.toString());row.addProperty("sha256",sha(bytes));row.addProperty("native_block_entities",blockEntities);row.addProperty("palette",states.size());resources.add(row);
            }
            result.addProperty("status",failures.isEmpty()?"pass":"incomplete");
        } catch (Throwable failure) {
            result.addProperty("status","failed");failures.add(failure.toString());failure.printStackTrace();
        } finally {
            result.add("failures",failures);result.add("resources",resources);result.add("palettes",palettes);
            result.addProperty("native_block_entities",entities);result.addProperty("world_or_inventory_mutated",false);
            result.addProperty("visual_acceptance",false);
            try {
                if (server.getServerDirectory().toAbsolutePath().normalize().equals(FIXTURE.resolve("server")))
                    Files.writeString(FIXTURE.resolve("server/valen-structure-fidelity-native.json"),GSON.toJson(result)+"\n");
            } catch(Exception error) { throw new IllegalStateException(error); }
        }
    }
}
