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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

import java.util.HashMap;
import java.util.Map;

public class NewbornCystModel implements IOverrideModel {

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        renderStockAttachments(matrixStack, buffer, stack, light, overlay);
        renderBarrelAttachments(matrixStack, buffer, stack, light, overlay);
        renderUnderBarrelAttachments(matrixStack, buffer, stack, light, overlay);

        if (Gun.getScope(stack) == null) {
            GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }


        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.30, 0.33);
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            cooldown = (float) ease(cooldown);
            float rotationAngle = -cooldown * 30;
            matrixStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(rotationAngle)));
            matrixStack.translate(0, 0.30, -0.33);
            GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_HAMMER.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }

    private void renderStockAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }

    private void renderBarrelAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        boolean hasExtendedBarrel = false;

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.EXTENDED_BARREL.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_EXT_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
                hasExtendedBarrel = true;
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }

        // Render the standard barrel if no extended barrel is attached
        if (!hasExtendedBarrel) {
            GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STAN_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderUnderBarrelAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_TACT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.NEWBORN_CYST_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}
