package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/** 26.2 SubmitNodeCollector beam renderer preserving the original Scorched Guns geometry. */
@OnlyIn(Dist.CLIENT)
public final class BeamRenderer {
    private static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");

    private BeamRenderer() {}

    public static void renderBeam(PoseStack poseStack, SubmitNodeCollector collector, float partialTicks,
                                  Vec3 start, Vec3 end, Vec3 lastStart, Vec3 lastEnd,
                                  float[] color, float fadeProgress) {
        Vec3 interpolatedStart = lerpVec3(partialTicks, lastStart, start);
        Vec3 interpolatedEnd = lerpVec3(partialTicks, lastEnd, end);

        Vec3 beamVec = interpolatedEnd.subtract(interpolatedStart);
        float length = (float) beamVec.length();
        if (length <= 1.0E-5F) return;
        Vec3 beamNormal = beamVec.normalize();

        poseStack.pushPose();
        poseStack.translate(interpolatedStart.x, interpolatedStart.y, interpolatedStart.z);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 axis = up.cross(beamNormal);
        double dot = Mth.clamp(up.dot(beamNormal), -1.0, 1.0);
        float angle = (float) Math.acos(dot);
        if (axis.lengthSqr() > 1.0E-8) {
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.of(new Vector3f((float) axis.x, (float) axis.y, (float) axis.z))
                    .rotationDegrees((float) Math.toDegrees(angle))));
        } else if (dot < 0.0) {
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(180.0F)));
        }

        long gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        renderBeamSegment(poseStack, collector, partialTicks, gameTime, 0, length, color);
        poseStack.popPose();
    }

    public static Vec3 lerpVec3(float partialTicks, Vec3 lastPos, Vec3 currentPos) {
        return new Vec3(
                Mth.lerp(partialTicks, lastPos.x, currentPos.x),
                Mth.lerp(partialTicks, lastPos.y, currentPos.y),
                Mth.lerp(partialTicks, lastPos.z, currentPos.z));
    }

    public static void renderBeamSegment(PoseStack poseStack, SubmitNodeCollector collector, float partialTick,
                                         long gameTime, float yOffset, float height, float[] colors) {
        renderBeamPart(poseStack, collector, BEAM_LOCATION, partialTick, 1.0F, gameTime,
                0, Math.round(height), colors, 0.015F, 0.020F);
    }

    public static void renderBeamPart(PoseStack poseStack, SubmitNodeCollector collector, Identifier beamLocation,
                                      float partialTick, float textureScale, long gameTime, int yOffset, int height,
                                      float[] colors, float beamRadius, float glowRadius) {
        int maxY = yOffset + height;
        poseStack.pushPose();
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];

        poseStack.pushPose();
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(f * 2.25F - 45.0F)));
        float solidMinV = -1.0F + f2;
        float solidMaxV = (float) height * textureScale * (0.5F / beamRadius) + solidMinV;
        submitPart(poseStack, collector, RenderTypes.beaconBeam(beamLocation, false),
                r, g, b, 1.0F, yOffset, maxY,
                0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius,
                0.0F, 1.0F, solidMaxV, solidMinV);
        poseStack.popPose();

        float glowMinV = -1.0F + f2;
        float glowMaxV = (float) height * textureScale + glowMinV;
        submitPart(poseStack, collector, RenderTypes.beaconBeam(beamLocation, true),
                r, g, b, 0.125F, yOffset, maxY,
                -glowRadius, -glowRadius, glowRadius, -glowRadius, -glowRadius, glowRadius, glowRadius, glowRadius,
                0.0F, 1.0F, glowMaxV, glowMinV);
        poseStack.popPose();
    }

    private static void submitPart(PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType renderType,
                                   float red, float green, float blue, float alpha, int minY, int maxY,
                                   float x0, float z0, float x1, float z1, float x2, float z2, float x3, float z3,
                                   float minU, float maxU, float minV, float maxV) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) ->
                renderPart(pose, consumer, red, green, blue, alpha, minY, maxY,
                        x0, z0, x1, z1, x2, z2, x3, z3, minU, maxU, minV, maxV));
    }

    private static void renderPart(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha,
                                   int minY, int maxY, float x0, float z0, float x1, float z1, float x2, float z2, float x3, float z3,
                                   float minU, float maxU, float minV, float maxV) {
        renderQuad(pose, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0, minU, maxU, minV, maxV);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha,
                                   int minY, int maxY, float minX, float minZ, float maxX, float maxZ,
                                   float minU, float maxU, float minV, float maxV) {
        addVertex(pose, consumer, red, green, blue, alpha, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, red, green, blue, alpha, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, red, green, blue, alpha, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, red, green, blue, alpha, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha,
                                  int y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, (float) y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
