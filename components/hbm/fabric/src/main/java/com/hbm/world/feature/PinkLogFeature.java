// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PinkLogFeature extends HbmFeature<HbmUnit> {

    public static final MapCodec<PinkLogFeature> CODEC = HbmFeature.unitCodec(PinkLogFeature::new);

    public PinkLogFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<PinkLogFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> context) {
        WorldGenLevel level = context.level();
        BlockState oak = Blocks.OAK_LOG.defaultBlockState();
        BlockState pink = ModBlocks.PINK_LOG.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = context.origin().mutable();
        boolean done = false;
        for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
            pos.setY(y);
            if (level.getBlockState(pos) == oak) {
                setBlock(level, pos, pink);
                done = true;
            }
        }
        return done;
    }
}
