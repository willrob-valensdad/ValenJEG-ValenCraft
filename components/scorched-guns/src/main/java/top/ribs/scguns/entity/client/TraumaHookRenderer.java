package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.render.entity.state.TraumaHookRenderState;
import top.ribs.scguns.entity.projectile.TraumaHookEntity;

/**
 * 26.2 submit-pipeline renderer for the Trauma Unit hook and cable.
 */
public class TraumaHookRenderer extends EntityRenderer<TraumaHookEntity, TraumaHookRenderState> {
    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final int LINE_SEGMENTS = 16;

    public TraumaHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected boolean affectedByCulling(TraumaHookEntity entity) {
        return false;
    }

    @Override
    public TraumaHookRenderState createRenderState() {
        return new TraumaHookRenderState();
    }

    @Override
    public void extractRenderState(TraumaHookEntity hook,
                                   TraumaHookRenderState state,
                                   float partialTicks) {
        super.extractRenderState(hook, state, partialTicks);

        Entity owner = hook.getOwner();
        state.hasLine = owner != null;
        state.lineDelta = Vec3.ZERO;

        if (owner != null) {
            double ownerX = Mth.lerp(partialTicks, owner.xo, owner.getX());
            double ownerY = Mth.lerp(partialTicks, owner.yo, owner.getY())
                    + owner.getEyeHeight() * 0.8D;
            double ownerZ = Mth.lerp(partialTicks, owner.zo, owner.getZ());

            double hookX = Mth.lerp(partialTicks, hook.xo, hook.getX());
            double hookY = Mth.lerp(partialTicks, hook.yo, hook.getY()) + 0.25D;
            double hookZ = Mth.lerp(partialTicks, hook.zo, hook.getZ());

            state.lineDelta = new Vec3(ownerX - hookX, ownerY - hookY, ownerZ - hookZ);
        }
    }

    @Override
    public void submit(TraumaHookRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (state.hasLine) {
            final float deltaX = (float) state.lineDelta.x;
            final float deltaY = (float) state.lineDelta.y;
            final float deltaZ = (float) state.lineDelta.z;

            collector.order(0).submitCustomGeometry(
                    poseStack,
                    RenderTypes.lines(),
                    (pose, consumer) -> {
                        for (int i = 0; i < LINE_SEGMENTS; i++) {
                            renderLineSegment(
                                    deltaX,
                                    deltaY,
                                    deltaZ,
                                    consumer,
                                    pose,
                                    fraction(i, LINE_SEGMENTS),
                                    fraction(i + 1, LINE_SEGMENTS)
                            );
                        }
                    }
            );
        }

        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(camera.orientation));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(180.0F)));

        collector.order(1).submitCustomGeometry(
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                (pose, consumer) -> {
                    vertex(consumer, pose, state.lightCoords, 0.0F, 0, 0, 1);
                    vertex(consumer, pose, state.lightCoords, 1.0F, 0, 1, 1);
                    vertex(consumer, pose, state.lightCoords, 1.0F, 1, 1, 0);
                    vertex(consumer, pose, state.lightCoords, 0.0F, 1, 0, 0);
                }
        );

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static float fraction(int numerator, int denominator) {
        return (float) numerator / (float) denominator;
    }

    private static void renderLineSegment(float deltaX,
                                          float deltaY,
                                          float deltaZ,
                                          VertexConsumer consumer,
                                          PoseStack.Pose pose,
                                          float start,
                                          float end) {
        float x0 = deltaX * start;
        float y0 = curveY(deltaY, start);
        float z0 = deltaZ * start;

        float x1 = deltaX * end;
        float y1 = curveY(deltaY, end);
        float z1 = deltaZ * end;

        float normalX = x1 - x0;
        float normalY = y1 - y0;
        float normalZ = z1 - z0;
        float length = Mth.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length > 1.0E-5F) {
            normalX /= length;
            normalY /= length;
            normalZ /= length;
        }

        lineVertex(consumer, pose, x0, y0, z0, normalX, normalY, normalZ);
        lineVertex(consumer, pose, x1, y1, z1, normalX, normalY, normalZ);
    }

    private static float curveY(float deltaY, float progress) {
        return deltaY * (progress * progress + progress) * 0.5F + 0.25F;
    }

    private static void lineVertex(VertexConsumer consumer,
                                   PoseStack.Pose pose,
                                   float x,
                                   float y,
                                   float z,
                                   float normalX,
                                   float normalY,
                                   float normalZ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(32, 32, 32, 255)
                .setNormal(pose, normalX, normalY, normalZ);
    }

    private static void vertex(VertexConsumer consumer,
                               PoseStack.Pose pose,
                               int light,
                               float x,
                               int y,
                               int u,
                               int v) {
        consumer.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
