package ttv.migami.jeg.station;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import ttv.migami.jeg.init.ModBlockEntities;

public final class StationBlock extends BaseEntityBlock {
    public static final Property<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public StationBlock(BlockBehaviour.Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)); }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING,r.rotate(s.getValue(FACING))); }
    protected BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return StationShapes.get(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this).getPath(),state.getValue(FACING));
    }
    public net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(BlockState state,BlockGetter level,BlockPos pos) {
        return StationShapes.get(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this).getPath(),state.getValue(FACING));
    }
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new StationBlockEntity(p,s); }
    protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
        if (!l.isClientSide() && player instanceof ServerPlayer server && l.getBlockEntity(p) instanceof StationBlockEntity station) server.openMenu(station);
        return l.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return l.isClientSide() ? null : createTickerHelper(t,ModBlockEntities.STATION.get(),StationBlockEntity::serverTick);
    }
}
