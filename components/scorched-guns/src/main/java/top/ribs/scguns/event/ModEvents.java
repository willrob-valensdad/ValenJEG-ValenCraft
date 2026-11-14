package top.ribs.scguns.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.effect.IncurableEffect;

/**
 * Common NeoForge event hooks that still require imperative handling in 26.2.
 *
 * <p>Villager and wandering-trader offers are data-driven in 26.2 and are now
 * supplied by the villager_trade/trade_set resources installed by Hotfix 67.</p>
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModEvents {
    private ModEvents() {
    }

    @SubscribeEvent
    public static void preventIncurableEffectRemoval(MobEffectEvent.Remove event) {
        if (event.getEffect().value() instanceof IncurableEffect) {
            event.setCanceled(true);
        }
    }
}
