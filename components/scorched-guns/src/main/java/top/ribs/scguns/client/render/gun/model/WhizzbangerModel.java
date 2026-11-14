package top.ribs.scguns.client.render.gun.model;

import top.ribs.scguns.client.render.gun.GunModelRenderUtil;

import top.ribs.scguns.client.render.gun.GunRenderContext;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;

public class WhizzbangerModel implements IOverrideModel {
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        GunModelRenderUtil.renderModel(SpecialModels.WHIZZBANGER_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
    }
}
