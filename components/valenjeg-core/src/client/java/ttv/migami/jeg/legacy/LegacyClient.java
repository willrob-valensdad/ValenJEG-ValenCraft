package ttv.migami.jeg.legacy;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
public final class LegacyClient {
    private LegacyClient() {}
    private static boolean holding;
    public static void register() {
        EntityRendererRegistry.register(LegacyContent.CHARGE_ENTITY,c->new ThrownItemRenderer<>(c,1F,true));
        EntityRendererRegistry.register(LegacyContent.ARROW_ENTITY,c->new ThrownItemRenderer<>(c,.5F,false));
        EntityRendererRegistry.register(LegacyContent.WHIRLPOOL_ENTITY,WhirlpoolRenderer::new);
        EntityRendererRegistry.register(LegacyContent.SPLASH_ENTITY,SplashRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            boolean down=client.player!=null && client.gui.screen()==null && client.player.getMainHandItem().getItem() instanceof CompoundBowItem bow && bow.canStart(client.player,client.player.getMainHandItem()) && client.options.keyAttack.isDown();
            CompoundBowItem.setClientHolding(down);
            if(down!=holding) {
                holding=down;
                if(client.player!=null && ClientPlayNetworking.canSend(BowTriggerPayload.TYPE))ClientPlayNetworking.send(new BowTriggerPayload(down));
            }
        });
    }
}
