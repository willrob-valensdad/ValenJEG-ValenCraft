package top.ribs.scguns.fabric;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
/** Sends the extra menu bytes 26.3's open-screen packet no longer carries, before the screen opens. */
public final class MenuExtras {
    public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("scguns", "menu_extra"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of(MenuExtras::write, MenuExtras::read);
    private static final AtomicReference<byte[]> PENDING = new AtomicReference<>();
    private MenuExtras() {}
    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
    public static void open(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            ServerPlayNetworking.send(player, new Payload(data));
        } finally {
            buf.release();
        }
        player.openMenu(provider);
    }
    public static void pending(byte[] data) { PENDING.set(data); }
    public static FriendlyByteBuf consume() {
        byte[] data = PENDING.getAndSet(null);
        if (data == null) return new FriendlyByteBuf(Unpooled.buffer());
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }
    private static void write(RegistryFriendlyByteBuf buf, Payload payload) {
        buf.writeVarInt(payload.data().length);
        buf.writeBytes(payload.data());
    }
    private static Payload read(RegistryFriendlyByteBuf buf) {
        byte[] data = new byte[buf.readVarInt()];
        buf.readBytes(data);
        return new Payload(data);
    }
    public record Payload(byte[] data) implements CustomPacketPayload {
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
