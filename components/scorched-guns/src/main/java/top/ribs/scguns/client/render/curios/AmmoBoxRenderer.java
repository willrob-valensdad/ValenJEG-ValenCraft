package top.ribs.scguns.client.render.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Curios 16 / Minecraft 26.2 submit-based ammo box renderer. */
public class AmmoBoxRenderer implements ICurioRenderer {

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int light,
            S renderState,
            RenderLayerParent<S, M> renderLayerParent,
            EntityRendererProvider.Context context,
            float yRot,
            float xRot) {
        poseStack.pushPose();

        LivingEntity entity = slotContext.entity();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            ICurioRenderer.translateIfSneaking(poseStack, entity);
            humanoidModel.body.translateAndRotate(poseStack);
        }

        double xOffset = slotContext.index() == 0 ? -0.18D : 0.18D;
        poseStack.translate(xOffset, 0.55D, 0.17D);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(90.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(90.0F)));
        poseStack.scale(0.8F, 0.8F, 0.8F);

        ItemStackRenderState itemState = new ItemStackRenderState();
        context.getItemModelResolver().updateForLiving(itemState, stack, ItemDisplayContext.GROUND, entity);
        itemState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
