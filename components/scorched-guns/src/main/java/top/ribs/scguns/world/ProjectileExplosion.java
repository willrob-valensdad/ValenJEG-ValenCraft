package top.ribs.scguns.world;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import top.ribs.scguns.compat.SableBlockInteraction;
import top.ribs.scguns.compat.ScgunsServerExplosion;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Scorched Guns projectile explosion migrated to Minecraft 26.2's
 * ServerExplosion contract while retaining the mod's ray-marched block list.
 */
public abstract class ProjectileExplosion extends ScgunsServerExplosion {
    private static final ExplosionDamageCalculator DEFAULT_CONTEXT = new ExplosionDamageCalculator();

    protected final Level world;
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float size;
    protected final Entity exploder;
    protected final ExplosionDamageCalculator context;
    private final DamageSource damageSource;

    public ProjectileExplosion(Level world, Entity exploder, @Nullable DamageSource source,
                               @Nullable ExplosionDamageCalculator context, double x, double y, double z,
                               float size, boolean causesFire, Explosion.BlockInteraction mode) {
        super(world, exploder, source, context, new Vec3(x, y, z), size, causesFire, mode);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
        this.exploder = exploder;
        this.context = context == null ? DEFAULT_CONTEXT : context;
        this.damageSource = source == null ? this.getDamageSource() : source;
    }

    @Override
    public int explode() {
        Set<BlockPos> affectedBlocks = Sets.newHashSet();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (x != 0 && x != 15 && y != 0 && y != 15 && z != 0 && z != 15) {
                        continue;
                    }

                    double dx = (float) x / 15.0F * 2.0F - 1.0F;
                    double dy = (float) y / 15.0F * 2.0F - 1.0F;
                    double dz = (float) z / 15.0F * 2.0F - 1.0F;
                    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= length;
                    dy /= length;
                    dz /= length;
                    float strength = this.size * (0.7F + this.world.getRandom().nextFloat() * 0.6F);
                    double blockX = this.x;
                    double blockY = this.y;
                    double blockZ = this.z;

                    for (; strength > 0.0F; strength -= 0.225F) {
                        BlockPos pos = BlockPos.containing(blockX, blockY, blockZ);
                        BlockState blockState = this.world.getBlockState(pos);
                        FluidState fluidState = this.world.getFluidState(pos);
                        Optional<Float> resistance = this.context.getBlockExplosionResistance(this, this.world, pos, blockState, fluidState);
                        if (resistance.isPresent()) {
                            strength -= (resistance.get() + 0.3F) * 0.3F;
                        }
                        strength = SableBlockInteraction.collectExplosionBlocks(
                                this.world, this, this.context, affectedBlocks, pos, blockState, strength);

                        if (strength > 0.0F && this.context.shouldBlockExplode(this, this.world, pos, blockState, strength)) {
                            affectedBlocks.add(pos);
                        }

                        blockX += dx * 0.3F;
                        blockY += dy * 0.3F;
                        blockZ += dz * 0.3F;
                    }
                }
            }
        }

        this.completeManualExplosion(affectedBlocks);

        float diameter = this.size * 2.0F;
        int minX = Mth.floor(this.x - (double) diameter - 1.0D);
        int maxX = Mth.floor(this.x + (double) diameter + 1.0D);
        int minY = Mth.floor(this.y - (double) diameter - 1.0D);
        int maxY = Mth.floor(this.y + (double) diameter + 1.0D);
        int minZ = Mth.floor(this.z - (double) diameter - 1.0D);
        int maxZ = Mth.floor(this.z + (double) diameter + 1.0D);
        List<Entity> entities = this.world.getEntities(this.exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

        EventHooks.onExplosionDetonate(this.world, this, entities, this.getToBlow());

        Vec3 explosionPos = new Vec3(this.x, this.y, this.z);
        for (Entity entity : entities) {
            if (entity.ignoreExplosion(this)) {
                continue;
            }

            double normalizedDistance = Math.sqrt(entity.distanceToSqr(explosionPos)) / diameter;
            if (normalizedDistance > 1.0D) {
                continue;
            }

            double dx = entity.getX() - this.x;
            double dy = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
            double dz = entity.getZ() - this.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance != 0.0D) {
                dx /= distance;
                dy /= distance;
                dz /= distance;
            } else {
                dx = 0.0D;
                dy = 1.0D;
                dz = 0.0D;
            }

            double exposure = ServerExplosion.getSeenPercent(explosionPos, entity);
            double damageFactor = (1.0D - normalizedDistance) * exposure;
            entity.hurt(this.damageSource,
                    (float) ((int) ((damageFactor * damageFactor + damageFactor) / 2.0D * 7.0D * (double) diameter + 1.0D)));

            double blastDamage = damageFactor;
            if (entity instanceof LivingEntity livingEntity) {
                blastDamage *= 1.0D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            }

            Vec3 knockback = new Vec3(dx * blastDamage, dy * blastDamage, dz * blastDamage);
            knockback = EventHooks.getExplosionKnockback(this.world, this, entity, knockback, this.getToBlow());
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));

            if (entity instanceof Player player
                    && !player.isSpectator()
                    && (!player.isCreative() || !player.getAbilities().flying)) {
                this.getHitPlayers().put(player, knockback);
            }
        }

        return this.getToBlow().size();
    }

    protected abstract float getEntityDamageAmount(Entity entity, double distance);

    protected DamageSource getExplosionDamageSource() {
        return this.damageSource;
    }
}
