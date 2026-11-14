package earth.terrarium.adastra.common.world.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

/**
 * Moon crater carver. The datapack config (probability, surface-relative y,
 * replaceable tag, lava level, y scale) stays on the carver. The carved
 * ellipsoid keeps the previous 64 by 32 block radii.
 */
public record CraterWorldCarver(float probability, HeightProvider y, VerticalAnchor lavaLevel, TagKey<Block> replaceable, float yScale) implements WorldCarver {

    private static final Codec<CraterWorldCarver> INNER = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("probability").forGetter(CraterWorldCarver::probability),
        HeightProvider.CODEC.fieldOf("y").forGetter(CraterWorldCarver::y),
        VerticalAnchor.CODEC.fieldOf("lava_level").forGetter(CraterWorldCarver::lavaLevel),
        TagKey.codec(Registries.BLOCK).fieldOf("replaceable").forGetter(CraterWorldCarver::replaceable),
        Codec.FLOAT.fieldOf("yScale").forGetter(CraterWorldCarver::yScale)
    ).apply(instance, CraterWorldCarver::new));

    public static final MapCodec<CraterWorldCarver> CODEC = INNER.fieldOf("config");

    @Override
    public MapCodec<? extends WorldCarver> codec() {
        return CODEC;
    }

    @Override
    public boolean isStartChunk(RandomSource random) {
        return random.nextFloat() <= probability;
    }

    @Override
    public boolean carve(WorldGenerationContext context, RandomSource random, ChunkPos chunkPos, ChunkPos sourceChunk, CarverOutput output) {
        double centerX = sourceChunk.getMiddleBlockX() + 0.5;
        double centerZ = sourceChunk.getMiddleBlockZ() + 0.5;
        double centerY = y.sample(random, context);
        double radiusY = 32.0 * yScale;
        WorldCarver.carveEllipsoid(chunkPos, centerX, centerY, centerZ, 64.0, radiusY, output, (dx, dy, dz, yBlock) -> false);
        return true;
    }
}
