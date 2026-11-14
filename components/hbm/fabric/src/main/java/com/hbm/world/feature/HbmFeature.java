// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * 26.3 features are codec-bearing records. This keeps HBM's configured place
 * body and publishes the config through the feature codec.
 */
public abstract class HbmFeature<C> implements Feature {
    private final C config;

    protected HbmFeature(C config) {
        this.config = config;
    }

    public final C config() {
        return config;
    }

    @Override
    public final boolean place(
            WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return placeConfigured(new HbmPlace<>(level, chunkGenerator, random, origin, config));
    }

    protected abstract boolean placeConfigured(HbmPlace<C> ctx);

    public static <C, F extends HbmFeature<C>> MapCodec<F> mapCodec(
            Codec<C> configCodec, Function<C, F> factory) {
        return MapCodec.assumeMapUnsafe(configCodec).xmap(factory, HbmFeature::config);
    }

    public static <F extends HbmFeature<HbmUnit>> MapCodec<F> unitCodec(Supplier<F> factory) {
        return MapCodec.unit(factory);
    }
}
