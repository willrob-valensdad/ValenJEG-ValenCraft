package top.ribs.scguns.fabric;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
/** 26.3 PoseStack.mulPose takes a matrix. NeoForge 26.2 passed a quaternion. */
public final class Poses {
    private Poses() {}
    public static Matrix4fc of(Quaternionfc rotation) { return new Matrix4f().rotation(rotation); }
}
