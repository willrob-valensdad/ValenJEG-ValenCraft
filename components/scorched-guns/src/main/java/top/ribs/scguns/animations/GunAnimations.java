package top.ribs.scguns.animations;

import com.geckolib.animation.object.LoopType;

import com.geckolib.animation.RawAnimation;
public final class GunAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation CARBINE_IDLE = RawAnimation.begin().thenLoop("carbine_idle");
    public static final RawAnimation SHOOT;
    public static final RawAnimation SHOOT1;
    public static final RawAnimation CARBINE_SHOOT;
    public static final RawAnimation AIM_SHOOT;
    public static final RawAnimation AIM_SHOOT1;
    public static final RawAnimation CARBINE_AIM_SHOOT;
    public static final RawAnimation RELOAD;
    public static final RawAnimation CARBINE_RELOAD;
    public static final RawAnimation RELOAD_ALT;
    public static final RawAnimation RELOAD_START;
    public static final RawAnimation CARBINE_RELOAD_START;
    public static final RawAnimation RELOAD_LOOP;
    public static final RawAnimation CARBINE_RELOAD_LOOP;
    public static final RawAnimation RELOAD_STOP;
    public static final RawAnimation CARBINE_RELOAD_STOP;
    public static final RawAnimation INSPECT;
    public static final RawAnimation CARBINE_INSPECT;
    public static final RawAnimation DRAW;
    public static final RawAnimation CARBINE_DRAW;
    public static final RawAnimation JAM;

    public GunAnimations() {}

    static {
        INSPECT = RawAnimation.begin().then("inspect", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_INSPECT = RawAnimation.begin().then("carbine_inspect", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        SHOOT = RawAnimation.begin().then("shoot", LoopType.PLAY_ONCE).thenLoop("idle");
        SHOOT1 = RawAnimation.begin().then("shoot1", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_SHOOT = RawAnimation.begin().then("carbine_shoot", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        AIM_SHOOT = RawAnimation.begin().then("aim_shoot", LoopType.PLAY_ONCE).thenLoop("idle");
        AIM_SHOOT1 = RawAnimation.begin().then("aim_shoot1", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_AIM_SHOOT = RawAnimation.begin().then("carbine_aim_shoot", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        RELOAD = RawAnimation.begin().then("reload", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_RELOAD = RawAnimation.begin().then("carbine_reload", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        RELOAD_ALT = RawAnimation.begin().then("reload_alt", LoopType.PLAY_ONCE).thenLoop("idle");
        RELOAD_START = RawAnimation.begin().then("reload_start", LoopType.PLAY_ONCE).thenLoop("reload_loop");
        CARBINE_RELOAD_START = RawAnimation.begin().then("carbine_reload_start", LoopType.PLAY_ONCE).thenLoop("carbine_reload_loop");
        RELOAD_LOOP = RawAnimation.begin().thenLoop("reload_loop");
        CARBINE_RELOAD_LOOP = RawAnimation.begin().thenLoop("carbine_reload_loop");
        RELOAD_STOP = RawAnimation.begin().then("reload_stop", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_RELOAD_STOP = RawAnimation.begin().then("carbine_reload_stop", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        DRAW = RawAnimation.begin().then("draw", LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_DRAW = RawAnimation.begin().then("carbine_draw", LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        JAM = RawAnimation.begin().then("jam", LoopType.PLAY_ONCE).thenLoop("idle");
    }
}