package org.alex_melan.spacereloaded.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.alex_melan.spacereloaded.client.SpinSky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Вращение небесной сферы (Солнце, Луна, звёзды) вокруг оси кольца, в котором стоит игрок (007). */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"))
    private void spacereloaded$spinPush(RenderPass renderPass, PoseStack pose, float a, float b, float c, MoonPhase phase, float d, float e,
                                        CallbackInfo ci) {
        pose.pushPose();
        int axis = SpinSky.axis();
        if (axis != 0) {
            float angle = SpinSky.angle();
            pose.rotate(axis == 1 ? Axis.XP : Axis.ZP, angle);
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("RETURN"))
    private void spacereloaded$spinPop(RenderPass renderPass, PoseStack pose, float a, float b, float c, MoonPhase phase, float d, float e,
                                       CallbackInfo ci) {
        pose.popPose();
    }
}
