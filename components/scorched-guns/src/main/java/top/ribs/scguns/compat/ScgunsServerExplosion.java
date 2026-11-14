package top.ribs.scguns.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Compatibility surface for Scorched Guns' pre-26.2 two-phase explosions.
 *
 * Minecraft 26.2 made {@link Explosion} an interface and moved the executable
 * implementation to {@link ServerExplosion}.  Scorched Guns still needs the
 * old affected-block list for projectile callbacks and its custom finalization
 * pass, so this class keeps that small compatibility surface while delegating
 * the actual Explosion contract to ServerExplosion.
 */
public class ScgunsServerExplosion extends ServerExplosion {
    private final ServerLevel scgunsLevel;
    private final List<BlockPos> affectedBlocks = new ArrayList<>();
    private final boolean createsFire;
    private boolean vanillaExplosionCompleted;
    private int lastBlockCount;

    protected ScgunsServerExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource,
                                    @Nullable ExplosionDamageCalculator damageCalculator, Vec3 center, float radius,
                                    boolean fire, Explosion.BlockInteraction blockInteraction) {
        super(requireServerLevel(level), source, damageSource, damageCalculator, center, radius, fire, blockInteraction);
        this.scgunsLevel = requireServerLevel(level);
        this.createsFire = fire;
    }

    protected static ServerLevel requireServerLevel(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }
        throw new IllegalStateException("Scorched Guns explosions must be created on a ServerLevel");
    }

    @Override
    public int explode() {
        this.vanillaExplosionCompleted = true;
        this.lastBlockCount = super.explode();
        return this.lastBlockCount;
    }

    protected final int completeManualExplosion(Collection<BlockPos> blocks) {
        this.affectedBlocks.clear();
        this.affectedBlocks.addAll(blocks);
        this.lastBlockCount = this.interactsWithBlocks() ? this.affectedBlocks.size() : 0;
        return this.lastBlockCount;
    }

    public final List<BlockPos> getToBlow() {
        return this.affectedBlocks;
    }

    public final void clearToBlow() {
        this.affectedBlocks.clear();
        this.lastBlockCount = 0;
    }

    public final boolean interactsWithBlocks() {
        return this.getBlockInteraction() != Explosion.BlockInteraction.KEEP;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return ParticleTypes.EXPLOSION;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return ParticleTypes.EXPLOSION_EMITTER;
    }

    public Holder<SoundEvent> getExplosionSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    /**
     * Compatibility replacement for the removed Explosion#finalizeExplosion.
     * Manual Scorched Guns explosions still destroy their precomputed block
     * list here; explosions delegated to ServerExplosion already processed
     * blocks inside ServerExplosion#explode().
     */
    public void finalizeExplosion(boolean spawnParticles) {
        if (!this.vanillaExplosionCompleted && this.interactsWithBlocks()) {
            this.interactWithAffectedBlocks();
        }

        if (!this.vanillaExplosionCompleted && this.createsFire) {
            this.createFire();
        }

        if (spawnParticles) {
            this.sendExplosionPacketToNearbyPlayers();
        }
    }

    private void interactWithAffectedBlocks() {
        for (BlockPos pos : List.copyOf(this.affectedBlocks)) {
            BlockState state = this.scgunsLevel.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            state.onExplosionHit(this.scgunsLevel, pos, this,
                    (stack, dropPos) -> Block.popResource(this.scgunsLevel, dropPos, stack));
        }
    }

    private void createFire() {
        for (BlockPos pos : this.affectedBlocks) {
            if (this.scgunsLevel.getRandom().nextInt(3) != 0 || !this.scgunsLevel.getBlockState(pos).isAir()) {
                continue;
            }

            BlockState fireState = BaseFireBlock.getState(this.scgunsLevel, pos);
            if (fireState.canSurvive(this.scgunsLevel, pos)) {
                this.scgunsLevel.setBlockAndUpdate(pos, fireState);
            }
        }
    }

    protected void sendExplosionPacketToNearbyPlayers() {
        Vec3 center = this.center();
        int blockCount = this.interactsWithBlocks() ? this.lastBlockCount : 0;
        ParticleOptions explosionParticle = this.isSmall()
                ? this.getSmallExplosionParticles()
                : this.getLargeExplosionParticles();
        WeightedList<ExplosionParticleInfo> blockParticles = WeightedList.of(
                new ExplosionParticleInfo(ParticleTypes.POOF, 1.0F, 1.0F));

        for (ServerPlayer player : this.scgunsLevel.players()) {
            if (player.distanceToSqr(center.x, center.y, center.z) >= 4096.0D) {
                continue;
            }

            player.connection.send(new ClientboundExplodePacket(
                    center,
                    this.radius(),
                    blockCount,
                    Optional.ofNullable(this.getHitPlayers().get(player)),
                    explosionParticle,
                    this.getExplosionSound(),
                    blockParticles,
                    true
            ));
        }
    }
}
