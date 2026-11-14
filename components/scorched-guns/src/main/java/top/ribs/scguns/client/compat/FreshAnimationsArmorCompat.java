package top.ribs.scguns.client.compat;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bridges the final EMF/Fresh Animations biped pose into Scorched Guns'
 * GeckoLib armor renderers.
 *
 * <p>GeoArmorRenderer normally rebuilds a vanilla HumanoidModel pose from the
 * render state immediately before converting it into GeckoLib armor bones.
 * That is correct for vanilla animation, but it discards model-space movement
 * added later by EMF resource-pack animations. This bridge snapshots the final
 * EMF pose and reapplies only those transforms to Scorched Guns armor bones.</p>
 *
 * <p>No gameplay state is stored here. Everything is client-only, keyed by UUID,
 * and cleared when the client leaves the world.</p>
 */
public final class FreshAnimationsArmorCompat {
    private static final Map<UUID, ArmorPose> ARMOR_POSES = new HashMap<>();
    private static final Map<UUID, PartSnapshot> ROOT_POSES = new HashMap<>();
    private static int cleanupCounter;

    private FreshAnimationsArmorCompat() {
    }

    /** Captures the final EMF model pose after resource-pack animation and gun pose restoration. */
    public static void captureFromEmf(UUID uuid,
                                      ModelPart root,
                                      ModelPart head,
                                      ModelPart body,
                                      ModelPart leftArm,
                                      ModelPart rightArm,
                                      ModelPart leftLeg,
                                      ModelPart rightLeg) {
        if (uuid == null || !OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        if (root != null) {
            ROOT_POSES.put(uuid, new PartSnapshot(root));
        }

        ARMOR_POSES.put(uuid, new ArmorPose(
                snapshot(head),
                snapshot(body),
                snapshot(leftArm),
                snapshot(rightArm),
                snapshot(leftLeg),
                snapshot(rightLeg)
        ));
        cleanupIfNeeded();
    }

    /**
     * Captures EMF's already-copied armor biped when that hook is available.
     * This is the most direct source for detached armor-model pivots and also
     * includes any Scorched Guns gun-pose corrections applied by the same hook.
     */
    public static void captureFromBiped(UUID uuid, HumanoidModel<?> model) {
        if (uuid == null || model == null || !OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        // EMF's own detached-biped copy includes model.root(). Keep that exact
        // root alongside the copied limbs instead of leaving ROOT_POSES sourced
        // from whichever EMF model happened to animate most recently.
        ModelPart root = model.root();
        if (root != null) {
            ROOT_POSES.put(uuid, new PartSnapshot(root));
        }

        ARMOR_POSES.put(uuid, new ArmorPose(
                new PartSnapshot(model.head),
                new PartSnapshot(model.body),
                new PartSnapshot(model.leftArm),
                new PartSnapshot(model.rightArm),
                new PartSnapshot(model.leftLeg),
                new PartSnapshot(model.rightLeg)
        ));
        cleanupIfNeeded();
    }

    /** Makes the entire GeckoLib armor model inherit Fresh Animations' animated root. */
    public static void applyRoot(UUID uuid, PoseStack poseStack) {
        if (uuid == null || poseStack == null || !OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        PartSnapshot root = ROOT_POSES.get(uuid);
        if (root == null) {
            return;
        }

        poseStack.translate(root.x / 16.0F, root.y / 16.0F, root.z / 16.0F);
        if (root.xRot != 0.0F || root.yRot != 0.0F || root.zRot != 0.0F) {
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(new Quaternionf().rotationZYX(root.zRot, root.yRot, root.xRot)));
        }
        if (root.xScale != 1.0F || root.yScale != 1.0F || root.zScale != 1.0F) {
            poseStack.scale(root.xScale, root.yScale, root.zScale);
        }
    }

    /** Replaces GeckoLib's vanilla-derived armor-bone pose with the final EMF biped pose. */
    public static void applyArmorPose(UUID uuid,
                                      HumanoidRenderState renderState,
                                      BoneSnapshots snapshots,
                                      GeoArmorRenderer<?, HumanoidRenderState> renderer) {
        if (uuid == null || renderState == null || snapshots == null || renderer == null
                || !OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        ArmorPose pose = ARMOR_POSES.get(uuid);
        if (pose == null) {
            return;
        }

        for (GeoArmorRenderer.ArmorSegment segment : GeoArmorRenderer.ArmorSegment.values()) {
            PartSnapshot part = pose.forSegment(segment);
            if (part == null) {
                continue;
            }

            snapshots.get(renderer.getBoneNameForSegment(renderState, segment))
                    .ifPresent(snapshot -> part.applyToArmorBone(segment, snapshot));
        }
    }

    public static void clearAll() {
        ARMOR_POSES.clear();
        ROOT_POSES.clear();
        cleanupCounter = 0;
    }

    private static PartSnapshot snapshot(ModelPart part) {
        return part == null ? null : new PartSnapshot(part);
    }

    private static void cleanupIfNeeded() {
        if (++cleanupCounter % 200 != 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Set<UUID> activePlayers = minecraft.level.players().stream()
                .map(player -> player.getUUID())
                .collect(Collectors.toSet());
        ARMOR_POSES.keySet().retainAll(activePlayers);
        ROOT_POSES.keySet().retainAll(activePlayers);
    }

    private record ArmorPose(PartSnapshot head,
                             PartSnapshot body,
                             PartSnapshot leftArm,
                             PartSnapshot rightArm,
                             PartSnapshot leftLeg,
                             PartSnapshot rightLeg) {
        private PartSnapshot forSegment(GeoArmorRenderer.ArmorSegment segment) {
            return switch (segment) {
                case HEAD -> this.head;
                case CHEST -> this.body;
                case LEFT_ARM -> this.leftArm;
                case RIGHT_ARM -> this.rightArm;
                case LEFT_LEG, LEFT_FOOT -> this.leftLeg;
                case RIGHT_LEG, RIGHT_FOOT -> this.rightLeg;
            };
        }
    }

    /** Immutable copy of one vanilla/EMF ModelPart transform. */
    private static final class PartSnapshot {
        private final float xRot;
        private final float yRot;
        private final float zRot;
        private final float x;
        private final float y;
        private final float z;
        private final float xScale;
        private final float yScale;
        private final float zScale;

        private PartSnapshot(ModelPart part) {
            this.xRot = part.xRot;
            this.yRot = part.yRot;
            this.zRot = part.zRot;
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.xScale = part.xScale;
            this.yScale = part.yScale;
            this.zScale = part.zScale;
        }

        private void applyToArmorBone(GeoArmorRenderer.ArmorSegment segment, BoneSnapshot snapshot) {
            // Mirror GeoArmorRenderer's own ModelPart -> armor-bone conversion so
            // only the pose source changes; armor geometry/segment semantics stay vanilla.
            Vector3f matched = segment.modelPartMatcher.apply(new Vector3f(this.x, this.y, this.z));
            snapshot.setRotX(-this.xRot)
                    .setRotY(-this.yRot)
                    .setRotZ(this.zRot)
                    .setTranslation(matched.x, matched.y, matched.z)
                    .setScale(this.xScale, this.yScale, this.zScale);
        }
    }
}
