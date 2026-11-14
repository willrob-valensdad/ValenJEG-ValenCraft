package top.ribs.scguns.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;
import top.ribs.scguns.block.GeothermalVentBlock;
import top.ribs.scguns.init.ModBlocks;

public class GeothermalVentFeature implements Feature {
    public static final MapCodec<GeothermalVentFeature> CODEC = MapCodec.unit(GeothermalVentFeature::new);
    @Override public MapCodec<? extends Feature> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos pos) {
        int height = Mth.nextInt(random, 1, 5);
        BlockPos magmaPos = pos.below();
        BlockState belowBlockState = world.getBlockState(magmaPos);
        if ((belowBlockState.is(Blocks.GRAVEL) || belowBlockState.is(Blocks.SAND)) &&
                world.getBlockState(magmaPos.below()).isFaceSturdy(world, magmaPos.below(), Direction.UP)) {
            world.setBlock(magmaPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            return false;
        }
        for (int i = 0; i < height; i++) {
            BlockPos currentPos = pos.above(i);
            BlockState state = ModBlocks.GEOTHERMAL_VENT.get().defaultBlockState()
                    .setValue(GeothermalVentBlock.VENT_TYPE, i == 0 ? GeothermalVentBlock.GeothermalVentType.BASE :
                            (i == height - 1 ? GeothermalVentBlock.GeothermalVentType.TOP : GeothermalVentBlock.GeothermalVentType.MIDDLE))
                    .setValue(GeothermalVentBlock.WATERLOGGED, true)
                    .setValue(GeothermalVentBlock.ACTIVE, true);  // Set the vent to active

            if (world.getBlockState(currentPos).canBeReplaced() && world.getBlockState(currentPos.below()).getBlock() != Blocks.AIR) {
                world.setBlock(currentPos, state, 3);
                world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
            } else {
                return false;
            }
        }

        return true;
    }
}