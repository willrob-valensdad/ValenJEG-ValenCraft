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
public class BirdfeederModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {

        GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        // Render the barrel and its attachments.
        renderBarrelAttachments(matrixStack, buffer, stack, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_WEIGHTED_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.BUMP_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_WEIGHTED_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_LIGHT_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_WOODEN_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        else {
            GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_STAN_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_VERTICAL_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        float magazinePosition = calculateMagazinePosition(stack);
        float translationMultiplier = 0.25f;
        matrixStack.pushPose();
        matrixStack.translate(clampMagazinePosition(magazinePosition * translationMultiplier), 0, 0);
        GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_MAGAZINE.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
    }

    private void renderBarrelAttachments(PoseStack matrixStack, GunRenderContext buffer, ItemStack stack, int light, int overlay) {
        boolean hasExtendedBarrel = false;

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.EXTENDED_BARREL.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_EXT_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
                hasExtendedBarrel = true;
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get()) {
                GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
        if (!hasExtendedBarrel) {
            GunModelRenderUtil.renderModel(SpecialModels.BIRDFEEDER_STAN_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }

    private float calculateMagazinePosition(ItemStack stack) {
        int maxAmmo = Gun.getMaxAmmo(stack);
        int currentAmmo = Gun.getAmmoCount(stack);
        return Math.min((maxAmmo - currentAmmo) / (float) maxAmmo, 1.0f);
    }

    private float clampMagazinePosition(float position) {
        return Math.max(0, Math.min(position, 1.0f));
    }
}
