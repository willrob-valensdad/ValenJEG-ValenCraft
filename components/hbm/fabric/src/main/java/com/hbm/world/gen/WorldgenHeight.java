// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.gen;

import com.hbm.blocks.BlockMotion;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class WorldgenHeight {
    private WorldgenHeight() {}

    public static int lightBlocking(LevelReader level, int x, int z) {
        return lightBlocking(level.getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY, false), x, z);
    }

    public static int lightBlocking(ChunkAccess chunk, int x, int z) {
        return scan(chunk, x, z, false);
    }

    public static int ground(LevelReader level, int x, int z) {
        return scan(level.getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY, false), x, z, true);
    }

    public static int ground(ChunkAccess chunk, int x, int z) {
        return scan(chunk, x, z, true);
    }

    public static int highestFilledSectionTop(LevelReader level, int x, int z) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY, false);
        if (chunk instanceof ImposterProtoChunk imposter) chunk = imposter.getWrapped();
        int highest = chunk.getHighestFilledSectionIndex();
        return highest == -1
                ? chunk.getMinY()
                : Math.min(
                        SectionPos.sectionToBlockCoord(
                                chunk.getSectionYFromSectionIndex(highest), 15),
                        chunk.getMaxY());
    }

    private static int scan(ChunkAccess chunk, int x, int z, boolean ground) {
        if (chunk instanceof ImposterProtoChunk imposter) chunk = imposter.getWrapped();
        int highest = chunk.getHighestFilledSectionIndex();
        int top =
                highest == -1
                        ? chunk.getMinY()
                        : Math.min(
                                SectionPos.sectionToBlockCoord(
                                        chunk.getSectionYFromSectionIndex(highest), 15),
                                chunk.getMaxY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        for (int y = top; y >= chunk.getMinY(); y--) {
            BlockState state = chunk.getBlockState(pos.setY(y));
            if (ground
                    ? BlockMotion.blocksMotion(state) && !state.is(BlockTags.LEAVES)
                    : state.getLightDampening() > 0) {
                return y + 1;
            }
        }
        return chunk.getMinY();
    }

    public static int averageGround(Structure.GenerationContext context, BoundingBox box) {
        return averageGround(context, box.minX(), box.minZ(), box.maxX(), box.maxZ());
    }

    public static int averageGround(
            Structure.GenerationContext context, int minX, int minZ, int maxX, int maxZ) {
        int xSamples = symmetricSampleCount(minX, maxX);
        int zSamples = symmetricSampleCount(minZ, maxZ);
        int total = 0;
        for (int zi = 0; zi < zSamples; zi++) {
            int z = symmetricSample(minZ, maxZ, zSamples, zi);
            for (int xi = 0; xi < xSamples; xi++) {
                int x = symmetricSample(minX, maxX, xSamples, xi);
                total +=
                        Math.max(
                                context.chunkGenerator()
                                        .getBaseHeight(
                                                x,
                                                z,
                                                Heightmap.Types.OCEAN_FLOOR_WG,
                                                context.heightAccessor(),
                                                context.randomState()),
                                64);
            }
        }
        return total / (xSamples * zSamples);
    }

    public static int averageBaseHeight(Structure.GenerationContext context, BoundingBox box) {
        long total = 0;
        for (int height :
                baseHeights(
                        context.chunkGenerator(),
                        context.heightAccessor(),
                        context.randomState(),
                        box)) {
            total += height;
        }
        return (int) (total / ((long) box.getXSpan() * box.getZSpan()));
    }

    public static int[] baseHeights(
            ChunkGenerator generator,
            LevelHeightAccessor heightAccessor,
            RandomState randomState,
            BoundingBox box) {
        int[] out = new int[box.getXSpan() * box.getZSpan()];
        if (generator.getClass() == NoiseBasedChunkGenerator.class
                && BaseHeightScan.fill(
                        (NoiseBasedChunkGenerator) generator,
                        heightAccessor,
                        randomState,
                        box,
                        out)) {
            return out;
        }
        int i = 0;
        for (int z = box.minZ(); z <= box.maxZ(); z++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
                out[i++] =
                        generator.getBaseHeight(
                                x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
            }
        }
        return out;
    }

    public static void fillUnstructuredTerrain(
            Structure.GenerationContext context, ProtoChunk chunk) {
        if (!(context.chunkGenerator() instanceof NoiseBasedChunkGenerator generator)) return;
        var settings = generator.generatorSettings().value();
        var noiseSettings =
                settings.noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        if (noiseSettings.height() <= 0 || SharedConstants.debugVoidTerrain(chunk.getPos())) return;
        var randomState = context.randomState();
        chunk.fillBiomesFromNoise(context.biomeResolver());
        var biomes =
                new BiomeManager(context.biomeResolver(), BiomeManager.obfuscateSeed(context.seed()));
        ChunkPos pos = chunk.getPos();
        var volume =
                new net.minecraft.world.level.levelgen.densityfunction.DensityVolume(
                        16,
                        noiseSettings.height(),
                        16,
                        pos.getMinBlockX(),
                        noiseSettings.minY(),
                        pos.getMinBlockZ());
        try (var noise =
                new NoiseChunk(
                        randomState,
                        net.minecraft.world.level.levelgen.Beardifier.EMPTY,
                        settings,
                        fluidPicker(settings),
                        Blender.empty(),
                        volume)) {
            fillDensity(chunk, noise, settings);
            if (!SharedConstants.DEBUG_DISABLE_SURFACE) {
                randomState
                        .surfaceSystem()
                        .buildSurface(
                                randomState,
                                biomes,
                                new WorldGenerationContext(generator, chunk.getHeightAccessorForGeneration()),
                                chunk,
                                noise,
                                settings.materialRule().value(),
                                null);
            }
            if (!SharedConstants.DEBUG_DISABLE_CARVERS) {
                carve(context, generator, chunk, noise, randomState, settings, biomes);
            }
        }
    }

    private static net.minecraft.world.level.levelgen.Aquifer.FluidPicker fluidPicker(
            net.minecraft.world.level.levelgen.NoiseGeneratorSettings settings) {
        var lava =
                new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(
                        -54, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
        int seaLevel = settings.seaLevel();
        var sea =
                new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(
                        seaLevel, settings.defaultFluid());
        var empty =
                new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(
                        net.minecraft.world.level.dimension.DimensionType.MIN_Y * 2,
                        net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        return (x, y, z) -> {
            if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) return empty;
            return y < Math.min(-54, seaLevel) ? lava : sea;
        };
    }

    private static void fillDensity(
            ProtoChunk chunk,
            NoiseChunk noise,
            net.minecraft.world.level.levelgen.NoiseGeneratorSettings settings) {
        var oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        var worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        var aquifer = noise.aquifer();
        var volume = noise.volume();
        var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        var blockPos = new BlockPos.MutableBlockPos();
        var density = noise.cachingSamplers().get(settings.noiseRouter().finalDensity());
        try (var buffer = density.sampleVolume(volume)) {
            for (int z = 0; z < volume.sizeZ(); z++) {
                int blockZ = volume.blockZ(z);
                for (int x = 0; x < volume.sizeX(); x++) {
                    int blockX = volume.blockX(x);
                    for (int y = volume.sizeY() - 1; y >= 0; y--) {
                        int blockY = volume.blockY(y);
                        var section = chunk.getSection(chunk.getSectionIndex(blockY));
                        float value = buffer.get(volume.indexUnchecked(x, y, z));
                        var state = aquifer.computeSubstance(blockX, blockY, blockZ, value);
                        if (state == null) state = settings.defaultBlock();
                        if (state == air) continue;
                        section.setBlockState(
                                x, SectionPos.sectionRelative(blockY), z, state, false);
                        oceanFloor.update(x, blockY, z, state);
                        worldSurface.update(x, blockY, z, state);
                        if (aquifer.shouldScheduleFluidUpdate() && !state.getFluidState().isEmpty()) {
                            chunk.markPosForPostProcessing(blockPos.set(blockX, blockY, blockZ));
                        }
                    }
                }
            }
        }
    }

    private static void carve(
            Structure.GenerationContext context,
            NoiseBasedChunkGenerator generator,
            ProtoChunk chunk,
            NoiseChunk noise,
            net.minecraft.world.level.levelgen.RandomState randomState,
            net.minecraft.world.level.levelgen.NoiseGeneratorSettings settings,
            BiomeManager biomes) {
        var generation =
                new WorldGenerationContext(generator, chunk.getHeightAccessorForGeneration());
        int protectedBlocksOnTop = chunk.isUpgrading() ? 0 : 7;
        int maxY = generation.getMinGenY() + generation.getGenDepth() - 1 - protectedBlocksOnTop;
        var mask =
                new net.minecraft.world.level.chunk.CarvingMask(generation.getMinGenY() + 1, maxY);
        var random = new WorldgenRandom(new LegacyRandomSource(0));
        ChunkPos at = chunk.getPos();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                ChunkPos from = new ChunkPos(at.x() + dx, at.z() + dz);
                var biome =
                        context.biomeResolver()
                                .getNoiseBiome(
                                        QuartPos.fromBlock(from.getMinBlockX()),
                                        0,
                                        QuartPos.fromBlock(from.getMinBlockZ()));
                int index = 0;
                for (var holder : generator.getBiomeGenerationSettings(biome).getCarvers()) {
                    var carver = holder.value();
                    random.setLargeFeatureSeed(randomState.seed() + index++, from.x(), from.z());
                    if (carver.isStartChunk(random)) {
                        carver.carve(generation, random, at, from, mask);
                    }
                }
            }
        }
        if (mask.isEmpty()) return;
        var aquifer = noise.aquifer();
        var rule = settings.materialRule().value();
        var helper = new BlockPos.MutableBlockPos();
        mask.visit(
                (x, z, bottomY, topY) -> {
                    boolean hasGrass = false;
                    int worldX = at.getBlockX(x);
                    int worldZ = at.getBlockZ(z);
                    var pos = new BlockPos.MutableBlockPos();
                    for (int worldY = topY; worldY >= bottomY; worldY--) {
                        pos.set(worldX, worldY, worldZ);
                        var blockState = chunk.getBlockState(pos);
                        if (blockState.is(net.minecraft.tags.BlockTags.UNCARVABLE)) continue;
                        if (blockState.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                                || blockState.is(net.minecraft.world.level.block.Blocks.MYCELIUM)) {
                            hasGrass = true;
                        }
                        var carved = aquifer.computeSubstance(worldX, worldY, worldZ, 0.0F);
                        if (carved == null) continue;
                        chunk.setBlockState(pos, carved);
                        if (aquifer.shouldScheduleFluidUpdate() && !carved.getFluidState().isEmpty()) {
                            chunk.markPosForPostProcessing(pos);
                        }
                        if (hasGrass) {
                            helper.setWithOffset(pos, net.minecraft.core.Direction.DOWN);
                            if (chunk.getBlockState(helper)
                                    .is(net.minecraft.world.level.block.Blocks.DIRT)) {
                                randomState
                                        .surfaceSystem()
                                        .topMaterial(
                                                rule,
                                                randomState,
                                                generation,
                                                biomes::getBiome,
                                                chunk,
                                                noise.cachingSamplers(),
                                                helper,
                                                !carved.getFluidState().isEmpty())
                                        .ifPresent(
                                                top -> {
                                                    chunk.setBlockState(helper, top);
                                                    if (!top.getFluidState().isEmpty()) {
                                                        chunk.markPosForPostProcessing(helper);
                                                    }
                                                });
                            }
                        }
                    }
                });
    }

    private static int symmetricSampleCount(int min, int max) {
        if (min == max) return 1;
        int lowerMiddle = min + (max - min) / 2;
        int upperMiddle = max - (max - min) / 2;
        if (lowerMiddle == min && upperMiddle == max) return 2;
        return lowerMiddle == upperMiddle ? 3 : 4;
    }

    private static int symmetricSample(int min, int max, int count, int index) {
        if (index == 0) return min;
        if (index == count - 1) return max;
        return index == 1 ? min + (max - min) / 2 : max - (max - min) / 2;
    }
}
