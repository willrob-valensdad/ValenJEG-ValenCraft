package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import top.ribs.scguns.client.render.entity.state.ScgunsItemRenderState;
import top.ribs.scguns.entity.throwable.ThrowableShotballEntity;

public class ThrowableShotballRenderer
        extends EntityRenderer<ThrowableShotballEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ThrowableShotballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(ThrowableShotballEntity entity, ScgunsItemRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderItem = true;
        state.stack = entity.getItem().copy();
        this.itemModelResolver.updateForNonLiving(
                state.item, entity.getItem(), ItemDisplayContext.NONE, entity
        );
    }

    @Override
    public void submit(ScgunsItemRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float spin = state.ageInTicks * 0.5F;
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(spin * 2.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(spin * 1.5F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(spin)));
        poseStack.scale(0.25F, 0.25F, 0.25F);
        state.item.submit(
                poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
