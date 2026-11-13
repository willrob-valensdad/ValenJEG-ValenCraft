package ttv.migami.jeg.enchantment;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/** Source BulletTrail local-shooter particle metadata; existing JEG/MTEG trail protocols stay intact. */
public record LegacyEnchantmentTrailPayload(Vec3 position,Vec3 motion,int life,double gravity,int shooterId,int flags,boolean master) implements CustomPacketPayload {
    public static final Type<LegacyEnchantmentTrailPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("jeg","legacy_enchantment_trail"));
    public static final StreamCodec<RegistryFriendlyByteBuf,LegacyEnchantmentTrailPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);b.writeDouble(p.motion.x);b.writeDouble(p.motion.y);b.writeDouble(p.motion.z);b.writeInt(p.life);b.writeDouble(p.gravity);b.writeInt(p.shooterId);b.writeInt(p.flags);b.writeBoolean(p.master);},
        b->new LegacyEnchantmentTrailPayload(new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readInt(),b.readDouble(),b.readInt(),b.readInt(),b.readBoolean()));
    @Override public Type<LegacyEnchantmentTrailPayload> type(){return TYPE;}
    public static void register(){PayloadTypeRegistry.clientboundPlay().register(TYPE,CODEC);}
    public static void send(Projectile projectile,LegacyGunEnchantments.Snapshot snapshot,int life,double gravity,boolean master){
        if(!snapshot.any()||!(projectile.getOwner() instanceof ServerPlayer player)||!ServerPlayNetworking.canSend(player,TYPE))return;
        ServerPlayNetworking.send(player,new LegacyEnchantmentTrailPayload(projectile.position(),projectile.getDeltaMovement(),life,gravity,player.getId(),snapshot.flags(),master));
    }
}
