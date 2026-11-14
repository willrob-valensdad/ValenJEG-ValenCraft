// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blockentity.bomb.BlockEntityLandmine;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.BalanceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;

public final class BosniaMineFeature extends HbmFeature<HbmUnit> {

    public static final MapCodec<BosniaMineFeature> CODEC = HbmFeature.unitCodec(BosniaMineFeature::new);

    public BosniaMineFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<BosniaMineFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> context) {
        if (!BalanceConfig.enable528BosniaSimulator) return false;
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        if (!LockedSafeFeature.canPlaceTorchOnTop(level.getBlockState(pos.below()))) return false;

        level.setBlock(pos, ModBlocks.MINE_HE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof BlockEntityLandmine mine)
            mine.deferUntilPlayerNear();
        return true;
    }
}
