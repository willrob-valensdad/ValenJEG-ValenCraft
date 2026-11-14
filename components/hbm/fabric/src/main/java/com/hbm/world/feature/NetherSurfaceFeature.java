// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;

public class NetherSurfaceFeature extends HbmFeature<NetherSurfaceConfig> {

    public static final MapCodec<NetherSurfaceFeature> CODEC =
            HbmFeature.mapCodec(NetherSurfaceConfig.CODEC, NetherSurfaceFeature::new);

    public NetherSurfaceFeature(NetherSurfaceConfig config) {
        super(config);
    }

        public MapCodec<NetherSurfaceFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<NetherSurfaceConfig> ctx) {
        WorldGenLevel level = ctx.level();
        NetherSurfaceConfig cfg = ctx.config();
        RandomSource rand = ctx.random();
        int cx = ctx.origin().getX() & ~15;
        int cz = ctx.origin().getZ() & ~15;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int k = 0; k < cfg.attempts(); k++) {
            int x = cx + rand.nextInt(16);
            int z = cz + rand.nextInt(16);
            int d = cfg.minD() + rand.nextInt(cfg.dRange());
            for (int y = d - cfg.scanDown(); y <= d; y++) {
                pos.set(x, y + 1, z);
                if (level.getBlockState(pos).isAir()
                        && level.getBlockState(pos.setY(y)).is(Blocks.NETHERRACK)) {
                    level.setBlock(pos, cfg.state(), 2);
                }
            }
        }
        return true;
    }
}
