package top.ribs.scguns.compat.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import top.ribs.scguns.ScorchedGuns;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transitional 26.2 bridge for Scorched Guns' legacy data-driven JSON loaders.
 *
 * <p>Minecraft 26.2 moved {@code SimpleJsonResourceReloadListener} to a codec-based
 * API. The original Scorched Guns loaders intentionally deserialize their JSON in
 * {@code apply(...)} with custom Gson adapters, so this bridge only reproduces the
 * old discovery/JSON-AST preparation stage. This keeps the existing custom-gun and
 * ExoSuit-upgrade formats intact while using the current reload lifecycle.</p>
 */
public abstract class LegacyJsonResourceReloadListener
        extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    private final FileToIdConverter converter;

    protected LegacyJsonResourceReloadListener(String directory) {
        this.converter = FileToIdConverter.json(directory);
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> prepared = new LinkedHashMap<>();
        this.converter.listMatchingResources(manager).forEach((fileId, resource) -> {
            Identifier id = this.converter.fileToId(fileId);
            try (var reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json != null && !json.isJsonNull()) {
                    prepared.put(id, json);
                }
            } catch (Exception e) {
                ScorchedGuns.LOGGER.error("Couldn't parse data file {} from {}", id, fileId, e);
            }
        });
        return prepared;
    }
}
