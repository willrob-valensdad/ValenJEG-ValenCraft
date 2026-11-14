package top.ribs.scguns.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

/** Extracted render-only data for {@code TraumaHookEntity}. */
public final class TraumaHookRenderState extends EntityRenderState {
    public boolean hasLine;
    public Vec3 lineDelta = Vec3.ZERO;
}
