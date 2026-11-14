// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.world.NtmWorldgenFields;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public class StratumFeature extends HbmFeature<StratumConfig> {

    private static final int SIZE = 16;
    private static final ThreadLocal<Scratch> SCRATCH = ThreadLocal.withInitial(Scratch::new);

    public static final MapCodec<StratumFeature> CODEC =
            HbmFeature.mapCodec(StratumConfig.CODEC, StratumFeature::new);

    public StratumFeature(StratumConfig config) {
        super(config);
    }

        public MapCodec<StratumFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<StratumConfig> ctx) {
        WorldGenLevel level = ctx.level();
        StratumConfig cfg = ctx.config();
        RandomSource rand = ctx.random();

        int cx = ctx.origin().getX() & ~15;
        int cz = ctx.origin().getZ() & ~15;

        FractalSimplexNoise noise = NtmWorldgenFields.get(level).fractal(cfg.domain(), 4);

        Scratch scratch = SCRATCH.get();
        double[] noiseValues = scratch.noiseValues;
        noise.getGrid(cx, cz, SIZE, SIZE, cfg.scale(), cfg.scale(), noiseValues);
        BlockPos.MutableBlockPos pos = scratch.pos;

        int index = 0;
        for (int x = cx; x < cx + SIZE; x++) {
            for (int z = cz; z < cz + SIZE; z++) {

                double n = noiseValues[index++];
                int range =
                        NoiseBand.halfHeight(n, cfg.threshold(), cfg.rangeMult(), cfg.maxRange());
                if (range < 0) continue;

                for (int y = cfg.yLevel() - range; y <= cfg.yLevel() + range; y++) {
                    if (cfg.density() < 1.0F && rand.nextFloat() >= cfg.density()) continue;
                    pos.set(x, y, z);
                    if (cfg.replaceable().test(level.getBlockState(pos), pos, rand)) {
                        level.setBlock(pos, cfg.state(), 2);
                    }
                }
            }
        }
        return true;
    }

    private static final class Scratch {
        private final double[] noiseValues = new double[SIZE * SIZE];
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    }
}
