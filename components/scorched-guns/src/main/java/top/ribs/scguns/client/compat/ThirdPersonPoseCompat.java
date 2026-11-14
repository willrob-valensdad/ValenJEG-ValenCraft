package top.ribs.scguns.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.WaraxeItem;

/**
 * Shared 26.2 bridge that reapplies Scorched Guns' legacy player pose after
 * vanilla has finished animating a player/armor humanoid model.
 */
public final class ThirdPersonPoseCompat {
    private ThirdPersonPoseCompat() {
    }

    public static void apply(HumanoidModel<?> model, HumanoidRenderState renderState, Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof WaraxeItem waraxeItem) {
            waraxeItem.applyHoldingPose(model, player, renderState.attackTime);
            return;
        }

        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        /*
         * Keep the original local-player safeguard used by Scorched Guns, but
         * never let it suppress the inventory/player GUI preview. The player
         * model mixin runs at the true TAIL of PlayerModel#setupAnim, so this
         * also overrides any one-frame vanilla use-item arm pose that would
         * otherwise flash when right-click is pressed.
         */
        if (player.isLocalPlayer()
                && minecraft.options.getCameraType().isFirstPerson()
                && minecraft.gui.screen() == null
                && renderState.walkAnimationPos == 0.0F) {
            model.rightArm.xRot = 0.0F;
            model.rightArm.yRot = 0.0F;
            model.rightArm.zRot = 0.0F;
            model.leftArm.xRot = 0.0F;
            model.leftArm.yRot = 0.0F;
            model.leftArm.zRot = 0.0F;
            return;
        }

        if (player.isSwimming() || player.isFallFlying() || player.isVisuallySwimming()) {
            applySwimmingGunPose(player, model);
            return;
        }

        Gun gun = gunItem.getModifiedGun(heldItem);
        gun.getGeneral().getGripType(heldItem).heldAnimation().applyPlayerModelRotation(
                player,
                model.rightArm,
                model.leftArm,
                model.head,
                InteractionHand.MAIN_HAND,
                AimingHandler.get().getAimProgress(
                        player,
                        minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)
                )
        );
    }

    private static void applySwimmingGunPose(Player player, HumanoidModel<?> model) {
        float aimProgress = 0.0F;
        if (player.isLocalPlayer()) {
            aimProgress = AimingHandler.get().getAimProgress(
                    player,
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
            );
        }

        model.rightArm.xRot = (float) Math.toRadians(-160F + (aimProgress * 10F));
        model.rightArm.yRot = (float) Math.toRadians(-15F);
        model.rightArm.zRot = (float) Math.toRadians(10F);

        model.leftArm.xRot = (float) Math.toRadians(-140F);
        model.leftArm.yRot = (float) Math.toRadians(20F);
        model.leftArm.zRot = (float) Math.toRadians(-15F);
    }
}
