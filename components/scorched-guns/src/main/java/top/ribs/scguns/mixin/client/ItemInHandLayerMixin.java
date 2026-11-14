package top.ribs.scguns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.FreshAnimationsCompat;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Author: MrCrayfish
 *
 * Minecraft 26.2 player-specific replacement for the old renderArmWithItem
 * hook. PlayerItemInHandLayer overrides submitArmWithItem in 26.2, so the
 * Scorched Guns transform must be attached to that override directly.
 */
@Mixin(PlayerItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void scguns$submitArmWithItemHead(AvatarRenderState renderState,
                                              ItemStackRenderState itemRenderState,
                                              ItemStack stack,
                                              HumanoidArm arm,
                                              PoseStack poseStack,
                                              SubmitNodeCollector submitNodeCollector,
                                              int light,
                                              CallbackInfo ci) {
        InteractionHand hand = renderState.mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        if (hand == InteractionHand.OFF_HAND) {
            if (stack.getItem() instanceof GunItem) {
                ci.cancel();
                return;
            }

            ItemStack mainHandStack = renderState.getMainHandItemStack();
            if (mainHandStack.getItem() instanceof GunItem gunItem) {
                Gun modifiedGun = gunItem.getModifiedGun(mainHandStack);
                if (!modifiedGun.getGeneral().getGripType(mainHandStack).heldAnimation().canRenderOffhandItem()) {
                    ci.cancel();
                    return;
                }
            }
        }

        if (stack.getItem() instanceof GunItem gunItem) {
            ci.cancel();
            ItemInHandLayer<?, ?> layer = (ItemInHandLayer<?, ?>) (Object) this;
            LivingEntity entity = RenderStateEntityBridge.resolve(renderState);
            ItemDisplayContext display = arm == HumanoidArm.RIGHT
                    ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                    : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            scguns$submitArmWithGun(layer, renderState, entity, itemRenderState, stack, gunItem, display, hand, arm,
                    poseStack, submitNodeCollector, light,
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void scguns$submitArmWithGun(ItemInHandLayer<?, ?> layer,
                                                AvatarRenderState renderState,
                                                LivingEntity entity,
                                                ItemStackRenderState itemRenderState,
                                                ItemStack stack,
                                                GunItem item,
                                                ItemDisplayContext display,
                                                InteractionHand hand,
                                                HumanoidArm arm,
                                                PoseStack poseStack,
                                                SubmitNodeCollector submitNodeCollector,
                                                int light,
                                                float deltaTicks) {
        poseStack.pushPose();

        // Fresh Animations can animate the player model root itself. Held-item layers
        // do not inherit that root because they start from the entity matrix, so replay
        // it before translating through the final posed arm.
        if (entity instanceof Player player) {
            FreshAnimationsCompat.applyAnimatedRootToHeldGun(player, poseStack);
        }

        // The parent PlayerModel has already been posed for this render state.
        // Translate through that posed arm exactly like the 1.20.1 renderer.
        ArmedModel parentModel = (ArmedModel) layer.getParentModel();
        parentModel.translateToHand(renderState, arm, poseStack);

        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(-90F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(180F)));
        poseStack.translate(((float) (arm == HumanoidArm.LEFT ? -1 : 1) / 16F), 0.125, -0.625);
        GunRenderingHandler.get().applyWeaponScale(stack, poseStack);

        Gun gun = item.getModifiedGun(stack);
        if (entity instanceof Player player) {
            gun.getGeneral().getGripType(stack).heldAnimation().applyHeldItemTransforms(
                    player, hand, AimingHandler.get().getAimProgress(player, deltaTicks), poseStack, submitNodeCollector);
        }

        if (stack.getItem() instanceof AnimatedGunItem) {
            /*
             * Minecraft 26.2 has already extracted the complete item render state
             * for this exact hand/display context before PlayerItemInHandLayer runs.
             * Re-submit that state after Scorched Guns applies its custom grip pose.
             *
             * Do not rebuild the old 1.20.1 static ModelOverrides path here: in
             * 26.2 that bypasses the GeckoLib special item renderer and can resolve
             * the modular standalone pieces to Minecraft's missing-model cube.
             */
            itemRenderState.submit(
                    poseStack,
                    submitNodeCollector,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    renderState.outlineColor
            );
        } else {
            // Defensive compatibility fallback for any future non-animated GunItem.
            GunRenderingHandler.get().renderWeapon(
                    entity, stack, display, poseStack, submitNodeCollector, light, deltaTicks);
        }
        poseStack.popPose();
    }
}
