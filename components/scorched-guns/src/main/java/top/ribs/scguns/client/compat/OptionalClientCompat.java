package top.ribs.scguns.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import com.geckolib.cache.model.GeoBone;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.item.GunItem;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import net.minecraft.world.entity.player.PlayerModelType;
public final class OptionalClientCompat {
    private static final String EMF_ANIMATION_API = "traben.entity_model_features.EMFAnimationApi";
    private static final String EMF_STATE = "traben.entity_model_features.models.animation.state.EMFState";
    private static final String EMF_ANIMATION_HOOK = "traben.entity_model_features.EMFAnimationApi$EMFAnimationHook";
    private static final String SCGUNS_EMF_HOOK = "top.ribs.scguns.client.compat.FreshAnimationsEmfHook";
    private static Boolean entityModelFeaturesLoaded;
    private static boolean emfVariableRegistered;

    private static ModelPart regularRightArm;
    private static ModelPart regularRightSleeve;
    private static ModelPart regularLeftArm;
    private static ModelPart regularLeftSleeve;
    private static ModelPart slimRightArm;
    private static ModelPart slimRightSleeve;
    private static ModelPart slimLeftArm;
    private static ModelPart slimLeftSleeve;

    private OptionalClientCompat() {
    }

    public static boolean isEntityModelFeaturesLoaded() {
        if (entityModelFeaturesLoaded == null) {
            entityModelFeaturesLoaded = isClassPresent(EMF_ANIMATION_API)
                    && isClassPresent(EMF_STATE)
                    && isClassPresent(EMF_ANIMATION_HOOK);
        }
        return entityModelFeaturesLoaded;
    }

    public static void registerEntityModelFeaturesVariables() {
        if (!isEntityModelFeaturesLoaded()) {
            return;
        }

        if (!emfVariableRegistered) {
            try {
                Class<?> api = Class.forName(EMF_ANIMATION_API, false, OptionalClientCompat.class.getClassLoader());
                Method register = api.getMethod(
                        "registerSingletonAnimationVariable",
                        String.class,
                        String.class,
                        String.class,
                        BooleanSupplier.class
                );
                register.invoke(
                        null,
                        Reference.MOD_ID,
                        "is_scguns_gun_held",
                        "True when the rendered entity is holding a Scorched Guns gun item.",
                        (BooleanSupplier) OptionalClientCompat::isScorchedGunHeldByEntityModelFeaturesContext
                );
                emfVariableRegistered = true;
            } catch (ReflectiveOperationException | LinkageError e) {
                ScorchedGuns.LOGGER.debug("Entity Model Features animation variable registration was skipped.", e);
            }
        }

        registerEntityModelFeaturesAnimationHook();
    }

    private static void registerEntityModelFeaturesAnimationHook() {
        try {
            Class<?> hook = Class.forName(SCGUNS_EMF_HOOK, true, OptionalClientCompat.class.getClassLoader());
            hook.getMethod("register").invoke(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            ScorchedGuns.LOGGER.debug("Entity Model Features animation hook registration was skipped.", e);
        }
    }

    public static boolean isScorchedGunHeldByLocalPlayer() {
        return isScorchedGunHeldByPlayer(Minecraft.getInstance().player);
    }

    public static boolean isScorchedGunHeldByPlayer(Player player) {
        return player != null && (isScorchedGunStack(player.getMainHandItem()) || isScorchedGunStack(player.getOffhandItem()));
    }

    public static boolean isScorchedGunStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    public static boolean renderCleanFirstPersonArm(LocalPlayer player, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector buffer, int light) {
        if (!isEntityModelFeaturesLoaded() || !isScorchedGunHeldByPlayer(player)) {
            return false;
        }
        boolean slim = isSlim(player);
        ensureArmParts(slim);
        ModelPart armPart = getArmPart(slim, arm);
        ModelPart sleevePart = getSleevePart(slim, arm);
        if (armPart == null || sleevePart == null) {
            return false;
        }

        armPart.resetPose();
        armPart.visible = true;
        armPart.skipDraw = false;

        Identifier playerSkin = player.getSkin().body().texturePath();
        PlayerModelPart sleeve = arm == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE;
        boolean showSleeve = player.isModelPartShown(sleeve);

        if (containsPart(armPart, sleevePart)) {
            prepareNestedSleevePart(sleevePart, showSleeve);
            buffer.submitModelPart(armPart, poseStack, RenderTypes.entityTranslucent(playerSkin), light, OverlayTexture.NO_OVERLAY, null);
            return true;
        }

        sleevePart.resetPose();
        sleevePart.loadPose(armPart.storePose());
        buffer.submitModelPart(armPart, poseStack, RenderTypes.entitySolid(playerSkin), light, OverlayTexture.NO_OVERLAY, null);
        if (showSleeve) {
            buffer.submitModelPart(sleevePart, poseStack, RenderTypes.entityTranslucent(playerSkin), light, OverlayTexture.NO_OVERLAY, null);
        }
        return true;
    }

    public static boolean renderCleanAnimatedArm(LocalPlayer player, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector buffer, Identifier playerSkin, GeoBone bone, int light, int overlay) {
        if (!isEntityModelFeaturesLoaded() || !isScorchedGunHeldByPlayer(player)) {
            return false;
        }
        boolean slim = isSlim(player);
        ensureArmParts(slim);
        ModelPart armPart = getArmPart(slim, arm);
        ModelPart sleevePart = getSleevePart(slim, arm);
        if (armPart == null || sleevePart == null) {
            return false;
        }

        // GeckoLib 5 per-bone tasks enter at the bone pivot, while the original
        // GeckoLib 4 renderer applied the custom arm transform from the post-
        // prepMatrixForBone (away-from-pivot) space. Restore that ordering here
        // as well so the optional EMF path matches vanilla Scorched Guns parity.
        bone.translateAwayFromPivotPoint(poseStack);
        poseStack.scale(0.66F, arm == HumanoidArm.RIGHT ? 0.78F : 0.79F, 0.66F);
        poseStack.translate(arm == HumanoidArm.RIGHT ? 0.25D : -0.25D, -0.1D, 0.1625D);

        prepareArmPart(armPart, bone);
        PlayerModelPart sleeve = arm == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE;
        boolean showSleeve = player.isModelPartShown(sleeve);

        if (containsPart(armPart, sleevePart)) {
            prepareNestedSleevePart(sleevePart, showSleeve);
            buffer.submitModelPart(armPart, poseStack, RenderTypes.entityTranslucent(playerSkin), light, overlay, null);
            return true;
        }

        buffer.submitModelPart(armPart, poseStack, RenderTypes.entitySolid(playerSkin), light, overlay, null);
        if (showSleeve) {
            prepareArmPart(sleevePart, bone);
            buffer.submitModelPart(sleevePart, poseStack, RenderTypes.entityTranslucent(playerSkin), light, overlay, null);
        }
        return true;
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent<?> event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isEntityModelFeaturesLoaded() && isScorchedGunHeldByPlayer(player)) {
            event.setCanceled(true);
        }
    }

