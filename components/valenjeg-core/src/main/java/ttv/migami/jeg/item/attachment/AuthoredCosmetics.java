package ttv.migami.jeg.item.attachment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;

/**
 * Shared allow-list of cosmetics that have authored assets. A resource pack may replace a listed
 * texture; a missing file keeps the base appearance. This list does not invent skins or dye colors.
 */
public final class AuthoredCosmetics {
    private static final Map<String, Set<String>> PAINT_SKINS = loadPaints();
    private static final Set<String> DYED_GUNS = loadDyed();

    private AuthoredCosmetics() {
    }

    public static boolean anyPaint(Identifier gunId) {
        if (!jeg(gunId)) {
            return false;
        }
        for (Set<String> guns : PAINT_SKINS.values()) {
            if (guns.contains(gunId.getPath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean paint(Identifier gunId, String paintJob) {
        if (!jeg(gunId) || paintJob == null || paintJob.isBlank()) {
            return false;
        }
        return PAINT_SKINS.getOrDefault(paintJob, Set.of()).contains(gunId.getPath());
    }

    public static boolean dye(Identifier gunId) {
        return gunId != null && DYED_GUNS.contains(gunId.toString());
    }

    private static boolean jeg(Identifier gunId) {
        return gunId != null && "jeg".equals(gunId.getNamespace());
    }

    private static Map<String, Set<String>> loadPaints() {
        JsonObject root = resource();
        if (root == null || !root.has("paint_skins") || !root.get("paint_skins").isJsonObject()) {
            return Map.of();
        }
        Map<String, Set<String>> paints = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("paint_skins").entrySet()) {
            if (!entry.getValue().isJsonArray()) {
                continue;
            }
            Set<String> guns = new LinkedHashSet<>();
            for (JsonElement gun : entry.getValue().getAsJsonArray()) {
                if (gun.isJsonPrimitive()) {
                    guns.add(gun.getAsString());
                }
            }
            paints.put(entry.getKey(), Collections.unmodifiableSet(guns));
        }
        return Collections.unmodifiableMap(paints);
    }

    private static Set<String> loadDyed() {
        JsonObject root = resource();
        if (root == null || !root.has("dyed_guns") || !root.get("dyed_guns").isJsonArray()) {
            return Set.of();
        }
        Set<String> guns = new LinkedHashSet<>();
        JsonArray array = root.getAsJsonArray("dyed_guns");
        for (JsonElement gun : array) {
            if (gun.isJsonPrimitive()) {
                guns.add(gun.getAsString());
            }
        }
        return Collections.unmodifiableSet(guns);
    }

    private static JsonObject resource() {
        try (InputStream in = AuthoredCosmetics.class.getResourceAsStream("/data/jeg/cosmetic_support.json")) {
            if (in == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
