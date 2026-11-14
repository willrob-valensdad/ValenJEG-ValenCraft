package ttv.migami.jeg.station.probe;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Loaded-registry proof for jeg:has_structure/valen/arctic_station.
 * Call from SERVER_STARTED after biome tags are bound. Fail closed.
 * Does not place a structure and does not touch other profiles.
 */
public final class ArcticStationWinterAssert {
    public static final TagKey<Biome> ARCTIC = TagKey.create(Registries.BIOME, Identifier.parse("jeg:has_structure/valen/arctic_station"));

    private static final String[] REQUIRED = {
        "minecraft:snowy_plains",
        "minecraft:snowy_taiga",
        "minecraft:snowy_beach",
        "minecraft:ice_spikes",
        "minecraft:grove",
        "minecraft:snowy_slopes",
        "minecraft:jagged_peaks",
        "minecraft:frozen_peaks",
        "minecraft:frozen_river",
    };

    private static final String[] OPTIONAL_WHEN_LOADED = {
        "terralith:alpha_islands_winter",
        "terralith:alpine_grove",
        "terralith:cold_shrubland",
        "terralith:emerald_peaks",
        "terralith:frozen_cliffs",
        "terralith:glacial_chasm",
        "terralith:gravel_desert",
        "terralith:ice_marsh",
        "terralith:rocky_shrubland",
        "terralith:scarlet_mountains",
        "terralith:siberian_grove",
        "terralith:snowy_badlands",
        "terralith:snowy_cherry_grove",
        "terralith:snowy_maple_forest",
        "terralith:snowy_shield",
        "terralith:wintry_forest",
        "terralith:wintry_lowlands",
        "biomesoplenty:auroral_garden",
        "biomesoplenty:muskeg",
        "biomesoplenty:snowblossom_grove",
        "biomesoplenty:snowy_coniferous_forest",
        "biomesoplenty:snowy_fir_clearing",
        "biomesoplenty:snowy_maple_woods",
        "biomesoplenty:wintry_origin_valley",
        "promenade:glacarian_taiga",
        "neon_and_rust:snowy_springs",
        "twilightforest:glacier",
        "twilightforest:snowy_forest",
    };

    private static final String[] REJECTED_WHEN_LOADED = {
        "minecraft:taiga",
        "minecraft:old_growth_pine_taiga",
        "minecraft:old_growth_spruce_taiga",
        "minecraft:plains",
        "minecraft:desert",
        "minecraft:forest",
        "terralith:rocky_mountains",
        "terralith:siberian_taiga",
        "terralith:alpine_highlands",
        "biomesoplenty:cold_desert",
        "biomesoplenty:tundra",
        "spacereloaded:moon_plains",
        "spacereloaded:orbit_void",
        "spacereloaded:mars_plains",
        "nerospace:terraformed_tundra",
        "nerospace:glacira",
        "nerospace:terraformed",
    };

    private ArcticStationWinterAssert() {}

    public static void assertLoaded(MinecraftServer server) {
        HolderLookup.RegistryLookup<Biome> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        Optional<HolderSet.Named<Biome>> bound = biomes.get(ARCTIC);
        if (bound.isEmpty()) {
            throw new AssertionError("arctic station biome tag is not loaded: " + ARCTIC.location());
        }
        HolderSet.Named<Biome> tag = bound.get();
        for (String id : REQUIRED) {
            Holder<Biome> holder = require(biomes, id);
            if (!tag.contains(holder)) {
                throw new AssertionError("winter biome missing from arctic tag: " + id);
            }
        }
        for (String id : OPTIONAL_WHEN_LOADED) {
            biomes.get(key(id)).ifPresent(holder -> {
                if (!tag.contains(holder)) {
                    throw new AssertionError("loaded winter biome missing from arctic tag: " + id);
                }
            });
        }
        for (String id : REJECTED_WHEN_LOADED) {
            biomes.get(key(id)).ifPresent(holder -> {
                if (tag.contains(holder)) {
                    throw new AssertionError("non-winter biome admitted by arctic tag: " + id);
                }
            });
        }
    }

    private static Holder<Biome> require(HolderLookup.RegistryLookup<Biome> biomes, String id) {
        return biomes.get(key(id)).orElseThrow(() -> new AssertionError("required biome is not loaded: " + id));
    }

    private static ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, Identifier.parse(id));
    }
}
