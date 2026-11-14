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
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.entity.state.ScgunsItemRenderState;
import top.ribs.scguns.client.render.gun.GunRenderContext;
import top.ribs.scguns.entity.projectile.MicroJetEntity;

public class MicroJetRenderer extends EntityRenderer<MicroJetEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public MicroJetRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(MicroJetEntity entity, ScgunsItemRenderState state,
                                   float partialTicks) {
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
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(state.xRot - 90.0F)));
            state.item.submit(
                    poseStack, collector, 15728880,
                    OverlayTexture.NO_OVERLAY, state.outlineColor
            );

            poseStack.translate(0.0, -1.0, 0.0);
            new GunRenderContext(collector, ItemDisplayContext.NONE, state.outlineColor)
                    .submitStandaloneModel(
                            SpecialModels.FLAME.getModel(),
                            state.stack,
                            ItemStack.EMPTY,
                            poseStack,
                            15728880,
                            OverlayTexture.NO_OVERLAY
                    );
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
