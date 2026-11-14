package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModCauldronInteractions;
import top.ribs.scguns.init.ModParticleTypes;

import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.entity.InsideBlockEffectApplier;
public class ViciousAcidCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<ViciousAcidCauldronBlock> CODEC = BlockCodecs.simpleCodec(ViciousAcidCauldronBlock::new);
    private static final float ENCHANTMENT_REMOVAL_CHANCE = 0.2F;

    public ViciousAcidCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, ModCauldronInteractions.VICIOUS_ACID);
    }

    protected MapCodec<? extends AbstractCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return 0.9375D;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (this.isEntityInsideAcid(state, pos, entity)) {
            ViciousAcidBlock.applyAcidEffects(level, pos, entity, ENCHANTMENT_REMOVAL_CHANCE);
        }
    }

    private boolean isEntityInsideAcid(BlockState state, BlockPos pos, Entity entity) {
        double surfaceY = (double) pos.getY() + this.getContentHeight(state);
        return entity.getY() < surfaceY && entity.getBoundingBox().maxY > (double) pos.getY() + 0.25D;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.95D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ModParticleTypes.ACID_BUBBLE.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
