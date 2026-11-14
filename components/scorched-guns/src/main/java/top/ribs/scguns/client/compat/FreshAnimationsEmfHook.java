package top.ribs.scguns.client.compat;

import net.minecraft.client.model.HumanoidModel;
import top.ribs.scguns.ScorchedGuns;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.animation.state.EMFBipedPose;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.animation.state.EMFState;
import traben.entity_model_features.models.parts.EMFModelPartRoot;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.Map;
import java.util.UUID;

/**
 * EMF 3.3+ animation-hook integration for Scorched Guns.
 *
 * <p>This intentionally uses EMF's public hook instead of injecting into
 * EMFModelPartRoot internals. Fresh Animations is allowed to finish first; the
 * gun-aligned head and arms are restored afterward, while body/legs/root
 * movement stays under the resource pack.</p>
 */
public final class FreshAnimationsEmfHook extends EMFAnimationApi.EMFAnimationHook {
    private static boolean registered;

    private FreshAnimationsEmfHook() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        try {
            registered = EMFAnimationApi.registerAnimationHook(new FreshAnimationsEmfHook());
            if (registered) {
                ScorchedGuns.LOGGER.info("Enabled EMF 3.3+ animation-hook compatibility for Scorched Guns player poses.");
            }
        } catch (Throwable throwable) {
            ScorchedGuns.LOGGER.warn("Could not register Scorched Guns' optional EMF animation hook.", throwable);
        }
    }

    @Override
    public void onAnimationEnd(AnimationContext context, boolean wasCancelledByHook) {
        try {
            EMFEntityRenderState state = context.activeState();
            if (state == null || state.isFirstPersonHand()) {
                return;
            }

            UUID uuid = state.uuid();
            if (uuid == null) {
                return;
            }
            EMFModelPartRoot root = context.animatingModelRoot();
            if (root == null) {
                return;
            }

            Map<String, EMFModelPartVanilla> parts = root.getAllVanillaPartsByNameEMF();

            if (FreshAnimationsCompat.hasGunPose(uuid)) {
                FreshAnimationsCompat.storeAnimatedRoot(uuid, root);
                FreshAnimationsCompat.restoreAnimatedPose(
                        uuid,
                        parts.get("body"),
                        parts.get("head"),
                        parts.get("hat"),
                        parts.get("left_arm"),
                        parts.get("right_arm"),
                        parts.get("left_sleeve"),
                        parts.get("right_sleeve")
                );
            } else {
                FreshAnimationsCompat.clearRoot(uuid);
            }

            // Capture the armor-driving pose only from the player's MAIN model.
            // onAnimationEnd also runs for animated feature/layer models; allowing
            // those passes to overwrite the UUID snapshot can replace the correct
            // player root with a layer-local root later in the same frame.
            if (EMFState.isMainPhase && !EMFState.isLayerPhase) {
                FreshAnimationsArmorCompat.captureFromEmf(
                        uuid,
                        root,
                        parts.get("head"),
                        parts.get("body"),
                        parts.get("left_arm"),
                        parts.get("right_arm"),
                        parts.get("left_leg"),
                        parts.get("right_leg")
                );
            }
        } catch (Throwable throwable) {
            ScorchedGuns.LOGGER.debug("Skipped one EMF gun-pose restore pass.", throwable);
        }
    }

    @Override
    public void onBipedPoseCopyEnd(EMFBipedPose pose, HumanoidModel<?> model, boolean wasCancelledByHook) {
        try {
            EMFEntityRenderState state = EMFState.state();
            if (state == null || state.isFirstPersonHand()) {
                return;
            }
            UUID uuid = state.uuid();
            if (uuid != null) {
                FreshAnimationsCompat.restoreArmorPose(uuid, model);
                FreshAnimationsArmorCompat.captureFromBiped(uuid, model);
            }
        } catch (Throwable throwable) {
            ScorchedGuns.LOGGER.debug("Skipped one EMF armor gun-pose restore pass.", throwable);
        }
    }
}
