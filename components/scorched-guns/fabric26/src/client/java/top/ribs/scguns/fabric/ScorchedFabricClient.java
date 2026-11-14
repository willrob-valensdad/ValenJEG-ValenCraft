package top.ribs.scguns.fabric;
import com.mrcrayfish.framework.api.network.ClientReceivers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
public final class ScorchedFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        for (ClientReceivers.Binding binding : ClientReceivers.BINDINGS) {
            ClientPlayNetworking.registerGlobalReceiver(binding.type(), (payload, context) ->
                    binding.handler().accept(payload, context.player()));
        }
    }
}
