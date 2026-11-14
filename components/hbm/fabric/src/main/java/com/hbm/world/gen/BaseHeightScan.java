// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.gen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Ocean-floor heights for a structure box. 26.3 samples each column through
 * {@link NoiseBasedChunkGenerator#getBaseHeight}, which reads the final density
 * function and aquifer. Cell width and height are no longer on {@code NoiseSettings}.
 */
final class BaseHeightScan {
    private BaseHeightScan() {}

    static boolean fill(
            NoiseBasedChunkGenerator generator,
            LevelHeightAccessor heightAccessor,
            RandomState randomState,
            BoundingBox box,
            int[] out) {
        if (generator.generatorSettings().value().noiseSettings().clampToHeightAccessor(heightAccessor).height()
                <= 0) {
            return false;
        }
        int index = 0;
        for (int z = box.minZ(); z <= box.maxZ(); z++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
                out[index++] =
                        generator.getBaseHeight(
                                x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
            }
        }
        return true;
    }
}
