package ttv.migami.jeg.station;
import com.google.gson.*;
import java.io.IOException;
import java.util.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;

public final class StationRecipes {
    private static final Gson GSON = new Gson();
    private static volatile Map<Identifier, StationRecipe> recipes = Map.of();
    public static Loader reloadListener() { return new Loader(); }
    public static List<StationRecipe> forStation(String station) {
        return recipes.values().stream().filter(r -> r.station().equals(station))
            .sorted(Comparator.comparing(r -> r.id().toString())).toList();
    }
    public static StationRecipe get(Identifier id) { return recipes.get(id); }
    public static final class Loader extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> implements IdentifiableResourceReloadListener {
        private static final FileToIdConverter LISTER = FileToIdConverter.json("valen_station");

        @Override
        public Identifier getFabricId() {
            return Reference.id("valen_station");
        }

        @Override
        protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, JsonElement> objects = new HashMap<>();
            LISTER.listMatchingResources(manager).forEach((fileId, resource) -> {
                Identifier id = LISTER.fileToId(fileId);
                try (var reader = resource.openAsReader()) {
                    objects.put(id, GsonHelper.fromJson(GSON, reader, JsonElement.class));
                } catch (IOException | RuntimeException exception) {
                    JustEnoughGuns.LOGGER.error("Failed to read vehicle assembly recipe {}: {}", id, exception.getMessage());
                }
            });
            return objects;
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, StationRecipe> loaded = new HashMap<>();
            Map<Identifier, String> raw = new HashMap<>();
            for (Map.Entry<Identifier, JsonElement> entry : objects.entrySet()) {
                try {
                    JsonObject object = entry.getValue().getAsJsonObject();
                    raw.put(entry.getKey(), GSON.toJson(object));
                    loaded.put(entry.getKey(), StationRecipe.parse(entry.getKey(), object));
                } catch (RuntimeException exception) {
                    JustEnoughGuns.LOGGER.error("Failed to load vehicle assembly recipe {}: {}", entry.getKey(), exception.getMessage());
                }
            }
            recipes = Map.copyOf(loaded);

            JustEnoughGuns.LOGGER.info("Loaded {} JEG restored station recipes", recipes.size());
        }
    }
}
