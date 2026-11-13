package ttv.migami.jeg.mteg;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import ttv.migami.jeg.network.BulletTrailPayload;
/** Reuses the accepted native trail transport while retaining original MTEG trail length. Existing JEG protocol is unchanged. */
public record MtegTrailPayload(BulletTrailPayload trail,float length) implements CustomPacketPayload {
 public static final Type<MtegTrailPayload> TYPE=new Type<>(MtegContent.id("bullet_trail"));
 public static final StreamCodec<RegistryFriendlyByteBuf,MtegTrailPayload> STREAM_CODEC=StreamCodec.of(
  (buffer,payload)->{BulletTrailPayload.STREAM_CODEC.encode(buffer,payload.trail());buffer.writeFloat(payload.length());},
  buffer->new MtegTrailPayload(BulletTrailPayload.STREAM_CODEC.decode(buffer),buffer.readFloat()));
 @Override public Type<MtegTrailPayload> type(){return TYPE;}
}
