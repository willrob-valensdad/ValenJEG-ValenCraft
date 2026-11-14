package top.ribs.scguns.client.compat;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

/**
 * Associates Minecraft's transient armed render state with the live entity
 * from which it was extracted.
 *
 * <p>The 26.2 port originally used a static IdentityHashMap here. Vanilla 26.2
 * creates a fresh render-state object for extraction, so retaining those keys
 * process-wide made the map grow with rendered frames and could also retain
 * old world render state. The association now lives directly on the render
 * state through a mixin-backed accessor, matching the lifetime of the state.</p>
 *
 * <p>The public bridge methods are intentionally preserved so existing Scorched
 * Guns code and addon integrations do not need to change.</p>
 */
public final class RenderStateEntityBridge {
    private RenderStateEntityBridge() {
    }

    public static void capture(ArmedEntityRenderState renderState, LivingEntity entity) {
        if ((Object) renderState instanceof RenderStateEntityAccessor accessor) {
            accessor.scguns$setEntity(entity);
        }
    }

    public static LivingEntity resolve(ArmedEntityRenderState renderState) {
        if ((Object) renderState instanceof RenderStateEntityAccessor accessor) {
            return accessor.scguns$getEntity();
        }
        return null;
    }
}
