package ttv.migami.jeg.legacy;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AmmoBoxBlock extends BaseEntityBlock {
    public static final Property<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public AmmoBoxBlock(BlockBehaviour.Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AmmoBoxBlockEntity(pos,state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis()==Direction.Axis.Z ? Shapes.or(Block.box(2,0,5,14,7,11),Block.box(1,7,5,15,9,11)) : Shapes.or(Block.box(5,0,2,11,7,14),Block.box(5,7,1,11,9,15));
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !player.isSpectator() && level.getBlockEntity(pos) instanceof AmmoBoxBlockEntity box) { box.releaseLegacyOverflow(); player.openMenu(box); player.awardStat(Stats.OPEN_CHEST); }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
    private static ItemStack packed(AmmoBoxBlockEntity box) { ItemStack stack = new ItemStack(LegacyContent.AMMO_BOX_ITEM); stack.applyComponents(box.collectComponents()); return stack; }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof AmmoBoxBlockEntity box) return List.of(packed(box));
        return List.of(new ItemStack(LegacyContent.AMMO_BOX_ITEM));
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative() && level.getBlockEntity(pos) instanceof AmmoBoxBlockEntity box && !box.isEmpty()) { ItemEntity item=new ItemEntity(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,packed(box)); item.setDefaultPickUpDelay(); level.addFreshEntity(item); }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) { return level.getBlockEntity(pos) instanceof AmmoBoxBlockEntity box ? AbstractContainerMenu.getRedstoneSignalFromContainer(box) : 0; }
    @Override protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) { level.updateNeighbourForOutputSignal(pos,this); super.affectNeighborsAfterRemoval(state,level,pos,moving); }
}
