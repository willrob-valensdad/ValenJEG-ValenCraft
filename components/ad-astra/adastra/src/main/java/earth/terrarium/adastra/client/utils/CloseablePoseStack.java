package earth.terrarium.adastra.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

/**
 * Push/pop scope used by the block renderers. Resourceful Lib 6 no longer
 * ships this helper. Transforms apply to the same pose stack the caller passes in.
 */
public final class CloseablePoseStack implements AutoCloseable {
    private final PoseStack pose;

    public CloseablePoseStack(PoseStack pose) {
        this.pose = pose;
        pose.pushPose();
    }

    public void translate(double x, double y, double z) {
        pose.translate(x, y, z);
    }

    public void translate(float x, float y, float z) {
        pose.translate(x, y, z);
    }

    public void mulPose(Quaternionf quaternion) {
        pose.mulPose(quaternion);
    }

    public void scale(float x, float y, float z) {
        pose.scale(x, y, z);
    }

    public void pushPose() {
        pose.pushPose();
    }

    public void popPose() {
        pose.popPose();
    }

    public PoseStack.Pose last() {
        return pose.last();
    }

    @Override
    public void close() {
        pose.popPose();
    }
}