    private static boolean isScorchedGunHeldByEntityModelFeaturesContext() {
        Object entity = getEntityModelFeaturesContextEntity();
        if (entity != null) {
            try {
                Method handItems = entity.getClass().getMethod("etf$getHandItems");
                Object value = handItems.invoke(entity);
                if (value instanceof Iterable<?> iterable) {
                    for (Object entry : iterable) {
                        if (entry instanceof ItemStack stack && isScorchedGunStack(stack)) {
                            return true;
                        }
                    }
                    return false;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
        return isScorchedGunHeldByLocalPlayer();
    }

    private static boolean isSlim(LocalPlayer player) {
        return player.getSkin().model() == PlayerModelType.SLIM;
    }

    private static void ensureArmParts(boolean slim) {
        if (slim) {
            if (slimRightArm == null) {
                bakeArmParts(true);
            }
        } else if (regularRightArm == null) {
            bakeArmParts(false);
        }
    }

    private static void bakeArmParts(boolean slim) {
        LayerDefinition layerDefinition = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64);
        ModelPart root = layerDefinition.bakeRoot();
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

    private static ModelPart getArmPart(boolean slim, HumanoidArm arm) {
        if (slim) {
            return arm == HumanoidArm.RIGHT ? slimRightArm : slimLeftArm;
        }
        return arm == HumanoidArm.RIGHT ? regularRightArm : regularLeftArm;
    }

    private static ModelPart getSleevePart(boolean slim, HumanoidArm arm) {
        if (slim) {
            return arm == HumanoidArm.RIGHT ? slimRightSleeve : slimLeftSleeve;
        }
        return arm == HumanoidArm.RIGHT ? regularRightSleeve : regularLeftSleeve;
    }

    private static void prepareArmPart(ModelPart armPart, GeoBone bone) {
        armPart.resetPose();
        armPart.visible = true;
        armPart.skipDraw = false;
        armPart.xScale = 1.0F;
        armPart.yScale = 1.0F;
        armPart.zScale = 1.0F;
        // Match Scorched Guns 1.20.1: after restoring the callback to the
        // away-from-pivot space and applying the arm scale/offset, place the
        // vanilla ModelPart back at the animated bone pivot.
        armPart.setPos(bone.pivotX(), bone.pivotY(), bone.pivotZ());
        armPart.setRotation(0.0F, 0.0F, 0.0F);
    }

    private static void prepareNestedSleevePart(ModelPart sleevePart, boolean visible) {
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

    public static Object getEntityModelFeaturesContextEntity() {
        try {
            Class<?> api = Class.forName(EMF_ANIMATION_API, false, OptionalClientCompat.class.getClassLoader());
            Method getCurrentEntity = api.getMethod("getCurrentEntity");
            return getCurrentEntity.invoke(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public static boolean isEntityModelFeaturesFirstPersonHand() {
        try {
            Class<?> stateClass = Class.forName(EMF_STATE, false, OptionalClientCompat.class.getClassLoader());
            Method stateMethod = stateClass.getMethod("state");
            Object state = stateMethod.invoke(null);
            if (state == null) {
                return false;
            }
            Method firstPersonHand = state.getClass().getMethod("isFirstPersonHand");
            return Boolean.TRUE.equals(firstPersonHand.invoke(state));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, OptionalClientCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
