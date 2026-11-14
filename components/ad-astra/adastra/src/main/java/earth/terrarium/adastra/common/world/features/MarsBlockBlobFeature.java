package earth.terrarium.adastra.common.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.adastra.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/*
 * Modified BlockBlobFeature. It still places the configured state on Mars sand.
 */
public record MarsBlockBlobFeature(BlockState state) implements Feature {

    private static final Codec<MarsBlockBlobFeature> INNER = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.fieldOf("state").forGetter(MarsBlockBlobFeature::state)
    ).apply(instance, MarsBlockBlobFeature::new));

    public static final MapCodec<MarsBlockBlobFeature> CODEC = INNER.fieldOf("config");

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos origin) {
        BlockPos blockPos = origin;
        for (; blockPos.getY() > worldGenLevel.getMinBuildHeight() + 3; blockPos = blockPos.below()) {
            if (!worldGenLevel.isEmptyBlock(blockPos.below())) {
                BlockState ground = worldGenLevel.getBlockState(blockPos.below());
                if (ground.is(ModBlocks.MARS_SAND.get())) break;
            }
        }

        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int j = randomSource.nextInt(2);
            int k = randomSource.nextInt(2);
            int l = randomSource.nextInt(2);
            float f = (float) (j + k + l) * 0.333F + 0.5F;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-j, -k, -l), blockPos.offset(j, k, l))) {
                if (blockPos2.distSqr(blockPos) <= (double) (f * f)) {
                    worldGenLevel.setBlock(blockPos2, state, 3);
                }
            }
            blockPos = blockPos.offset(-1 + randomSource.nextInt(2), -randomSource.nextInt(2), -1 + randomSource.nextInt(2));
        }
        return true;
    }
}
