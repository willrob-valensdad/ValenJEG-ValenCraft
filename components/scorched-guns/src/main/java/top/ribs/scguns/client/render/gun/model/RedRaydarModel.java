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
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

public class RedRaydarModel implements IOverrideModel {
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.getScope(stack) == null) {
            GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        renderStockAttachments(stack, matrixStack, buffer, light, overlay);
        renderUnderBarrelAttachments(stack, matrixStack, buffer, light, overlay);
    }

    private void renderStockAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_STOCK_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }

    private void renderUnderBarrelAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_VERTICAL_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.RED_RAYDAR_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
}
