package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import top.ribs.scguns.client.render.entity.state.ScgunsItemRenderState;
import top.ribs.scguns.entity.throwable.ThrowableGrenadeEntity;

public class ThrowableGrenadeRenderer
        extends EntityRenderer<ThrowableGrenadeEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ThrowableGrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(ThrowableGrenadeEntity entity, ScgunsItemRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderItem = true;
        state.stack = entity.getItem().copy();
        this.itemModelResolver.updateForNonLiving(
                state.item, entity.getItem(), ItemDisplayContext.GROUND, entity
        );
    }

    @Override
    public void submit(ScgunsItemRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(camera.orientation));
        state.item.submit(
                poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
