package top.ribs.scguns.client.util;

import com.geckolib.cache.model.GeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.client.compat.OptionalClientCompat;
import top.ribs.scguns.client.render.gun.GunRenderContext;
import top.ribs.scguns.client.render.gun.ScgunsLegacyItemTransforms;

/** 26.2 render helpers used by Scorched Guns' custom weapon pipeline. */
public final class RenderUtil {
    private static ModelPart regularRightArm;
    private static ModelPart regularRightSleeve;
    private static ModelPart regularLeftArm;
    private static ModelPart regularLeftSleeve;
    private static ModelPart slimRightArm;
    private static ModelPart slimRightSleeve;
    private static ModelPart slimLeftArm;
    private static ModelPart slimLeftSleeve;

    private RenderUtil() {}

    public static void rotateZ(PoseStack poseStack, float xOffset, float yOffset, float rotation) {
        poseStack.translate(xOffset, yOffset, 0);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZN.rotationDegrees(rotation)));
        poseStack.translate(-xOffset, -yOffset, 0);
    }

    public static void applyTransformType(ItemStack stack, PoseStack poseStack, ItemDisplayContext display, @Nullable LivingEntity entity) {
        ScgunsLegacyItemTransforms.apply(stack, poseStack, display);
    }

    public static boolean isMouseWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack poseStack,
                                            SubmitNodeCollector collector, int combinedLight) {
        if (OptionalClientCompat.renderCleanFirstPersonArm(player, hand, poseStack, collector, combinedLight)) {
            return;
        }
        renderVanillaFirstPersonArm(player, hand, poseStack, collector, combinedLight);
    }

    /**
     * Submits Minecraft's dedicated vanilla first-person hand model without
     * running the optional compatibility hook. Use this only from vanilla
     * first-person hand coordinate space, not from an animated GeoBone callback.
     */
    public static void renderVanillaFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack poseStack,
                                                   SubmitNodeCollector collector, int combinedLight) {
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skin = player.getSkin().body().texturePath();
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, collector, combinedLight, skin,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(poseStack, collector, combinedLight, skin,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }

    /**
     * Renders a clean player arm in GeckoLib bone-local space.
     *
     * <p>Unlike PlayerRenderer#renderRightHand/renderLeftHand, this helper does not
     * re-apply Minecraft's normal first-person shoulder/hand transform. Animated
     * Scorched Guns already arrive here with the PoseStack transformed to the
     * animated GeoBone, so only the raw arm/sleeve geometry must be submitted.</p>
     */
    public static void renderBoneLocalFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack poseStack,
                                                     SubmitNodeCollector collector, GeoBone bone,
                                                     int combinedLight, int overlay) {
        boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
        ensureBoneLocalArmParts(slim);

        ModelPart armPart = getBoneLocalArmPart(slim, hand);
        ModelPart sleevePart = getBoneLocalSleevePart(slim, hand);
        if (armPart == null || sleevePart == null) {
            return;
        }

        prepareBoneLocalPart(armPart, bone);

        Identifier skin = player.getSkin().body().texturePath();
        PlayerModelPart sleeve = hand == HumanoidArm.RIGHT
                ? PlayerModelPart.RIGHT_SLEEVE
                : PlayerModelPart.LEFT_SLEEVE;
        boolean showSleeve = player.isModelPartShown(sleeve);

        // Minecraft 26.2's first-person hand model may store the sleeve inside
        // the arm ModelPart hierarchy. In that layout, submitting the arm is
        // recursive and already submits the sleeve. Rendering the sleeve again
        // as an independent part duplicates geometry; worse, the first recursive
        // pass used entitySolid, turning transparent outer-skin pixels black.
        // Keep the nested sleeve at its baked relative pose and submit the whole
        // hand hierarchy once with the translucent entity pipeline, matching the
        // modern AvatarRenderer single-arm contract.
        if (containsPart(armPart, sleevePart)) {
            prepareNestedSleevePart(sleevePart, showSleeve);
            collector.submitModelPart(armPart, poseStack, RenderTypes.entityTranslucent(skin), combinedLight, overlay, null);
            return;
        }

        // Fallback for an old/third-party layout where arm and sleeve are
        // siblings rather than a single hierarchy.
        collector.submitModelPart(armPart, poseStack, RenderTypes.entitySolid(skin), combinedLight, overlay, null);
        if (showSleeve) {
            prepareBoneLocalPart(sleevePart, bone);
            collector.submitModelPart(sleevePart, poseStack, RenderTypes.entityTranslucent(skin), combinedLight, overlay, null);
        }
    }

    private static void ensureBoneLocalArmParts(boolean slim) {
        if (slim) {
            if (slimRightArm == null) {
                bakeBoneLocalArmParts(true);
            }
        } else if (regularRightArm == null) {
            bakeBoneLocalArmParts(false);
        }
    }

    private static void bakeBoneLocalArmParts(boolean slim) {
        LayerDefinition definition = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64);
        ModelPart root = definition.bakeRoot();
        // Minecraft 26.2 changed the internal PlayerModel part hierarchy. Do not
        // assume that sleeves are direct children of the baked root. Let the
        // vanilla PlayerModel constructor resolve its own current hierarchy, then
        // reuse the public arm/sleeve fields as detached first-person geometry.
        PlayerModel playerModel = new PlayerModel(root, slim);
        if (slim) {
            slimRightArm = playerModel.rightArm;
            slimRightSleeve = playerModel.rightSleeve;
            slimLeftArm = playerModel.leftArm;
            slimLeftSleeve = playerModel.leftSleeve;
        } else {
            regularRightArm = playerModel.rightArm;
            regularRightSleeve = playerModel.rightSleeve;
            regularLeftArm = playerModel.leftArm;
            regularLeftSleeve = playerModel.leftSleeve;
        }
    }

    private static ModelPart getBoneLocalArmPart(boolean slim, HumanoidArm hand) {
        if (slim) {
            return hand == HumanoidArm.RIGHT ? slimRightArm : slimLeftArm;
        }
        return hand == HumanoidArm.RIGHT ? regularRightArm : regularLeftArm;
    }

    private static ModelPart getBoneLocalSleevePart(boolean slim, HumanoidArm hand) {
        if (slim) {
            return hand == HumanoidArm.RIGHT ? slimRightSleeve : slimLeftSleeve;
        }
        return hand == HumanoidArm.RIGHT ? regularRightSleeve : regularLeftSleeve;
    }

    private static void prepareBoneLocalPart(ModelPart part, GeoBone bone) {
        part.resetPose();
        part.visible = true;
        part.skipDraw = false;
        part.xScale = 1.0F;
        part.yScale = 1.0F;
        part.zScale = 1.0F;
        // Scorched Guns 1.20.1 explicitly positioned BOTH the arm and sleeve
        // ModelParts at the animated GeoBone pivot after applying its custom
        // first-person scale/offset. HF84.1-HF84.4 incorrectly zeroed this pose
        // because GeckoLib 5's per-bone callback itself sits at the pivot. HF84.5
        // restores the old two-step contract: callback -> away from pivot -> arm
        // scale/offset -> ModelPart back to pivot.
        part.setPos(bone.pivotX(), bone.pivotY(), bone.pivotZ());
        part.setRotation(0.0F, 0.0F, 0.0F);
    }

    private static void prepareNestedSleevePart(ModelPart sleevePart, boolean visible) {
        // resetPose restores the sleeve's baked LOCAL pose relative to its arm.
        // Do not zero position/rotation here: that was the HF84.3 visual bug.
        sleevePart.resetPose();
        sleevePart.visible = visible;
        sleevePart.skipDraw = false;
        sleevePart.xScale = 1.0F;
        sleevePart.yScale = 1.0F;
        sleevePart.zScale = 1.0F;
    }

    private static boolean containsPart(ModelPart parent, ModelPart candidate) {
        for (ModelPart part : parent.getAllParts()) {
            if (part == candidate) {
                return true;
            }
        }
        return false;
    }

    /** Native ItemStackRenderState path, useful for trails and other secondary item renders. */
    public static void renderModel(ItemStack stack, ItemDisplayContext display, PoseStack poseStack,
                                   SubmitNodeCollector collector, int light, int overlay,
                                   @Nullable Level level, @Nullable LivingEntity entity) {
        new GunRenderContext(collector, display).submitItemStack(stack, poseStack, light, overlay, entity);
    }

    // GeckoLib 5.5 bone transform helpers used by AnimatedGunRenderer.
    public static void translateMatrixToBone(PoseStack poseStack, GeoBone bone) {
        bone.frameSnapshot.translate(poseStack);
    }

    public static void translateToPivotPoint(PoseStack poseStack, GeoBone bone) {
        bone.translateToPivotPoint(poseStack);
    }

    public static void rotateMatrixAroundBone(PoseStack poseStack, GeoBone bone) {
        bone.frameSnapshot.rotate(poseStack);
    }

    public static void scaleMatrixForBone(PoseStack poseStack, GeoBone bone) {
        bone.frameSnapshot.scale(poseStack);
    }

    public static void translateAwayFromPivotPoint(PoseStack poseStack, GeoBone bone) {
        bone.translateAwayFromPivotPoint(poseStack);
    }
}
