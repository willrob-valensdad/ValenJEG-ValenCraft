package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.blockentity.PenetratorBlockEntity;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.world.level.redstone.Orientation;
public class PenetratorBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
    private final int tunnelLength;
    private final MapCodec<PenetratorBlock> codec;

    public PenetratorBlock(BlockBehaviour.Properties pProperties, int tunnelLength) {
        super(pProperties);
        this.tunnelLength = tunnelLength;
        this.codec = BlockCodecs.simpleCodec(properties -> new PenetratorBlock(properties, tunnelLength));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    protected MapCodec<PenetratorBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing;

        // If the player is crouching, use the clicked face direction
        if (pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown()) {
            facing = pContext.getClickedFace();
        } else {
            // Use the player's horizontal facing direction
            facing = pContext.getHorizontalDirection();
        }

        return this.defaultBlockState().setValue(FACING, facing);
    }

    // Override redstone connection methods
    @Override
    public boolean shouldRedstoneWireConnectTo(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        // Get the direction this penetrator is facing
        Direction facing = state.getValue(FACING);

        // The redstone connection point should be the visual back side (same as facing direction)
        Direction connectionSide = facing; // Changed: removed .getOpposite()

        // Debug: You can add this line temporarily to check what's happening
        // System.out.println("Facing: " + facing + ", Connection side: " + connectionSide + ", Requested side: " + side);

        // Allow connection if the side matches our connection side, or if side is null (for general queries)
        return side == null || side == connectionSide;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return false; // This block doesn't output redstone, only receives it
    }

    // Override the neighbor change detection to be more specific about redstone
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide()) {
            // Get the direction this penetrator is facing
            Direction facing = state.getValue(FACING);
            Direction connectionSide = facing; // Changed: removed .getOpposite()

            // Check if the changed neighbor is on our redstone connection side
            BlockPos connectionPos = pos.relative(connectionSide);

            boolean shouldActivate = false;
            // 26.2 no longer exposes the changed-neighbor BlockPos here.
            // Both legacy branches had the same effective condition.
            shouldActivate = level.getSignal(connectionPos, connectionSide) > 0
                    || level.hasNeighborSignal(pos);

            if (shouldActivate) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof PenetratorBlockEntity) {
                    PenetratorBlockEntity.tick(level, pos, state, (PenetratorBlockEntity) blockEntity);
                }
            }
        }
    }



    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PenetratorBlockEntity(pos, state);
    }

    public int getTunnelLength() {
        return this.tunnelLength;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
