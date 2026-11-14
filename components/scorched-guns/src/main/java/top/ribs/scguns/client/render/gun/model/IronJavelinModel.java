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

/**
 * Since we want to have an animation for the charging handle, we will be overriding the standard model rendering.
 * This also allows us to replace the model for the different stocks.
 */
public class IronJavelinModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {

        // Renders the static parts of the model.
        GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        // Renders the iron sights if no scope is attached.
        if (Gun.getScope(stack) == null) {
            GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        // Render stock attachments
        renderStockAttachments(stack, matrixStack, buffer, light, overlay);

        // Render under-barrel attachments
        renderUnderBarrelAttachments(stack, matrixStack, buffer, light, overlay);

        // Render magazine attachments
        renderMagazineAttachments(stack, matrixStack, buffer, light, overlay);

        // Render barrel and attachments with the new system
        renderBarrelAndAttachments(stack, matrixStack, buffer, light, overlay);

        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -5.8 * 0.0625, 0);
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            cooldown = (float) ease(cooldown);
            matrixStack.translate(0, 0, cooldown / 10);
            matrixStack.translate(0, 5.8 * 0.0625, 0);
            GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_BOLT.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }

    private void renderStockAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderUnderBarrelAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_TACT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderMagazineAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)) {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_EXT_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.SPEED_MAG.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_SPEED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.PLUS_P_MAG.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_EXT_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            GunModelRenderUtil.renderModel(SpecialModels.IRON_JAVELIN_STAN_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderBarrelAndAttachments(ItemStack stack, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {
        boolean hasExtendedBarrel = false;

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.EXTENDED_BARREL.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_EXT_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
                hasExtendedBarrel = true;
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get())
                GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        // Render the standard barrel if no extended barrel is attached
        if (!hasExtendedBarrel) {
            GunModelRenderUtil.renderModel(SpecialModels.IRON_SPEAR_STAN_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}
