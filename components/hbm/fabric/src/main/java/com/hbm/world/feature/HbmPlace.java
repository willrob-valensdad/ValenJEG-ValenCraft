// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Placement arguments carried by the 26.3 Feature.place signature, plus the feature config. */
public record HbmPlace<C>(
        WorldGenLevel level,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BlockPos origin,
        C config) {}
