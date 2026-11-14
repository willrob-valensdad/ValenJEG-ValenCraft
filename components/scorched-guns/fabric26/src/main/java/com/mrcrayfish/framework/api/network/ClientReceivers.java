package com.mrcrayfish.framework.api.network;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
public final class ClientReceivers {
    public record Binding(CustomPacketPayload.Type<FrameworkPayload> type, BiConsumer<FrameworkPayload, Player> handler) {}
    public static final List<Binding> BINDINGS = new ArrayList<>();
    private ClientReceivers() {}
    public static void register(CustomPacketPayload.Type<FrameworkPayload> type, BiConsumer<FrameworkPayload, Player> handler) {
        BINDINGS.add(new Binding(type, handler));
    }
}
