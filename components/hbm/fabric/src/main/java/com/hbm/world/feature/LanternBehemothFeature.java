// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockLanternBehemoth;
import com.hbm.blocks.generic.BlockLoot;
import com.hbm.util.LootGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class LanternBehemothFeature extends HbmFeature<HbmUnit> {

    public static final MapCodec<LanternBehemothFeature> CODEC = HbmFeature.unitCodec(LanternBehemothFeature::new);

    public LanternBehemothFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<LanternBehemothFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> context) {
        var level = context.level();
        BlockPos pos = context.origin();
        if (!LockedSafeFeature.canPlaceTorchOnTop(level.getBlockState(pos.below()))
                || !isReplaceable(level.getBlockState(pos))) return false;

        BlockLanternBehemoth block = ModBlocks.LANTERN_BEHEMOTH.get();
        BlockState state =
                block.defaultBlockState()
                        .setValue(BlockLanternBehemoth.FACING, Direction.NORTH)
                        .setValue(BlockLanternBehemoth.BROKEN, true);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        block.fillSpace(level, pos, Direction.NORTH);

        if (context.random().nextInt(2) == 0) {
            BlockPos loot = pos.north(2);
            BlockLoot.place(
                    level,
                    loot,
                    LootGenerator.roll(
                            LootGenerator.LOOT_BOOKLET,
                            level.getLevel(),
                            context.random(),
                            level.getSeed()));
        }
        return true;
    }

    public static boolean isReplaceable(BlockState state) {
        return state.canBeReplaced() && !(state.getBlock() instanceof DoublePlantBlock);
    }
}
