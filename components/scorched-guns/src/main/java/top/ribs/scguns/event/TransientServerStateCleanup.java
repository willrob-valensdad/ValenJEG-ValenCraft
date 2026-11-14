package top.ribs.scguns.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.BeamHandlerCommon;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.GunEffectsHandler;
import top.ribs.scguns.entity.monster.HiveEntity;

/**
 * Releases process-wide transient gameplay state when an integrated/dedicated
 * server stops. None of this data is persistent game data; keeping it after the
 * Level instances are gone can retain an old world or leak state into the next
 * session.
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public final class TransientServerStateCleanup {
    private TransientServerStateCleanup() {
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GuanoItemEventHandler.clearTrackedItems();
        HiveEntity.clearSummonedSwarmCache();
        BeamHandlerCommon.BeamMiningManager.clearTransientState();
        GunEffectsHandler.clearTransientState();
        ChargeHandler.clearAll();
        HotBarrelCache.clearAll();
    }
}
