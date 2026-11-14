// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.world.LocationIsValidSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class MeteoriteFeature extends HbmFeature<HbmUnit> {

    public static final MapCodec<MeteoriteFeature> CODEC = HbmFeature.unitCodec(MeteoriteFeature::new);

    public MeteoriteFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<MeteoriteFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos source = ctx.origin();
        BlockPos.MutableBlockPos origin =
                new BlockPos.MutableBlockPos(
                        source.getX(), source.getY() - rand.nextInt(10), source.getZ());

        BlockState below2 = level.getBlockState(origin.move(0, -2, 0));
        origin.move(0, 2, 0);
        if (!LocationIsValidSpawn.isDryGround(below2) || origin.getY() <= level.getMinY() + 1)
            return false;

        Meteorite.INSTANCE.generate(level, rand, origin, false, false, false);
        return true;
    }
}
