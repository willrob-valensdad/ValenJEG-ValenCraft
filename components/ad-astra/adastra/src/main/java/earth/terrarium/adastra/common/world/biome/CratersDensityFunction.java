package earth.terrarium.adastra.common.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.adastra.AdAstra;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * Crater mask. 26.3 density functions cannot extend BlendedNoise, so the world
 * seed comes from the compile context and the easing curve is unchanged.
 */
public record CratersDensityFunction(double scale) implements DensityFunction {

    public static final MapCodec<CratersDensityFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.doubleRange(Double.MIN_VALUE, 1).fieldOf("scale").forGetter(CratersDensityFunction::scale)
    ).apply(instance, CratersDensityFunction::new));

    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.of(0.0F, 1.0F);
    }

    @Override
    public int domainAxes() {
        return AXIS_X | AXIS_Z;
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        long seed = context.createRandom(Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "craters")).nextLong();
        double craterScale = scale;
        return new DensitySampler() {
            @Override
            public void sampleVolume(SamplerContext samplerContext, DensityBuffer buffer, DensityVolume volume) {
                DensitySampler.sampleVolumeNaive(samplerContext, buffer, volume, this);
            }

            @Override
            public float sampleValue(SamplerContext samplerContext, int x, int y, int z) {
                double value = 1 - Math.min(CellularNoise.getValue(seed, x / (craterScale * 100), z / (craterScale * 100)), 1);
                value *= value;
                value *= value;
                value *= value;
                value *= value;
                return (float) value;
            }
        };
    }
}
