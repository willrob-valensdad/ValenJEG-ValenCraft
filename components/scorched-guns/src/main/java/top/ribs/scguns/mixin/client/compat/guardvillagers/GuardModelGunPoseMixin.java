package top.ribs.scguns.mixin.client.compat.guardvillagers;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;
import top.ribs.scguns.client.render.pose.AimPose;
import top.ribs.scguns.client.render.pose.LimbPose;
import top.ribs.scguns.client.render.pose.WeaponPose;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import java.lang.reflect.Method;

/**
 * Guard Villagers 4.x uses Minecraft's render-state model pipeline. Reapply the
 * Scorched Guns arm pose after the Guard model has completed its own animation
 * so the held gun follows the same two-hand pose used by other gunner mobs.
 *
 * The target classes are optional and are referenced by name only; Guard
 * Villagers is not a compile-time or runtime dependency of Scorched Guns.
 */
@Pseudo
@Mixin(
        targets = {
                "tallestegg.guardvillagers.client.models.GuardModel",
                "tallestegg.guardvillagers.client.models.GuardSteveModel"
        },
        remap = false
)
public class GuardModelGunPoseMixin {
    @Inject(
            method = "setupAnim(Ltallestegg/guardvillagers/client/renderer/state/GuardRenderState;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void scguns$applyGunPose(@Coerce Object renderStateObject, CallbackInfo ci) {
        if (!(renderStateObject instanceof ArmedEntityRenderState renderState)) {
            return;
        }

        LivingEntity entity = RenderStateEntityBridge.resolve(renderState);
        if (entity == null) {
            return;
        }

        ItemStack heldItem = entity.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Gun gun = gunItem.getModifiedGun(heldItem);
        Object heldAnimation = gun.getGeneral().getGripType(heldItem).heldAnimation();
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        if (heldAnimation instanceof WeaponPose weaponPose) {
            AimPose forwardPose = scguns$getForwardPose(weaponPose);
            if (forwardPose != null) {
                float aimProgress = entity instanceof Mob mob && mob.getTarget() != null ? 1.0F : 0.0F;
                scguns$applyForwardPose(forwardPose, model.rightArm, model.leftArm, aimProgress);
                return;
            }
        }

        scguns$applyFallbackPose(model.rightArm, model.leftArm);
    }

    private static AimPose scguns$getForwardPose(WeaponPose weaponPose) {
        try {
            Method method = WeaponPose.class.getDeclaredMethod("getForwardPose");
            method.setAccessible(true);
            return (AimPose) method.invoke(weaponPose);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void scguns$applyForwardPose(AimPose forwardPose, ModelPart rightArm, ModelPart leftArm, float aimProgress) {
        scguns$applyLimbPose(forwardPose.getIdle().getRightArm(), forwardPose.getAiming().getRightArm(), rightArm, aimProgress, 1.0F);
        scguns$applyLimbPose(forwardPose.getIdle().getLeftArm(), forwardPose.getAiming().getLeftArm(), leftArm, aimProgress, 1.0F);
    }

    private static void scguns$applyLimbPose(LimbPose idlePose, LimbPose aimingPose, ModelPart modelPart, float aimProgress, float handedness) {
        if (idlePose == null) {
            return;
        }
        if (aimingPose == null) {
            aimingPose = idlePose;
        }

        modelPart.xRot = scguns$lerpRotation(idlePose.getRotationAngleX(), aimingPose.getRotationAngleX(), modelPart.xRot, aimProgress, 1.0F);
        modelPart.yRot = scguns$lerpRotation(idlePose.getRotationAngleY(), aimingPose.getRotationAngleY(), modelPart.yRot, aimProgress, handedness);
        modelPart.zRot = scguns$lerpRotation(idlePose.getRotationAngleZ(), aimingPose.getRotationAngleZ(), modelPart.zRot, aimProgress, handedness);

        modelPart.x = scguns$lerp(idlePose.getRotationPointX(), aimingPose.getRotationPointX(), modelPart.x, aimProgress, handedness);
        modelPart.y = scguns$lerp(idlePose.getRotationPointY(), aimingPose.getRotationPointY(), modelPart.y, aimProgress, 1.0F);
        modelPart.z = scguns$lerp(idlePose.getRotationPointZ(), aimingPose.getRotationPointZ(), modelPart.z, aimProgress, 1.0F);
    }

    private static float scguns$lerp(Float idleValue, Float aimingValue, float fallback, float progress, float multiplier) {
        if (idleValue == null && aimingValue == null) {
            return fallback;
        }
        float start = idleValue != null ? idleValue : fallback;
        float end = aimingValue != null ? aimingValue : start;
        return (start + (end - start) * progress) * multiplier;
    }

    private static float scguns$lerpRotation(Float idleDegrees, Float aimingDegrees, float fallbackRadians, float progress, float multiplier) {
        if (idleDegrees == null && aimingDegrees == null) {
            return fallbackRadians;
        }
        float start = idleDegrees != null ? idleDegrees : (float) Math.toDegrees(fallbackRadians);
        float end = aimingDegrees != null ? aimingDegrees : start;
        return (float) Math.toRadians((start + (end - start) * progress) * multiplier);
    }

    private static void scguns$applyFallbackPose(ModelPart rightArm, ModelPart leftArm) {
        rightArm.xRot = (float) Math.toRadians(-80F);
        rightArm.yRot = (float) Math.toRadians(-10F);
        rightArm.zRot = 0F;

        leftArm.xRot = (float) Math.toRadians(-35F);
        leftArm.yRot = (float) Math.toRadians(15F);
        leftArm.zRot = 0F;
    }
}
