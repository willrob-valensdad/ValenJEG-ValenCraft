// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCrashedBomb.EnumDudType;
import com.hbm.world.LocationIsValidSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public class DudFeature extends HbmFeature<HbmUnit> {

    private static final EnumDudType[] DUD_TYPES = EnumDudType.values();

    public static final MapCodec<DudFeature> CODEC = HbmFeature.unitCodec(DudFeature::new);

    public DudFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<DudFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos pos = ctx.origin();

        if (!level.getBlockState(pos).isAir()
                || !LocationIsValidSpawn.isValidGround(
                        level.getBlockState(pos.below()), level.getBlockState(pos.below(2))))
            return false;

        EnumDudType type = DUD_TYPES[rand.nextInt(DUD_TYPES.length)];
        level.setBlock(
                pos, ModBlocks.CRASHED_BOMBS.get(type.ordinal()).get().defaultBlockState(), 3);
        return true;
    }
}
