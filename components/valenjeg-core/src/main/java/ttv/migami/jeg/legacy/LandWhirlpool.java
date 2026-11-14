package ttv.migami.jeg.legacy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import ttv.migami.jeg.Reference;
/** Original Splash70-tick area, texture cycle3ticks, slowIV,2.5damage/firetagdouble, charge defusing. */
public final class LandWhirlpool extends Projectile {
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(LandWhirlpool.class,EntityDataSerializers.INT);
    private static final TagKey<EntityType<?>> FIRE=TagKey.create(Registries.ENTITY_TYPE,Reference.id("fire"));
    private int life=70;
    public LandWhirlpool(EntityType<? extends LandWhirlpool> type,Level level) { super(type,level);setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(FRAME,0); }
    public int frame() { return entityData.get(FRAME); }
    @Override public void tick() {
        super.tick();
        if(!(level() instanceof ServerLevel server)) return;
        if(--life<0) { discard();return; }
        if(tickCount%3==0)entityData.set(FRAME,(frame()+1)%6);
        if(tickCount==1)server.playSound(null,blockPosition(),SoundEvents.PLAYER_SPLASH_HIGH_SPEED,SoundSource.PLAYERS,2F,1F);
        if(frame()==3 && tickCount%3==0 && tickCount>20)server.playSound(null,blockPosition(),SoundEvents.PLAYER_SPLASH_HIGH_SPEED,SoundSource.PLAYERS,2F,1F);
        BlockPos center=blockPosition();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-5,-5,-5),center.offset(5,5,5)))if(server.getBlockState(pos).getBlock() instanceof BaseFireBlock)server.removeBlock(pos,false);
        for(Entity entity:server.getEntities(this,getBoundingBox())) {
            if(entity instanceof ExplosiveChargeEntity charge) {
                charge.defuse();server.sendParticles(ParticleTypes.CLOUD,entity.getX(),entity.getEyeY()-.1,entity.getZ(),5,.1,.1,.1,.01);
                server.playSound(null,entity.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,1F,1F);continue;
            }
            if(entity instanceof LivingEntity living && entity!=getOwner()) {
                living.extinguishFire();
                if(frame()==3)living.hurtServer(server,damageSources().generic(),living.getType().builtInRegistryHolder().is(FIRE)?5F:2.5F);
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,3,false,true));
            }
        }
        server.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.5,getZ(),3,1.5,2,1.5,.1);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out);out.putInt("Life",life);out.putInt("Frame",frame()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);life=Math.clamp(in.getIntOr("Life",70),0,70);entityData.set(FRAME,Math.clamp(in.getIntOr("Frame",0),0,5)); }
}
