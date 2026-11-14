package top.ribs.scguns.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Render-only state for Scorched Guns mob renderer migration batch 2. */
public class ScgunsMobRenderState extends ArmedEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public boolean muzzleFlashVisible;
    public boolean attacking;
    public int attackTimeout;
    public boolean charging;
    public int maskColor = -1;
    public boolean tame;
    public float lookYaw;
    public float lookPitch;

    // Batch 3 procedural mob state
    public boolean holdingItem;
    public boolean holdingGun;
    public boolean secondPhase;
    public boolean roaring;
    public int roarTick;
    public int attackVariation;
    public boolean defensive;
    public boolean partying;
    public boolean turretFlashVisible;
    public boolean mainTurretFlashVisible;
    public boolean machineGunFlashVisible;
    public boolean hasAimTarget;
    public float aimYaw;
    public float aimPitch;

    // Sulfurhead render state (Hotfix 29)
    public boolean primed;
    public float swelling;
}
