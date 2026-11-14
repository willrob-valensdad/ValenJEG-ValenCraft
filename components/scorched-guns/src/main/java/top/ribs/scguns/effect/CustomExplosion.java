package top.ribs.scguns.effect;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import top.ribs.scguns.compat.ScgunsServerExplosion;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModDamageTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static top.ribs.scguns.entity.projectile.MicroJetEntity.EXPLOSION_DAMAGE_MULTIPLIER;

public abstract class CustomExplosion extends ScgunsServerExplosion {
    final CustomBlockInteraction customBlockInteraction;
    protected final Level level;
    final Entity source;
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float radius;
    private static final float MAX_EXPLOSION_DAMAGE = 7.0F;

    public CustomExplosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ,
                           float pRadius, boolean pFire, CustomBlockInteraction customBlockInteraction) {
        super(pLevel, pSource, null, null, new Vec3(pToBlowX, pToBlowY, pToBlowZ), pRadius + 1.0F, pFire,
                customBlockInteraction == CustomBlockInteraction.DESTROY
                        ? Explosion.BlockInteraction.DESTROY
                        : Explosion.BlockInteraction.KEEP);
        this.customBlockInteraction = customBlockInteraction;
        this.level = pLevel;
        this.source = pSource;
        this.x = pToBlowX;
        this.y = pToBlowY;
        this.z = pToBlowZ;
        this.radius = pRadius + 1.0F;
    }

    @Override
    public int explode() {
        if (this.customBlockInteraction != CustomBlockInteraction.NONE) {
            return super.explode();
        }

        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> blocks = Sets.newHashSet();
        float diameter = this.radius * 2.0F;
        int minX = Mth.floor(this.x - (double) diameter - 1.0);
        int maxX = Mth.floor(this.x + (double) diameter + 1.0);
        int minY = Mth.floor(this.y - (double) diameter - 1.0);
        int maxY = Mth.floor(this.y + (double) diameter + 1.0);
        int minZ = Mth.floor(this.z - (double) diameter - 1.0);
        int maxZ = Mth.floor(this.z + (double) diameter + 1.0);
        List<Entity> entities = this.level.getEntities(this.source, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        EventHooks.onExplosionDetonate(this.level, this, entities, List.of());
        Vec3 center = new Vec3(this.x, this.y, this.z);

        for (Entity entity : entities) {
            if (entity instanceof ItemEntity || entity.ignoreExplosion(this)) {
                continue;
            }

            double normalizedDistance = Math.sqrt(entity.distanceToSqr(center)) / (double) diameter;
            if (normalizedDistance > 1.0D) {
                continue;
            }

            double dx = entity.getX() - this.x;
            double dy = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
            double dz = entity.getZ() - this.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance == 0.0D) {
                continue;
            }

            dx /= distance;
            dy /= distance;
            dz /= distance;
            double exposure = ServerExplosion.getSeenPercent(center, entity);
            double impact = (1.0D - normalizedDistance) * exposure;
            float explosionDamage = (float) ((impact * impact + impact) / 2.0D * 4.0D * (double) diameter + 1.0D)
                    * EXPLOSION_DAMAGE_MULTIPLIER;
            explosionDamage = Math.min(explosionDamage, MAX_EXPLOSION_DAMAGE);

            if (this.source instanceof ProjectileEntity projectile && projectile.getOwner() instanceof LivingEntity owner) {
                entity.hurt(ModDamageTypes.Sources.projectile(this.level.registryAccess(), projectile, owner), explosionDamage);
            } else {
                entity.hurt(this.getDamageSource(), explosionDamage);
            }

            double knockbackMultiplier = impact;
            if (entity instanceof LivingEntity livingEntity) {
                knockbackMultiplier *= 1.0D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            }

            Vec3 knockback = new Vec3(dx * knockbackMultiplier, dy * knockbackMultiplier, dz * knockbackMultiplier);
            knockback = EventHooks.getExplosionKnockback(this.level, this, entity, knockback, List.of());
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            if (entity instanceof Player player
                    && !player.isSpectator()
                    && (!player.isCreative() || !player.getAbilities().flying)) {
                this.getHitPlayers().put(player, knockback);
            }
        }

        return this.completeManualExplosion(blocks);
    }

    public enum CustomBlockInteraction {
        NONE,
        KEEP,
        DESTROY
    }
}
