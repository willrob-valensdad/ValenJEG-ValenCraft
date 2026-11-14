package ttv.migami.jeg.legacy;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import ttv.migami.jeg.Reference;
/** Server measures draw time; packet supplies only press/release, never damage or charge. */
public record BowTriggerPayload(boolean holding) implements CustomPacketPayload {
    public static final Type<BowTriggerPayload> TYPE=new Type<>(Reference.id("legacy_bow_trigger"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BowTriggerPayload> STREAM_CODEC=StreamCodec.of((buf,p)->buf.writeBoolean(p.holding()),buf->new BowTriggerPayload(buf.readBoolean()));
    @Override public Type<BowTriggerPayload> type() { return TYPE; }
    private record Charge(net.minecraft.world.item.ItemStack stack,long tick) {}
    private static final java.util.Map<net.minecraft.server.level.ServerPlayer,Charge> CHARGES=new java.util.WeakHashMap<>();
    public static void handle(net.minecraft.server.level.ServerPlayer player,boolean holding) {
        var stack=player.getMainHandItem();
        if(holding) {
            if(stack.getItem() instanceof CompoundBowItem bow && !CHARGES.containsKey(player) && bow.canStart(player,stack)) CHARGES.put(player,new Charge(stack,player.level().getGameTime()));
        } else {
            var charge=CHARGES.remove(player);
            if(charge==null || charge.stack()!=stack || !(stack.getItem() instanceof CompoundBowItem bow))return;
            int ticks=(int)Math.clamp(player.level().getGameTime()-charge.tick(),0L,72000L);
            bow.releaseUsing(stack,player.level(),player,72000-ticks);
        }
    }
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE,STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE,(payload,context)->context.server().execute(()->handle(context.player(),payload.holding())));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->CHARGES.remove(handler.getPlayer()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->CHARGES.entrySet().removeIf(entry->!entry.getKey().isAlive() || entry.getKey().isSpectator() || entry.getKey().getMainHandItem()!=entry.getValue().stack() || entry.getKey().level().getGameTime()-entry.getValue().tick()>72000));
    }
}
