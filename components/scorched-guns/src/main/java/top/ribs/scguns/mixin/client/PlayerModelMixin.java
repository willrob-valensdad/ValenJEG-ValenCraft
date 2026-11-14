package top.ribs.scguns.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.FreshAnimationsCompat;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;
import top.ribs.scguns.client.compat.ThirdPersonPoseCompat;

/**
 * Applies the Scorched Guns player pose at the final PlayerModel animation
 * point. This deliberately targets PlayerModel rather than HumanoidModel: in
 * Minecraft 26.2 PlayerModel performs player-specific arm/use-item work after
 * its HumanoidModel super call, so a superclass-tail injection can be
 * overwritten before the model is submitted.
 */
@Mixin(PlayerModel.class)
public class PlayerModelMixin {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("TAIL")
    )
    private void scguns$setupAnimTail(AvatarRenderState renderState, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = resolvePlayer(renderState, minecraft);
        if (player == null) {
            return;
        }

        PlayerModel model = (PlayerModel) (Object) this;
        ThirdPersonPoseCompat.apply(model, renderState, player);
        FreshAnimationsCompat.captureGunPose(model, player);
    }

    private static Player resolvePlayer(AvatarRenderState renderState, Minecraft minecraft) {
        Entity captured = RenderStateEntityBridge.resolve(renderState);
        if (captured instanceof Player player) {
            return player;
        }

        // AvatarRenderState still exposes the entity id, so keep this only as a
        // compatibility fallback for unusual preview/render paths.
        if (minecraft.level == null) {
            return null;
        }
        Entity entity = minecraft.level.getEntity(renderState.id);
        return entity instanceof Player player ? player : null;
    }
}
