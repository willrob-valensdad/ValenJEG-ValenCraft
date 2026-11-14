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

/**
 * Since we want to have an animation for the charging handle, we will be overriding the standard model rendering.
 * This also allows us to replace the model for the different stocks.
 */
public class MusketModel implements IOverrideModel {

    private static final int FLASH_DURATION = 10;
    private int flashTimer = 0;
    private float scguns$lastCooldown = 0.0F;

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, GunRenderContext buffer, int light, int overlay) {

        // Renders the static parts of the model.
        boolean scguns$shotEffectContext = entity != null
                && entity.equals(Minecraft.getInstance().player)
                && (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        if (!scguns$shotEffectContext) {
            // The override model is a singleton shared by GUI, player, villagers and
            // other render paths. Never let a timed flash leak from one render into another.
            this.flashTimer = 0;
            this.scguns$lastCooldown = 0.0F;
        }

        GunModelRenderUtil.renderModel(SpecialModels.MUSKET_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_STOCK_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_VERTICAL_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                GunModelRenderUtil.renderModel(SpecialModels.MUSKET_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);

        }
        if (entity != null && entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.5, 0.5);
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            cooldown = (float) ease(cooldown);
            float rotationAngle = -cooldown * 45;
            matrixStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(rotationAngle)));
            matrixStack.translate(0, 0.5, -0.5);
            GunModelRenderUtil.renderModel(SpecialModels.MUSKET_HAMMER.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
            if (scguns$shotEffectContext) {
                // ItemCooldowns starts near 1 immediately after a shot and decays to 0.
                // Trigger on the rising edge from idle, never from a GUI/model-preview render.
                if (this.scguns$lastCooldown < 0.95F && cooldown >= 0.95F) {
                    this.flashTimer = FLASH_DURATION;
                }
                this.scguns$lastCooldown = cooldown;
            }
        }

        if (scguns$shotEffectContext && flashTimer > 0) {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.0, 0);
            GunModelRenderUtil.renderModel(SpecialModels.MUSKET_FLASH.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
            flashTimer--;
        }
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}

