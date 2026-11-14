// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.saveddata.TomSaveData;
import com.hbm.world.gen.WorldgenHeight;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class FlowerPatchFeature extends HbmFeature<HbmUnit> {

    private final Supplier<Block> flower;
    private final int odds;

    private final MapCodec<FlowerPatchFeature> codec;

    public FlowerPatchFeature(Supplier<Block> flower, int odds) {
        super(HbmUnit.INSTANCE);
        this.flower = flower;
        this.odds = odds;
        this.codec = MapCodec.unit(this);
    }

        public MapCodec<FlowerPatchFeature> codec() {
        return codec;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (TomSaveData.impact(level.getLevel()) || random.nextInt(odds) != 0) return false;

        BlockPos chunkOrigin = context.origin();
        int x = (chunkOrigin.getX() & ~15) + random.nextInt(16);
        int z = (chunkOrigin.getZ() & ~15) + random.nextInt(16);
        int y = WorldgenHeight.lightBlocking(level, x, z);
        BlockState flower = this.flower.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int attempt = 0; attempt < 64; attempt++) {
            pos.set(
                    x + random.nextInt(8) - random.nextInt(8),
                    y + random.nextInt(4) - random.nextInt(4),
                    z + random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(pos) && flower.canSurvive(level, pos)) {
                level.setBlock(pos, flower, 2);
                placed = true;
            }
        }
        return placed;
    }
}
