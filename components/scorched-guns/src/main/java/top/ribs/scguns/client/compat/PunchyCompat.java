package top.ribs.scguns.client.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.item.GunItem;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Punchy compatibility is intentionally config-only. Scorched Guns does not
 * mix into, copy, or replace Punchy's rendering code. When Punchy is present,
 * this class merges every registered scguns GunItem id into Punchy's own
 * itemBlacklist so Punchy can opt out before the two first-person arm renderers
 * overlap.
 */
public final class PunchyCompat {
    private static final String PUNCHY_MOD_ID = "punchy";
    private static final String BLACKLIST_KEY = "itemBlacklist";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int RETRY_INTERVAL_TICKS = 20;
    private static final int MAX_RETRY_ATTEMPTS = 120;

    private static boolean finished;
    private static int tickCounter;
    private static int attempts;

    private PunchyCompat() {
    }

    /**
     * Called from the client setup queue and retried briefly from client ticks.
     * Retrying handles launch order where Punchy has not written its config yet.
     */
    public static void syncGunBlacklist() {
        if (finished) {
            return;
        }
        if (!ModList.get().isLoaded(PUNCHY_MOD_ID)) {
            finished = true;
            return;
        }

        Path configPath = findPunchyConfig();
        if (configPath == null) {
            attempts++;
            if (attempts >= MAX_RETRY_ATTEMPTS) {
                ScorchedGuns.LOGGER.debug("Punchy compatibility: punchy_config.json was not found; blacklist sync was skipped.");
                finished = true;
            }
            return;
        }

        try {
            JsonObject root;
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!parsed.isJsonObject()) {
                    ScorchedGuns.LOGGER.warn("Punchy compatibility: {} does not contain a JSON object; leaving it unchanged.", configPath);
                    finished = true;
                    return;
                }
                root = parsed.getAsJsonObject();
            }

            JsonArray blacklist;
            JsonElement existingBlacklist = root.get(BLACKLIST_KEY);
            if (existingBlacklist == null || existingBlacklist.isJsonNull()) {
                blacklist = new JsonArray();
                root.add(BLACKLIST_KEY, blacklist);
            } else if (existingBlacklist.isJsonArray()) {
                blacklist = existingBlacklist.getAsJsonArray();
            } else {
                ScorchedGuns.LOGGER.warn("Punchy compatibility: '{}' in {} is not an array; leaving it unchanged.", BLACKLIST_KEY, configPath);
                finished = true;
                return;
            }

            Set<String> existingEntries = new LinkedHashSet<>();
            for (JsonElement element : blacklist) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    existingEntries.add(element.getAsString());
                }
            }

            List<String> gunIds = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof GunItem)
                    .map(BuiltInRegistries.ITEM::getKey)
                    .filter(id -> id != null && Reference.MOD_ID.equals(id.getNamespace()))
                    .map(Identifier::toString)
                    .sorted()
                    .toList();

            int added = 0;
            for (String gunId : gunIds) {
                if (existingEntries.add(gunId)) {
                    blacklist.add(gunId);
                    added++;
                }
            }

            if (added > 0) {
                writeAtomically(configPath, root);
                ScorchedGuns.LOGGER.info(
                        "Punchy compatibility: added {} Scorched Guns weapon ids to Punchy's item blacklist. " +
                                "If Punchy already loaded its config this launch, restart the game once for the new blacklist to take effect.",
                        added
                );
            }
            finished = true;
        } catch (Exception e) {
            ScorchedGuns.LOGGER.warn("Punchy compatibility: failed to update {}. Punchy's config was left untouched where possible.", configPath, e);
            finished = true;
        }
    }

    public static void clientTick() {
        if (finished || !ModList.get().isLoaded(PUNCHY_MOD_ID)) {
            finished = true;
            return;
        }
        tickCounter++;
        if (tickCounter >= RETRY_INTERVAL_TICKS) {
            tickCounter = 0;
            syncGunBlacklist();
        }
    }

    private static Path findPunchyConfig() {
        Path configDir = FMLPaths.CONFIGDIR;
        Path nested = configDir.resolve("punchy").resolve("punchy_config.json");
        if (Files.isRegularFile(nested)) {
            return nested;
        }
        Path flat = configDir.resolve("punchy_config.json");
        if (Files.isRegularFile(flat)) {
            return flat;
        }
        return null;
    }

    private static void writeAtomically(Path configPath, JsonObject root) throws Exception {
        Path parent = configPath.getParent();
        if (parent == null) {
            throw new IllegalStateException("Punchy config has no parent directory: " + configPath);
        }
        Path temp = Files.createTempFile(parent, "punchy_config.scguns.", ".tmp");
        boolean moved = false;
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            try {
                Files.move(temp, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temp);
            }
        }
    }
}
