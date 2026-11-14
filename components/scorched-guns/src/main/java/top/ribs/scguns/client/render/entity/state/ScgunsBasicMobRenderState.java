package top.ribs.scguns.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/**
 * Shared render-only state for the first batch of Scorched Guns mobs migrated
 * to Minecraft 26.2's extract/submit entity rendering architecture.
 */
public class ScgunsBasicMobRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public boolean muzzleFlashVisible;
}
