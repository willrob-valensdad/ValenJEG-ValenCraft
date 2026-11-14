package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.item.animated.AnimatedGunItem;

public class InspectHandler {
    private static InspectHandler instance;

    public static InspectHandler get() {
        if (instance == null) {
            instance = new InspectHandler();
        }
        return instance;
    }

    private InspectHandler() {
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof AnimatedGunItem gunItem) {
                if (KeyBinds.KEY_INSPECT.isDown() && event.getAction() == 1) {
                    long id = GeoItem.getId(heldItem);
                    AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache()
                            .getManagerForId(id)
                            .getAnimationControllers()
                            .get("controller");
                    if (animationController != null) {
                        boolean canInspect = true;
                        if (animationController.getCurrentAnimationPoint() != null) {
                            String currentAnim = animationController.getCurrentAnimationPoint().animation().name();
                            canInspect = !currentAnim.contains("draw") &&
                                    !currentAnim.contains("reload") &&
                                    !AimingHandler.get().isAiming();
                        }

                        if (canInspect) {
                            animationController.setAnimationSpeed(1.0);
                            animationController.reset();
                            boolean isCarbine = gunItem.isInCarbineMode(heldItem);
                            String animToPlay = isCarbine ? "carbine_inspect" : "inspect";
                            animationController.triggerAnimation(animToPlay);
                        }
                    }
                }
            }
        }
    }
}
