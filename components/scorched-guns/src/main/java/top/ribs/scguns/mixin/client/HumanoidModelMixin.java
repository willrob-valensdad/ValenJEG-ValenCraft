package top.ribs.scguns.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;
import top.ribs.scguns.client.compat.ThirdPersonPoseCompat;

/**
 * Applies the same held pose to non-PlayerModel humanoid models submitted with
 * the player's AvatarRenderState (primarily armor models). PlayerModel itself
 * is excluded because its dedicated mixin must run later at PlayerModel TAIL.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL")
    )
    private void scguns$setupArmorAnimTail(HumanoidRenderState renderState, CallbackInfo ci) {
        if ((Object) this instanceof PlayerModel) {
            return;
        }

        if (RenderStateEntityBridge.resolve(renderState) instanceof Player player) {
            ThirdPersonPoseCompat.apply((HumanoidModel<?>) (Object) this, renderState, player);
        }
    }
}
