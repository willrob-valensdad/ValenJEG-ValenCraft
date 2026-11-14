// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world;

import com.mojang.serialization.MapCodec;

import com.hbm.blocks.ModBlocks;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.world.feature.*;
import com.hbm.world.placement.LightBlockingHeightPlacement;
import com.hbm.world.placement.OreHostPlacement;
import com.hbm.world.placement.WorldConfigPlacement;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class HbmFeatures {

    public static RegistryHandle<MapCodec<? extends Feature>> STRATUM;
    public static RegistryHandle<MapCodec<? extends Feature>> STRATUM_3D;
    public static RegistryHandle<MapCodec<? extends Feature>> DEPTH_DEPOSIT;
    public static RegistryHandle<MapCodec<? extends Feature>> WINDOWED_ORE;
    public static RegistryHandle<MapCodec<? extends Feature>> COLTAN_DEPOSIT;
    public static RegistryHandle<MapCodec<? extends Feature>> NETHER_SURFACE;
    public static RegistryHandle<MapCodec<? extends Feature>> BEDROCK_ORE;
    public static RegistryHandle<MapCodec<? extends Feature>> ORE_CAVE;
    public static RegistryHandle<MapCodec<? extends Feature>> OIL_BUBBLE;
    public static RegistryHandle<MapCodec<? extends Feature>> BEDROCK_OIL_DEPOSIT;

    public static RegistryHandle<MapCodec<? extends Feature>> METEORITE;
    public static RegistryHandle<MapCodec<? extends Feature>> GEYSER;
    public static RegistryHandle<MapCodec<? extends Feature>> BIOME_CAVE;
    public static RegistryHandle<MapCodec<? extends Feature>> GLYPHID_HIVE;
    public static RegistryHandle<MapCodec<? extends Feature>> DUD;
    public static RegistryHandle<MapCodec<? extends Feature>> LOCKED_SAFE;
    public static RegistryHandle<MapCodec<? extends Feature>> LANTERN_BEHEMOTH;
    public static RegistryHandle<MapCodec<? extends Feature>> BOSNIA_MINE;
    public static RegistryHandle<MapCodec<? extends Feature>> PINK_LOG;
    public static RegistryHandle<MapCodec<? extends Feature>> LIBRARY_DUNGEON;
    public static RegistryHandle<MapCodec<? extends Feature>> BARREL;
    public static RegistryHandle<MapCodec<? extends Feature>> ARCTIC_VAULT;
    public static RegistryHandle<MapCodec<? extends Feature>> FOXGLOVE;
    public static RegistryHandle<MapCodec<? extends Feature>> NIGHTSHADE;
    public static RegistryHandle<MapCodec<? extends Feature>> TOBACCO;
    public static RegistryHandle<MapCodec<? extends Feature>> WEED;
    public static RegistryHandle<MapCodec<? extends Feature>> REEDS_RIVER;
    public static RegistryHandle<MapCodec<? extends Feature>> REEDS_BEACH;
    public static RegistryHandle<MapCodec<? extends Feature>> JUNGLE_DUNGEON_MARKER;
    public static RegistryHandle<MapCodec<? extends Feature>> CAPSULE;
    public static RegistryHandle<MapCodec<? extends Feature>> KEYHOLE;

    private HbmFeatures() {}

    public static void register() {
        LightBlockingHeightPlacement.register();
        WorldConfigPlacement.register();
        OreHostPlacement.register();
        STRATUM = FabricRegistrar.registerFeature("stratum", StratumFeature.CODEC);
        STRATUM_3D = FabricRegistrar.registerFeature("stratum_3d", Stratum3DFeature.CODEC);
        DEPTH_DEPOSIT = FabricRegistrar.registerFeature("depth_deposit", DepthDepositFeature.CODEC);
        WINDOWED_ORE = FabricRegistrar.registerFeature("windowed_ore", WindowedOreFeature.CODEC);
        COLTAN_DEPOSIT =
                FabricRegistrar.registerFeature("coltan_deposit", ColtanDepositFeature.CODEC);
        NETHER_SURFACE =
                FabricRegistrar.registerFeature("nether_surface", NetherSurfaceFeature.CODEC);
        BEDROCK_ORE = FabricRegistrar.registerFeature("bedrock_ore", BedrockOreFeature.CODEC);
        ORE_CAVE = FabricRegistrar.registerFeature("ore_cave", OreCaveFeature.CODEC);
        OIL_BUBBLE = FabricRegistrar.registerFeature("oil_bubble", OilBubbleFeature.CODEC);
        BEDROCK_OIL_DEPOSIT =
                FabricRegistrar.registerFeature(
                        "bedrock_oil_deposit", BedrockOilDepositFeature.CODEC);
        METEORITE = FabricRegistrar.registerFeature("meteorite", MeteoriteFeature.CODEC);
        GEYSER = FabricRegistrar.registerFeature("geyser", GeyserFeature.CODEC);
        BIOME_CAVE = FabricRegistrar.registerFeature("biome_cave", BiomeCaveFeature.CODEC);
        GLYPHID_HIVE = FabricRegistrar.registerFeature("glyphid_hive", GlyphidHiveFeature.CODEC);
        DUD = FabricRegistrar.registerFeature("dud", DudFeature.CODEC);
        LOCKED_SAFE = FabricRegistrar.registerFeature("locked_safe", LockedSafeFeature.CODEC);
        LANTERN_BEHEMOTH =
                FabricRegistrar.registerFeature("lantern_behemoth", LanternBehemothFeature.CODEC);
        BOSNIA_MINE = FabricRegistrar.registerFeature("bosnia_mine", BosniaMineFeature.CODEC);
        PINK_LOG = FabricRegistrar.registerFeature("pink_log", PinkLogFeature.CODEC);
        LIBRARY_DUNGEON =
                FabricRegistrar.registerFeature("library_dungeon", LibraryDungeonFeature.CODEC);
        BARREL = FabricRegistrar.registerFeature("barrel", BarrelFeature.CODEC);
        ARCTIC_VAULT = FabricRegistrar.registerFeature("arctic_vault", ArcticVaultFeature.CODEC);
        FOXGLOVE =
                FabricRegistrar.registerFeature(
                        "foxglove",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_FLOWER_FOXGLOVE.get(), 16).codec());
        NIGHTSHADE =
                FabricRegistrar.registerFeature(
                        "nightshade",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_FLOWER_NIGHTSHADE.get(), 8).codec());
        TOBACCO =
                FabricRegistrar.registerFeature(
                        "tobacco",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_FLOWER_TOBACCO.get(), 8).codec());
        WEED =
                FabricRegistrar.registerFeature(
                        "weed",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_FLOWER_WEED.get(), 64).codec());
        REEDS_RIVER =
                FabricRegistrar.registerFeature(
                        "reeds_river",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_REEDS.get(), 4).codec());
        REEDS_BEACH =
                FabricRegistrar.registerFeature(
                        "reeds_beach",
                        new FlowerPatchFeature(() -> ModBlocks.PLANT_REEDS.get(), 8).codec());
        JUNGLE_DUNGEON_MARKER =
                FabricRegistrar.registerFeature(
                        "jungle_dungeon_marker", JungleDungeonMarkerFeature.CODEC);
        CAPSULE = FabricRegistrar.registerFeature("capsule", CapsuleFeature.CODEC);
        KEYHOLE = FabricRegistrar.registerFeature("keyhole", KeyholeFeature.CODEC);
    }

    public static MapCodec<? extends Feature> stratum() {
        return STRATUM.get();
    }

    public static MapCodec<? extends Feature> stratum3d() {
        return STRATUM_3D.get();
    }

    public static MapCodec<? extends Feature> depthDeposit() {
        return DEPTH_DEPOSIT.get();
    }

    public static MapCodec<? extends Feature> windowedOre() {
        return WINDOWED_ORE.get();
    }

    public static MapCodec<? extends Feature> coltanDeposit() {
        return COLTAN_DEPOSIT.get();
    }

    public static MapCodec<? extends Feature> netherSurface() {
        return NETHER_SURFACE.get();
    }

    public static MapCodec<? extends Feature> bedrockOre() {
        return BEDROCK_ORE.get();
    }

    public static MapCodec<? extends Feature> oreCave() {
        return ORE_CAVE.get();
    }

    public static MapCodec<? extends Feature> oilBubble() {
        return OIL_BUBBLE.get();
    }

    public static MapCodec<? extends Feature> bedrockOilDeposit() {
        return BEDROCK_OIL_DEPOSIT.get();
    }

    public static MapCodec<? extends Feature> meteorite() {
        return METEORITE.get();
    }

    public static MapCodec<? extends Feature> geyser() {
        return GEYSER.get();
    }

    public static MapCodec<? extends Feature> capsule() {
        return CAPSULE.get();
    }

    public static MapCodec<? extends Feature> biomeCave() {
        return BIOME_CAVE.get();
    }

    public static MapCodec<? extends Feature> glyphidHive() {
        return GLYPHID_HIVE.get();
    }

    public static MapCodec<? extends Feature> dud() {
        return DUD.get();
    }

    public static MapCodec<? extends Feature> lanternBehemoth() {
        return LANTERN_BEHEMOTH.get();
    }

    public static MapCodec<? extends Feature> libraryDungeon() {
        return LIBRARY_DUNGEON.get();
    }

    public static MapCodec<? extends Feature> arcticVault() {
        return ARCTIC_VAULT.get();
    }

    public static MapCodec<? extends Feature> barrel() {
        return BARREL.get();
    }

    public static MapCodec<? extends Feature> foxglove() {
        return FOXGLOVE.get();
    }

    public static MapCodec<? extends Feature> nightshade() {
        return NIGHTSHADE.get();
    }

    public static MapCodec<? extends Feature> tobacco() {
        return TOBACCO.get();
    }

    public static MapCodec<? extends Feature> weed() {
        return WEED.get();
    }

    public static MapCodec<? extends Feature> reedsRiver() {
        return REEDS_RIVER.get();
    }

    public static MapCodec<? extends Feature> reedsBeach() {
        return REEDS_BEACH.get();
    }
}
