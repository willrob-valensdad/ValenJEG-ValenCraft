package top.ribs.scguns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.client.compat.FreshAnimationsArmorCompat;
import top.ribs.scguns.client.compat.FreshAnimationsCompat;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.SoundHandler;
import top.ribs.scguns.common.ChargeHandler;

/** Client-side counterpart to TransientServerStateCleanup. */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class TransientClientStateCleanup {
    private TransientClientStateCleanup() {
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SoundHandler.get().clearTransientState();
        GunRenderingHandler.get().clearTransientState();
        BeamHandler.clearTransientState();
        FreshAnimationsCompat.clearAll();
        FreshAnimationsArmorCompat.clearAll();
        ChargeHandler.clearAll();
        HotBarrelCache.clearAll();
    }
}
