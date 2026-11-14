package top.ribs.scguns.client.compat;

import net.minecraft.world.entity.LivingEntity;

/**
 * Mixin-backed attachment point for the live entity that produced an
 * ArmedEntityRenderState.
 *
 * <p>Minecraft 26.2 creates a fresh render-state object during extraction, so
 * the entity association belongs on that transient state itself rather than in
 * a process-lifetime static map.</p>
 */
public interface RenderStateEntityAccessor {
    void scguns$setEntity(LivingEntity entity);

    LivingEntity scguns$getEntity();
}
