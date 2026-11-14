// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;

public final class KeyholeFeature extends HbmFeature<HbmUnit> {
    public static final MapCodec<KeyholeFeature> CODEC = HbmFeature.unitCodec(KeyholeFeature::new);

    public KeyholeFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<KeyholeFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> context) {
        var random = context.random();
        if (random.nextInt(4) != 0) return false;
        int x = (context.origin().getX() & ~15) + random.nextInt(16);
        int y = 6 + random.nextInt(13);
        int z = (context.origin().getZ() & ~15) + random.nextInt(16);
        BlockPos pos = new BlockPos(x, y, z);
        if (!context.level().getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) return false;
        return context.level().setBlock(pos, ModBlocks.STONE_KEYHOLE.get().defaultBlockState(), 2);
    }
}
