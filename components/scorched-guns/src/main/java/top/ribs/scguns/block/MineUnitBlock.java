package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.blockentity.MineUnitBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModTags;

import net.minecraft.world.level.redstone.Orientation;
public class MineUnitBlock extends BaseEntityBlock {
    public static final MapCodec<MineUnitBlock> CODEC = BlockCodecs.simpleCodec(MineUnitBlock::new);
    public static final BooleanProperty PRIMED = BooleanProperty.create("primed");
    private static final VoxelShape UNPRIMED_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    private static final VoxelShape PRIMED_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.1D, 16.0D);

    public MineUnitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PRIMED, false));
    }

    protected MapCodec<MineUnitBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PRIMED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PRIMED) ? PRIMED_SHAPE : UNPRIMED_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MineUnitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.MINE_UNIT.get(), MineUnitBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MineUnitBlockEntity mineUnit)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (mineUnit.isPrimed()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!heldItem.is(ModTags.Items.GRENADES) || mineUnit.hasGrenade()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            ItemStack grenadeStack = heldItem.copy();
            grenadeStack.setCount(1);
            mineUnit.setGrenade(grenadeStack, player);
            if (!player.isCreative()) {
                heldItem.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.spawnLoadingParticles(level, pos);
        }
        return ((level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MineUnitBlockEntity mineUnit)) {
            return InteractionResult.PASS;
        }
        if (mineUnit.isPrimed()) {
            return InteractionResult.PASS;
        }
        if (mineUnit.hasGrenade()) {
            if (!level.isClientSide()) {
                mineUnit.setPrimed(true);
                level.setBlock(pos, state.setValue(PRIMED, true), 3);
                level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 0.8F, 1.2F);
                level.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 0.9F);
                this.spawnPrimingParticles(level, pos);
            } else {
                player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
            }
            return ((level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }

        if (level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("message.scguns.mine_unit.needs_grenade").withStyle(ChatFormatting.RED));
        }
        return ((level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    private void spawnLoadingParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double offsetX = pos.getX() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.4D;
                double offsetY = pos.getY() + 0.2D;
                double offsetZ = pos.getZ() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.4D;
                double velocityX = (level.getRandom().nextDouble() - 0.5D) * 0.1D;
                double velocityY = level.getRandom().nextDouble() * 0.05D;
                double velocityZ = (level.getRandom().nextDouble() - 0.5D) * 0.1D;
                serverLevel.sendParticles(ParticleTypes.SMOKE, offsetX, offsetY, offsetZ, 1, velocityX, velocityY, velocityZ, 0.05D);
            }
        }
    }

    private void spawnPrimingParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!belowState.isAir()) {
                for (int i = 0; i < 15; i++) {
                    double offsetX = pos.getX() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.6D;
                    double offsetY = pos.getY() + 0.1D;
                    double offsetZ = pos.getZ() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.6D;
                    double velocityX = (level.getRandom().nextDouble() - 0.5D) * 0.15D;
                    double velocityY = level.getRandom().nextDouble() * 0.1D;
                    double velocityZ = (level.getRandom().nextDouble() - 0.5D) * 0.15D;
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, belowState), offsetX, offsetY, offsetZ, 1, velocityX, velocityY, velocityZ, 0.1D);
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
