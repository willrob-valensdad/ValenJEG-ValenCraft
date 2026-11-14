package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: MrCrayfish
 */
public class PlayerModelHandler {
    private static final AtomicBoolean ACTIVE_INSTANCE_CREATED = new AtomicBoolean();
    private final boolean active = ACTIVE_INSTANCE_CREATED.compareAndSet(false, true);

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!this.active) {
            return;
        }

        AvatarRenderState renderState = event.getRenderState();
        Player player = resolvePlayer(renderState);
        if (player == null) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        GunRenderingHandler renderingHandler = GunRenderingHandler.get();
        if (!(event.getRenderer().getModel() instanceof PlayerModel model)) {
            return;
        }

        if (renderingHandler.isThirdPersonMeleeAttacking()) {
            applyCustomArmTransforms(event.getPoseStack(), event.getPartialTick(), model, player);
        } else if (!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);

            /*
             * The original 1.20.1 held animation writes its desired body yaw as
             *   player yaw + weapon offset.
             *
             * 26.2 renders from AvatarRenderState. GUI previews no longer rotate
             * the live Player; they rotate the render state instead. Replacing
             * renderState.bodyRot with the live entity yaw therefore loses the
             * GUI's ~180 degree facing transform and makes the inventory model
             * appear backwards.
             *
             * Let the legacy pose calculate only its weapon yaw offset, then
             * reproduce the old result from the render state's absolute head
             * direction. Adjust yRot at the same time so the head keeps looking
             * in the same world/screen direction, matching the old renderer.
             */
            float previousBodyRotO = player.yBodyRotO;
            float previousBodyRot = player.yBodyRot;
            float baseBodyRot = renderState.bodyRot;
            float baseHeadRelativeRot = renderState.yRot;
            try {
                gun.getGeneral().getGripType(heldItem).heldAnimation().applyPlayerPreRender(
                        player,
                        InteractionHand.MAIN_HAND,
                        AimingHandler.get().getAimProgress(player, event.getPartialTick()),
                        event.getPoseStack(),
                        event.getSubmitNodeCollector()
                );

                float previousWeaponOffset = Mth.wrapDegrees(player.yBodyRotO - player.yRotO);
                float currentWeaponOffset = Mth.wrapDegrees(player.yBodyRot - player.getYRot());
                float weaponOffset = Mth.lerp(event.getPartialTick(), previousWeaponOffset, currentWeaponOffset);

                float absoluteHeadRot = baseBodyRot + baseHeadRelativeRot;
                renderState.bodyRot = Mth.wrapDegrees(absoluteHeadRot + weaponOffset);
                renderState.yRot = Mth.wrapDegrees(absoluteHeadRot - renderState.bodyRot);
            } finally {
                player.yBodyRotO = previousBodyRotO;
                player.yBodyRot = previousBodyRot;
            }
        }
    }

    /*
     * No RenderPlayerEvent.Post model-part reset is needed on 26.2. Model.setupAnim()
     * resets every baked ModelPart before each submitted model is drawn. Keeping the old
     * immediate-render reset here would only mutate a model that the submit pipeline may
     * still reference later in the frame.
     */

    private static Player resolvePlayer(AvatarRenderState renderState) {
        Entity captured = RenderStateEntityBridge.resolve(renderState);
        if (captured instanceof Player player) {
            return player;
        }

        // Keep an ID lookup only as a compatibility fallback. The normal 26.2
        // path uses the entity captured while the render state is extracted.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        Entity entity = minecraft.level.getEntity(renderState.id);
        return entity instanceof Player player ? player : null;
    }

    private void applyCustomArmTransforms(PoseStack poseStack, float partialTicks, PlayerModel model, Player player) {
        GunRenderingHandler renderingHandler = GunRenderingHandler.get();
        float progress = Mth.lerp(partialTicks, renderingHandler.prevThirdPersonMeleeProgress, renderingHandler.thirdPersonMeleeProgress);

        // Apply transformations to the right arm.
        poseStack.pushPose();
        model.rightArm.translateAndRotate(poseStack);

        if (progress < 0.33f) {
            float raiseProgress = progress / 0.33f;
            poseStack.translate(0, 0.35 * raiseProgress, 0);
            poseStack.translate(0, 0, 0.1 * raiseProgress);
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(35F * raiseProgress)));
        } else if (progress < 0.66f) {
            float swingProgress = (progress - 0.33f) / 0.33f;
            poseStack.translate(0, 0.35 - 0.7 * swingProgress, 0);
            poseStack.translate(0, 0.1 - 0.2 * swingProgress, 0);
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(35F - 70F * swingProgress)));
        } else {
            float returnProgress = (progress - 0.66f) / 0.34f;
            poseStack.translate(0, -0.35 * (1 - returnProgress), 0);
            poseStack.translate(0, 0, -0.1 * (1 - returnProgress));
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(-35F * (1 - returnProgress))));
        }
        model.rightArm.translateAndRotate(poseStack);
        poseStack.popPose();
    }
}
