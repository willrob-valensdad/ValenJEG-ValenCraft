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
import top.ribs.scguns.entity.projectile.ProjectileEntity;

public class ProjectileRenderer extends EntityRenderer<ProjectileEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(ProjectileEntity entity, ScgunsItemRenderState state, float partialTicks) {
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
            state.item.submit(
                    poseStack, collector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor
            );
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
