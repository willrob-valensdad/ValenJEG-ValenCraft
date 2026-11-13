package ttv.migami.jeg.structure.mesh;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Static structure blocks. No fluid, radiation, damage, GUI, or block entity. */
public final class HbmMeshBlocks {
    public static final IntegerProperty META = IntegerProperty.create("meta", 0, 15);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    private HbmMeshBlocks() {}

    public static Block oriented(BlockBehaviour.Properties properties) {
        return new Block(properties) {
            { registerDefaultState(stateDefinition.any().setValue(META, 0).setValue(ROTATION, 0)); }
            @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(META, ROTATION); }
            @Override protected BlockState rotate(BlockState state, Rotation rotation) {
                return state.setValue(ROTATION, HbmQuarterTurn.compose(state.getValue(ROTATION), rotation));
            }
        };
    }

    /** Pole top has no source metadata. The quarter turn is the only orientation. */
    public static Block poleTop(BlockBehaviour.Properties properties) {
        return new Block(properties) {
            { registerDefaultState(stateDefinition.any().setValue(ROTATION, 0)); }
            @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(ROTATION); }
            @Override protected BlockState rotate(BlockState state, Rotation rotation) {
                return state.setValue(ROTATION, HbmQuarterTurn.compose(state.getValue(ROTATION), rotation));
            }
        };
    }

    /** Source BlockMetalFence extends BlockFence, so FenceBlock.rotate already turns the connections. */
    public static final class MetalFenceBlock extends FenceBlock {
        public MetalFenceBlock(BlockBehaviour.Properties properties) { super(properties); }
    }
}
