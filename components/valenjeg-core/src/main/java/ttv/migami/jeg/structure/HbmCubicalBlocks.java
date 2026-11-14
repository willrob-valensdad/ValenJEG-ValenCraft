package ttv.migami.jeg.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cubical blocks for the four Desert Atom ids that are not meshes.
 * YellowBarrelBlock keeps the voxel box from the naval static-prop proposal.
 * Fence and pole-top classes from that proposal are not registered here.
 */
public final class HbmCubicalBlocks {
    private HbmCubicalBlocks() {}

    public static final class YellowBarrelBlock extends Block {
        private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

        public YellowBarrelBlock(BlockBehaviour.Properties properties) { super(properties); }

        @Override protected VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) { return SHAPE; }
    }
}
