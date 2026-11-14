package top.ribs.scguns.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import top.ribs.scguns.item.GunItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Small state bridge between Scorched Guns' player pose and EMF.
 *
 * <p>The important ordering is:</p>
 * <ol>
 *     <li>vanilla + Scorched Guns finish posing the PlayerModel;</li>
 *     <li>we snapshot the gun arms, the aim-adjusted head, and the body's base position;</li>
 *     <li>EMF/Fresh Animations is allowed to animate the complete player;</li>
 *     <li>the EMF hook restores the captured gun pose while letting it follow the
 *         torso translation produced by the resource pack.</li>
 * </ol>
 *
 * <p>The animated EMF root is stored separately because held items are rendered
 * from the entity/layer matrix rather than as children of that root. Without
 * replaying the root transform on the gun, Fresh Animations can move the whole
 * player while the weapon appears to stay behind.</p>
 */
public final class FreshAnimationsCompat {
    private static final Map<UUID, GunPose> GUN_POSES = new HashMap<>();
    private static final Map<UUID, PartSnapshot> ROOT_POSES = new HashMap<>();
    private static int cleanupCounter;

    private FreshAnimationsCompat() {
    }

    public static void captureGunPose(PlayerModel model, Player player) {
        if (!OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        UUID uuid = player.getUUID();
        if (!(player.getMainHandItem().getItem() instanceof GunItem)) {
            clear(uuid);
            return;
        }

        ROOT_POSES.remove(uuid);

        GUN_POSES.put(uuid, new GunPose(
                new PartSnapshot(model.leftArm),
                new PartSnapshot(model.rightArm),
                new PartSnapshot(model.head),
                model.body.x,
                model.body.y,
                model.body.z
        ));

        cleanupIfNeeded();
    }

    public static boolean hasGunPose(UUID uuid) {
        return uuid != null && GUN_POSES.containsKey(uuid);
    }

    public static void storeAnimatedRoot(UUID uuid, ModelPart root) {
        if (uuid != null && root != null && hasGunPose(uuid)) {
            ROOT_POSES.put(uuid, new PartSnapshot(root));
        }
    }

    public static void clearRoot(UUID uuid) {
        if (uuid != null) {
            ROOT_POSES.remove(uuid);
            }
    }

    public static void restoreAnimatedPose(UUID uuid,
                                           ModelPart body,
                                           ModelPart head,
                                           ModelPart hat,
                                           ModelPart leftArm,
                                           ModelPart rightArm,
                                           ModelPart leftSleeve,
                                           ModelPart rightSleeve) {
        GunPose pose = GUN_POSES.get(uuid);
        if (pose == null) {
            return;
        }

        float bodyDx = 0.0F;
        float bodyDy = 0.0F;
        float bodyDz = 0.0F;
        if (body != null) {
            bodyDx = body.x - pose.bodyX;
            bodyDy = body.y - pose.bodyY;
            bodyDz = body.z - pose.bodyZ;
        }

        if (head != null) {
            pose.head.applyRotation(head);
            syncOuterLayer(head, hat, "hat");
        }
        if (leftArm != null) {
            pose.leftArm.applyBodyFollowing(leftArm, bodyDx, bodyDy, bodyDz);
            syncOuterLayer(leftArm, leftSleeve, "left_sleeve");
        }
        if (rightArm != null) {
            pose.rightArm.applyBodyFollowing(rightArm, bodyDx, bodyDy, bodyDz);
            syncOuterLayer(rightArm, rightSleeve, "right_sleeve");
        }
    }

    /**
     * EMF's biped-pose copy already gives armor the animated body/part pivots.
     * Reapply the gun-driven rotations here so armor keeps those EMF positions
     * instead of being forced onto the player's raw pivots.
     */
    public static void restoreArmorPose(UUID uuid, HumanoidModel<?> model) {
        GunPose pose = GUN_POSES.get(uuid);
        if (pose == null || model == null) {
            return;
        }

        pose.head.applyRotation(model.head);
        copyRotation(model.head, model.hat);
        pose.leftArm.applyRotation(model.leftArm);
        pose.rightArm.applyRotation(model.rightArm);
    }

    /**
     * Makes a hand-rendered gun inherit the animated EMF root exactly once,
     * before Scorched Guns translates through the already body-following arm.
     *
     * <p>Do not replay the body translation here. HF92/HF93 already applies
     * that delta to the arm pivot itself, and {@code translateToHand} consumes
     * that final arm transform. Re-applying the same delta to the held-item
     * PoseStack doubles Fresh Animations' locomotion sway, which is especially
     * visible while running.</p>
     */
    public static void applyAnimatedRootToHeldGun(Player player, PoseStack poseStack) {
        if (player == null || poseStack == null || !OptionalClientCompat.isEntityModelFeaturesLoaded()) {
            return;
        }

        PartSnapshot root = ROOT_POSES.get(player.getUUID());
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

    public static void clear(UUID uuid) {
        if (uuid == null) {
            return;
        }
        GUN_POSES.remove(uuid);
        ROOT_POSES.remove(uuid);
    }

    private static void syncOuterLayer(ModelPart main, ModelPart overlay, String nestedName) {
        if (main == null || overlay == null) {
            return;
        }
        if (!main.hasChild(nestedName)) {
            PartSnapshot.copy(main, overlay);
        }
    }

    private static void copyRotation(ModelPart from, ModelPart to) {
        if (from == null || to == null) {
            return;
        }
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
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
                .map(Player::getUUID)
                .collect(Collectors.toSet());
        GUN_POSES.keySet().retainAll(activePlayers);
        ROOT_POSES.keySet().retainAll(activePlayers);
    }

    private record GunPose(PartSnapshot leftArm, PartSnapshot rightArm, PartSnapshot head,
                           float bodyX, float bodyY, float bodyZ) {
    }

    /** Immutable ModelPart transform used only by this compatibility bridge. */
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
        private final boolean visible;
        private final boolean skipDraw;

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
            this.visible = part.visible;
            this.skipDraw = part.skipDraw;
        }

        private void applyBodyFollowing(ModelPart part, float dx, float dy, float dz) {
            applyRotation(part);
            part.x = this.x + dx;
            part.y = this.y + dy;
            part.z = this.z + dz;
            part.xScale = this.xScale;
            part.yScale = this.yScale;
            part.zScale = this.zScale;
            part.visible = this.visible;
            part.skipDraw = this.skipDraw;
        }

        private void applyRotation(ModelPart part) {
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
        }

        private static void copy(ModelPart from, ModelPart to) {
            to.xRot = from.xRot;
            to.yRot = from.yRot;
            to.zRot = from.zRot;
            to.x = from.x;
            to.y = from.y;
            to.z = from.z;
            to.xScale = from.xScale;
            to.yScale = from.yScale;
            to.zScale = from.zScale;
            to.visible = from.visible;
            to.skipDraw = from.skipDraw;
        }
    }

    /** Clears UUID-only pose snapshots when leaving the current client world. */
    public static void clearAll() {
        GUN_POSES.clear();
        ROOT_POSES.clear();
        cleanupCounter = 0;
    }
}
