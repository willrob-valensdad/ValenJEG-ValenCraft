package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.ScampRocketEntity;

/** Minecraft 26.2 render-state renderer for the Scamp Tank rocket. */
public class ScampRocketRenderer extends EntityRenderer<ScampRocketEntity, ScampRocketRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/scamp_rocket.png");

    private final ScampRocketModel model;

    public ScampRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ScampRocketModel(context.bakeLayer(ModModelLayers.SCAMP_ROCKET_LAYER));
    }

    @Override
    public ScampRocketRenderState createRenderState() {
        return new ScampRocketRenderState();
    }

    @Override
    public void extractRenderState(ScampRocketEntity entity,
                                   ScampRocketRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.lengthSqr() > 0.01D) {
            state.yaw = (float) (Mth.atan2(velocity.x, velocity.z) * (180.0D / Math.PI));
            double horizontalDistance = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            state.pitch = (float) (Mth.atan2(-velocity.y, horizontalDistance) * (180.0D / Math.PI));
        } else {
            state.yaw = 0.0F;
            state.pitch = 0.0F;
        }
    }

    @Override
    public void submit(ScampRocketRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.yaw)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(state.pitch)));

        collector.order(0).submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                state.outlineColor
        );

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
