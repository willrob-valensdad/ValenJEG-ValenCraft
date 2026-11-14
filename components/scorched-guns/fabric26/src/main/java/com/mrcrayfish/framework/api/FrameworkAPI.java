package com.mrcrayfish.framework.api;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import net.minecraft.resources.Identifier;
public final class FrameworkAPI {
    private FrameworkAPI() {}
    public static FrameworkNetwork.Builder createNetworkBuilder(Identifier channel, int protocol) {
        return FrameworkNetwork.builder(channel);
    }
}
