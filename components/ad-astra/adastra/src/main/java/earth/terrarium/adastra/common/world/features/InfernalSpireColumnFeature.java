package earth.terrarium.adastra.common.world.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.adastra.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import org.jetbrains.annotations.Nullable;

/*
 * This is a modified version of the BasaltColumnFeature which uses infernal spire blocks instead of basalt.
 */
public class InfernalSpireColumnFeature implements Feature {

    private static final Codec<InfernalSpireColumnFeature> INNER = RecordCodecBuilder.create(instance -> instance.group(
        IntProviders.POSITIVE_CODEC.fieldOf("height").forGetter(feature -> feature.height),
        IntProviders.NON_NEGATIVE_CODEC.fieldOf("reach").forGetter(feature -> feature.reach)
    ).apply(instance, InfernalSpireColumnFeature::new));

    public static final MapCodec<InfernalSpireColumnFeature> CODEC = INNER.fieldOf("config");

    private final IntProvider height;
    private final IntProvider reach;

    public InfernalSpireColumnFeature(IntProvider height, IntProvider reach) {
        this.height = height;
        this.reach = reach;
    }

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(
        Blocks.LAVA,
        Blocks.BEDROCK,
        Blocks.MAGMA_BLOCK,
        Blocks.SOUL_SAND,
        Blocks.NETHER_BRICKS,
        Blocks.NETHER_BRICK_FENCE,
        Blocks.NETHER_BRICK_STAIRS,
        Blocks.NETHER_WART,
        Blocks.CHEST,
        Blocks.SPAWNER
    );

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos origin) {
        int i = chunkGenerator.getSeaLevel();
        BlockPos blockPos = origin;
        if (!canPlaceAt(worldGenLevel, i, blockPos.mutable())) {
            return false;
        } else {
            int j = height.sample(randomSource);
            boolean bl = randomSource.nextFloat() < 0.9F;
            int k = Math.min(j, bl ? 5 : 8);
            int l = bl ? 50 : 15;
            boolean bl2 = false;

            for (BlockPos blockPos2 : BlockPos.randomBetweenClosed(
                randomSource, l, blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k
            )) {
                int m = j - blockPos2.distManhattan(blockPos);
                if (m >= 0) {
                    bl2 |= this.placeColumn(worldGenLevel, i, blockPos2, m, reach.sample(randomSource));
                }
            }

            return bl2;
        }
    }

    private boolean placeColumn(LevelAccessor level, int seaLevel, BlockPos pos, int distance, int reach) {
        boolean bl = false;

        for (BlockPos blockPos : BlockPos.betweenClosed(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach)) {
            int i = blockPos.distManhattan(pos);
            BlockPos blockPos2 = isAirOrLavaOcean(level, seaLevel, blockPos)
                ? findSurface(level, seaLevel, blockPos.mutable(), i)
                : findAir(level, blockPos.mutable(), i);
            if (blockPos2 != null) {
                int j = distance - i / 2;

                for (BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable(); j >= 0; --j) {
                    if (isAirOrLavaOcean(level, seaLevel, mutableBlockPos)) {
                        this.setBlock(level, mutableBlockPos, ModBlocks.INFERNAL_SPIRE_BLOCK.get().defaultBlockState());
                        mutableBlockPos.move(Direction.UP);
                        bl = true;
                    } else {
                        if (!level.getBlockState(mutableBlockPos).is(ModBlocks.INFERNAL_SPIRE_BLOCK.get())) {
                            break;
                        }

                        mutableBlockPos.move(Direction.UP);
                    }
                }
            }
        }

        return bl;
    }

    @Nullable
    private static BlockPos findSurface(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos, int distance) {
        while (pos.getY() > level.getMinBuildHeight() + 1 && distance > 0) {
            --distance;
            if (canPlaceAt(level, seaLevel, pos)) {
                return pos;
            }

            pos.move(Direction.DOWN);
        }

        return null;
    }

    private static boolean canPlaceAt(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos) {
        if (!isAirOrLavaOcean(level, seaLevel, pos)) {
            return false;
        } else {
            BlockState blockState = level.getBlockState(pos.move(Direction.DOWN));
            pos.move(Direction.UP);
            return !blockState.isAir() && !CANNOT_PLACE_ON.contains(blockState.getBlock());
        }
    }

    @Nullable
    private static BlockPos findAir(LevelAccessor level, BlockPos.MutableBlockPos pos, int distance) {
        while (pos.getY() < level.getMaxBuildHeight() && distance > 0) {
            --distance;
            BlockState blockState = level.getBlockState(pos);
            if (CANNOT_PLACE_ON.contains(blockState.getBlock())) {
                return null;
            }

            if (blockState.isAir()) {
                return pos;
            }

            pos.move(Direction.UP);
        }

        return null;
    }

    private static boolean isAirOrLavaOcean(LevelAccessor level, int seaLevel, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        return blockState.isAir() || blockState.is(Blocks.LAVA) && pos.getY() <= seaLevel;
    }
}
