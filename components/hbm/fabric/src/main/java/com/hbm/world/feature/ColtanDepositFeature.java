// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.world.NtmWorldgenFields;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public class ColtanDepositFeature extends HbmFeature<ColtanDepositConfig> {

    public static final MapCodec<ColtanDepositFeature> CODEC =
            HbmFeature.mapCodec(ColtanDepositConfig.CODEC, ColtanDepositFeature::new);

    public ColtanDepositFeature(ColtanDepositConfig config) {
        super(config);
    }

        public MapCodec<ColtanDepositFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<ColtanDepositConfig> ctx) {
        ColtanDepositConfig cfg = ctx.config();
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        int cx = ctx.origin().getX() & ~15;
        int cz = ctx.origin().getZ() & ~15;

        NtmWorldgenFields fields = NtmWorldgenFields.get(level);
        int centerX = fields.coltanX(cfg.spread());
        int centerZ = fields.coltanZ(cfg.spread());
        int reach = cfg.baseRange();
        if (cx + 15 < centerX - reach
                || cx > centerX + reach
                || cz + 15 < centerZ - reach
                || cz > centerZ + reach) {
            return true;
        }
        BlockPos.MutableBlockPos veinPos = new BlockPos.MutableBlockPos();

        for (int k = 0; k < cfg.outerAttempts(); k++) {
            for (int r = 1; r <= cfg.rings(); r++) {
                int x = cx + rand.nextInt(16);
                int y = cfg.minY() + rand.nextInt(cfg.yRange());
                int z = cz + rand.nextInt(16);
                int range = cfg.baseRange() / r;
                if (x <= centerX + range
                        && x >= centerX - range
                        && z <= centerZ + range
                        && z >= centerZ - range) {
                    cfg.vein().place(level, ctx.chunkGenerator(), rand, veinPos.set(x, y, z));
                }
            }
        }
        return true;
    }
}
