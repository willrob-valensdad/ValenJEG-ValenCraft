package top.ribs.scguns.client.render.gun.model;

import top.ribs.scguns.client.render.gun.GunModelRenderUtil;

import top.ribs.scguns.client.render.gun.GunRenderContext;

import com.mojang.blaze3d.vertex.PoseStack;
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
import com.mojang.math.Axis;

public class CogloaderModel implements IOverrideModel {
    private float magazineRotation = 0.0f;

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.getScope(stack) == null) {
            GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        renderStockAttachments(matrixStack, buffer, stack, light, overlay);
        renderBarrelAndAttachments(matrixStack, buffer, stack, light, overlay);
        renderUnderBarrelAttachments(matrixStack, buffer, stack, light, overlay);
        if (entity.equals(Minecraft.getInstance().player)) {
            renderBoltAndMagazine(matrixStack, buffer, stack, light, overlay);
        }
    }

    private void renderStockAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
    private void renderBarrelAndAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        boolean hasExtendedBarrel = false;

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.EXTENDED_BARREL.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_EXT_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
                hasExtendedBarrel = true;
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }

        // Render the standard barrel if no extended barrel is attached
        if (!hasExtendedBarrel) {
            GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_STAN_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }
    private void renderUnderBarrelAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_GRIP_VERTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_GRIP_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
    private void renderBoltAndMagazine(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        assert Minecraft.getInstance().player != null;
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        matrixStack.pushPose();
        matrixStack.translate(0, -5.8 * 0.0625, 0);
        if (cooldown > 0) {
            matrixStack.translate(0, 0, cooldown / 6);
            magazineRotation += (float) (5.0f * (1.0 - cooldown));
            magazineRotation = magazineRotation % 360;
        }
        matrixStack.translate(0, 5.8 * 0.0625, 0);
        GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_BOLT.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
        renderMagazineRotation(matrixStack, buffer, stack, light, overlay);
    }
    private void renderMagazineRotation(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 0);
        matrixStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(magazineRotation)));
        matrixStack.translate(-0, -0, -0);
        GunModelRenderUtil.renderModel(SpecialModels.COGLOADER_MAGAZINE.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
    }
}
