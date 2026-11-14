package com.mrcrayfish.framework.api.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public final class FrameworkNetwork {
    private final Map<Class<?>, CustomPacketPayload.Type<FrameworkPayload>> types = new HashMap<>();
    private final Map<Class<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> codecs = new HashMap<>();

    private FrameworkNetwork() {}

    public static Builder builder(Identifier channel) { return new Builder(channel); }

    public <T> void sendToServer(T message) {
        try {
            Class<?> client = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
            client.getMethod("send", CustomPacketPayload.class).invoke(null, payload(message));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Client play channel is unavailable", failure);
        }
    }
    public <T> void sendToPlayer(Supplier<? extends ServerPlayer> player, T message) {
        ServerPlayer target = player.get();
        if (target != null) ServerPlayNetworking.send(target, payload(message));
    }
    public <T> void sendToAll(T message) {
        ServerLevel level = currentServerLevel();
        if (level == null) return;
        FrameworkPayload packet = payload(message);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) ServerPlayNetworking.send(player, packet);
    }
    public <T> void sendToTrackingChunk(Supplier<LevelChunk> chunk, T message) {
        LevelChunk levelChunk = chunk.get();
        if (!(levelChunk.getLevel() instanceof ServerLevel server)) return;
        FrameworkPayload packet = payload(message);
        for (ServerPlayer player : server.players()) {
            if (player.chunkPosition().getChessboardDistance(levelChunk.getPos()) <= server.getServer().getPlayerList().getViewDistance()) {
                ServerPlayNetworking.send(player, packet);
            }
        }
    }
    public <T> void sendToTrackingEntity(Supplier<? extends Entity> entity, T message) {
        Entity target = entity.get();
        if (target == null || !(target.level() instanceof ServerLevel server)) return;
        FrameworkPayload packet = payload(message);
        for (ServerPlayer player : server.players()) {
            if (player.chunkPosition().getChessboardDistance(target.chunkPosition()) <= server.getServer().getPlayerList().getViewDistance()) {
                ServerPlayNetworking.send(player, packet);
            }
        }
    }
    public <T> void sendToNearbyPlayers(Supplier<LevelLocation> location, T message) {
        LevelLocation point = location.get();
        if (!(point.level instanceof ServerLevel server)) return;
        FrameworkPayload packet = payload(message);
        double radius = point.radius;
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(point.x, point.y, point.z) <= radius * radius) ServerPlayNetworking.send(player, packet);
        }
    }
    @SuppressWarnings("unchecked")
    private <T> FrameworkPayload payload(T message) {
        CustomPacketPayload.Type<FrameworkPayload> type = (CustomPacketPayload.Type<FrameworkPayload>) types.get(message.getClass());
        if (type == null) throw new IllegalStateException("Unregistered play message " + message.getClass().getName());
        return new FrameworkPayload(type, message);
    }
    private static ServerLevel currentServerLevel() { return null; }

    public static final class Builder {
        private final FrameworkNetwork network = new FrameworkNetwork();
        private final Identifier channel;
        private Builder(Identifier channel) { this.channel = channel; }
        public <T> Builder registerPlayMessage(String name, Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PlayMessageContext> handler, PacketFlow flow) {
            CustomPacketPayload.Type<FrameworkPayload> id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(channel.getNamespace(), name));
            StreamCodec<RegistryFriendlyByteBuf, FrameworkPayload> wrapped = StreamCodec.of(
                    (buffer, payload) -> codec.encode(buffer, type.cast(payload.value)),
                    buffer -> new FrameworkPayload(id, codec.decode(buffer)));
            network.types.put(type, id);
            network.codecs.put(type, codec);
            if (flow == PacketFlow.SERVERBOUND) {
                PayloadTypeRegistry.serverboundPlay().register(id, wrapped);
                ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> handler.accept(type.cast(payload.value), new PlayMessageContext(context.player())));
            } else {
                PayloadTypeRegistry.clientboundPlay().register(id, wrapped);
                ClientReceivers.register(id, (payload, player) -> handler.accept(type.cast(payload.value), new PlayMessageContext(player)));
            }
            return this;
        }
        public FrameworkNetwork build() { return network; }
    }
}
