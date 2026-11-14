package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.projectile.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import top.ribs.scguns.entity.projectile.ShulkshotProjectileEntity;

/**
 * Minecraft 26.2 render-state implementation for the Scorched Guns shulkshot.
 *
 * <p>The live entity is only read during {@link #extractRenderState}; model
 * submission is deferred to the feature renderer through {@link SubmitNodeCollector}.</p>
 */
public class ShulkshotRenderer extends EntityRenderer<ShulkshotProjectileEntity, ShulkerBulletRenderState> {
    private static final Identifier TEXTURE_LOCATION =
            Identifier.withDefaultNamespace("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE_LOCATION);

    private final ShulkerBulletModel model;

    public ShulkshotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(ShulkshotProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ShulkerBulletRenderState createRenderState() {
        return new ShulkerBulletRenderState();
    }

    @Override
    public void extractRenderState(ShulkshotProjectileEntity entity,
                                   ShulkerBulletRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(ShulkerBulletRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.15F, 0.0F);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(Mth.sin(state.ageInTicks * 0.1F) * 180.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(Mth.cos(state.ageInTicks * 0.1F) * 180.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(Mth.sin(state.ageInTicks * 0.15F) * 360.0F)));
        poseStack.scale(-0.5F, -0.5F, 0.5F);

        collector.order(0).submitModel(
                this.model,
                state,
                poseStack,
                this.model.renderType(TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F),
                null,
                state.outlineColor
        );

        poseStack.scale(1.5F, 1.5F, 1.5F);
        collector.order(1).submitModel(
                this.model,
                state,
                poseStack,
                RENDER_TYPE,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(0.15F, 1.0F, 1.0F, 1.0F),
                null,
                state.outlineColor
        );

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
