package com.mrcrayfish.framework.entity.sync;
import com.mrcrayfish.framework.api.sync.Serializers;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
/** Server-owned copies of aiming, reload, shoot, burst, and melee. Clients render the broadcast values. */
public final class SyncedEntityData {
    private static final List<SyncedDataKey<?, ?>> KEYS = new ArrayList<>();
    private static final Identifier SERVERBOUND_ID = Identifier.fromNamespaceAndPath("scguns", "synced_state_c2s");
    private static final Identifier CLIENTBOUND_ID = Identifier.fromNamespaceAndPath("scguns", "synced_state_s2c");
    private static SyncedEntityData instance;
    private SyncedEntityData() {}
    public static SyncedEntityData instance() {
        if (instance == null) instance = new SyncedEntityData();
        return instance;
    }
    public void registerDataKey(SyncedDataKey<?, ?> key) { KEYS.add(key); }
    public static void initPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(StatePayload.SERVERBOUND, StatePayload.SERVER_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatePayload.CLIENTBOUND, StatePayload.CLIENT_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StatePayload.SERVERBOUND, (payload, context) ->
                context.server().execute(() -> applyServer(context.player(), payload)));
    }
    public static List<SyncedDataKey<?, ?>> keys() { return KEYS; }
    public static void broadcast(SyncedDataKey<?, ?> key, ServerPlayer owner) {
        StatePayload payload = StatePayload.of(owner.getUUID(), key, key.read(owner));
        for (ServerPlayer watcher : owner.level().players()) {
            if (watcher.chunkPosition().getChessboardDistance(owner.chunkPosition()) <= owner.level().getServer().getPlayerList().getViewDistance() || watcher == owner) {
                ServerPlayNetworking.send(watcher, payload);
            }
        }
    }
    public static void sendAllTo(ServerPlayer joiner) {
        for (ServerPlayer owner : joiner.level().getServer().getPlayerList().getPlayers()) {
            for (SyncedDataKey<?, ?> key : KEYS) ServerPlayNetworking.send(joiner, StatePayload.of(owner.getUUID(), key, key.read(owner)));
        }
    }
    public static void resetOnDeath(ServerPlayer player) {
        for (SyncedDataKey<?, ?> key : KEYS) {
            if (key.resetOnDeath()) { key.writeAuthoritative(player, key.defaultValue()); broadcast(key, player); }
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> void requestServer(SyncedDataKey<?, T> key, T value) {
        try {
            Class<?> client = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
            client.getMethod("send", CustomPacketPayload.class).invoke(null, StatePayload.clientRequest(key, value));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not send synced gun state to the server", failure);
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyServer(ServerPlayer player, StatePayload payload) {
        SyncedDataKey key = find(payload.key);
        if (key == null) return;
        key.writeAuthoritative(player, payload.value);
        broadcast(key, player);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void applyClient(Player player, StatePayload payload) {
        SyncedDataKey key = find(payload.key);
        if (key != null) key.writeAuthoritative(player, payload.value);
    }
    private static SyncedDataKey<?, ?> find(Identifier id) {
        for (SyncedDataKey<?, ?> key : KEYS) if (id.equals(key.id())) return key;
        return null;
    }
    public static final class StatePayload implements CustomPacketPayload {
        public static final Type<StatePayload> SERVERBOUND = new Type<>(SERVERBOUND_ID);
        public static final Type<StatePayload> CLIENTBOUND = new Type<>(CLIENTBOUND_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> SERVER_CODEC = StreamCodec.of(StatePayload::write, buffer -> read(SERVERBOUND, buffer));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> CLIENT_CODEC = StreamCodec.of(StatePayload::write, buffer -> read(CLIENTBOUND, buffer));
        private final Type<StatePayload> type;
        public final java.util.UUID player;
        final Identifier key;
        final Object value;
        private StatePayload(Type<StatePayload> type, java.util.UUID player, Identifier key, Object value) {
            this.type = type; this.player = player; this.key = key; this.value = value;
        }
        static StatePayload of(java.util.UUID player, SyncedDataKey<?, ?> key, Object value) {
            return new StatePayload(CLIENTBOUND, player, key.id(), value);
        }
        static <T> StatePayload clientRequest(SyncedDataKey<?, T> key, T value) {
            return new StatePayload(SERVERBOUND, new java.util.UUID(0, 0), key.id(), value);
        }
        private static void write(RegistryFriendlyByteBuf buffer, StatePayload payload) {
            buffer.writeUUID(payload.player);
            buffer.writeIdentifier(payload.key);
            if (payload.value instanceof Boolean bool) { buffer.writeByte(1); buffer.writeBoolean(bool); }
            else if (payload.value instanceof Integer number) { buffer.writeByte(2); buffer.writeVarInt(number); }
            else throw new IllegalStateException("Unsupported synced value " + payload.value);
        }
        private static StatePayload read(Type<StatePayload> type, RegistryFriendlyByteBuf buffer) {
            java.util.UUID player = buffer.readUUID();
            Identifier key = buffer.readIdentifier();
            int kind = buffer.readByte();
            Object value = kind == 1 ? buffer.readBoolean() : buffer.readVarInt();
            return new StatePayload(type, player, key, value);
        }
        public java.util.UUID playerId() { return player; }
        @Override public Type<? extends CustomPacketPayload> type() { return type; }
    }
}
