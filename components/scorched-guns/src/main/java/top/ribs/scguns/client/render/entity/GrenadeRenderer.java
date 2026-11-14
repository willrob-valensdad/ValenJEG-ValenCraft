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
import top.ribs.scguns.entity.throwable.GrenadeEntity;

public class GrenadeRenderer extends EntityRenderer<GrenadeEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(GrenadeEntity entity, ScgunsItemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderItem = !entity.getProjectile().isVisible() && entity.tickCount > 1;
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.stack = entity.getItem().copy();

        if (state.renderItem) {
            this.itemModelResolver.updateForNonLiving(
                    state.item, entity.getItem(), ItemDisplayContext.NONE, entity
            );
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(ScgunsItemRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.renderItem) {
            poseStack.pushPose();
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(180.0F)));
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.yRot)));
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(state.xRot)));

            float rotation = state.ageInTicks;
            poseStack.translate(0.0, 0.15, 0.0);
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XN.rotationDegrees(rotation * 20.0F)));
            poseStack.translate(0.0, -0.15, 0.0);
            poseStack.translate(0.0, 0.5, 0.0);

            state.item.submit(
                    poseStack, collector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor
            );
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
