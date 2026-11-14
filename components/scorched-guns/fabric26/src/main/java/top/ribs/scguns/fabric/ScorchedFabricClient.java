package top.ribs.scguns.fabric;
import com.mrcrayfish.framework.api.network.ClientReceivers;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
public final class ScorchedFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        for (ClientReceivers.Binding binding : ClientReceivers.BINDINGS) {
            ClientPlayNetworking.registerGlobalReceiver(binding.type(), (payload, context) ->
                    binding.handler().accept(payload, context.player()));
        }
        ClientPlayNetworking.registerGlobalReceiver(MenuExtras.TYPE, (payload, context) -> MenuExtras.pending(payload.data()));
        ClientPlayNetworking.registerGlobalReceiver(SpawnExtras.TYPE, (payload, context) ->
                context.client().execute(() -> SpawnExtras.accept(payload.entityId(), payload.data())));
        ScorchedFabric.MOD_BUS.post(new RegisterKeyMappingsEvent());
        ScorchedFabric.MOD_BUS.post(new RegisterMenuScreensEvent());
        ScorchedFabric.MOD_BUS.post(new net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers());
        ScorchedFabric.MOD_BUS.post(new FMLClientSetupEvent());
        ScorchedFabric.MOD_BUS.post(new RegisterParticleProvidersEvent());
        ClientPlayNetworking.registerGlobalReceiver(SyncedEntityData.StatePayload.CLIENTBOUND, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().level == null) return;
                    Player player = context.client().level.getPlayerByUUID(payload.playerId());
                    if (player != null) SyncedEntityData.applyClient(player, payload);
                }));
    }
}
