package top.ribs.scguns.fabric;

import io.netty.buffer.Unpooled;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/** Sends the spawn bytes 26.3's add-entity packet no longer carries. */
public final class SpawnExtras {
    public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("scguns", "spawn_extra"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of(SpawnExtras::write, SpawnExtras::read);
    private static final Map<Integer, Pending> PENDING = new ConcurrentHashMap<>();
    private SpawnExtras() {}

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static void send(Entity entity) {
        if (!(entity instanceof IEntityWithComplexSpawn extra)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            extra.writeSpawnData(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            Payload payload = new Payload(entity.getId(), data);
            for (ServerPlayer player : PlayerLookup.tracking(entity)) {
                ServerPlayNetworking.send(player, payload);
            }
        } finally {
            buf.release();
        }
    }

    public static void accept(int entityId, byte[] data) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Entity entity = minecraft.level.getEntity(entityId);
            if (entity instanceof IEntityWithComplexSpawn spawn) {
                apply(spawn, data, minecraft.level.registryAccess());
                return;
            }
        }
        PENDING.put(entityId, new Pending(data, 0));
    }

    public static void clientTick() {
        if (PENDING.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Iterator<Map.Entry<Integer, Pending>> entries = PENDING.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, Pending> entry = entries.next();
            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity instanceof IEntityWithComplexSpawn spawn) {
                apply(spawn, entry.getValue().data(), minecraft.level.registryAccess());
                entries.remove();
            } else if (entry.getValue().age() > 40) {
                entries.remove();
            } else {
                entry.setValue(new Pending(entry.getValue().data(), entry.getValue().age() + 1));
            }
        }
    }

    private static void apply(IEntityWithComplexSpawn spawn, byte[] data, RegistryAccess registries) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), registries);
        try {
            spawn.readSpawnData(buf);
        } finally {
            buf.release();
        }
    }

    private static void write(RegistryFriendlyByteBuf buf, Payload payload) {
        buf.writeVarInt(payload.entityId());
        buf.writeVarInt(payload.data().length);
        buf.writeBytes(payload.data());
    }

    private static Payload read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        byte[] data = new byte[buf.readVarInt()];
        buf.readBytes(data);
        return new Payload(entityId, data);
    }

    private record Pending(byte[] data, int age) {}

    public record Payload(int entityId, byte[] data) implements CustomPacketPayload {
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
