package top.ribs.scguns.client.render.gun.model;

import top.ribs.scguns.client.render.gun.GunModelRenderUtil;

import top.ribs.scguns.client.render.gun.GunRenderContext;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

public class GattalerModel implements IOverrideModel {
    private float barrelRotation = 0.0f;

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        GunModelRenderUtil.renderModel(SpecialModels.GATTALER_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        if (entity.equals(Minecraft.getInstance().player)) {
            renderBarrels(matrixStack, buffer, stack, light, overlay);
        }
    }

    private void renderBarrels(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        assert Minecraft.getInstance().player != null;
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        matrixStack.pushPose();
        matrixStack.translate(0, -0.0, 0);
        if (cooldown > 0) {
            barrelRotation += (float) (5.0f * (1.0 - cooldown));
            barrelRotation = barrelRotation % 360;
        }
        matrixStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(barrelRotation)));
        matrixStack.translate(0, 0.0, 0);
        GunModelRenderUtil.renderModel(SpecialModels.GATTALER_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
    }
}
