package ttv.migami.jeg.legacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.TimedThrowableItemProjectile;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
public final class ExplosiveChargeEntity extends TimedThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> LANDED=SynchedEntityData.defineId(ExplosiveChargeEntity.class,EntityDataSerializers.BOOLEAN);
    private int age;
    public ExplosiveChargeEntity(EntityType<? extends ExplosiveChargeEntity> type,Level level) { super(type,level);setFuse(200); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(LANDED,false); }
    @Override protected Item getDefaultItem() { return LegacyContent.EXPLOSIVE_CHARGE; }
    @Override protected boolean usesStandardFuse() { return false; }
    @Override protected boolean shouldBounce() { return false; }
    public boolean landed() { return entityData.get(LANDED); }
    private void land() { entityData.set(LANDED,true);setNoGravity(true);setDeltaMovement(Vec3.ZERO); }
    @Override protected void onHitBlock(BlockHitResult hit) { super.onHitBlock(hit);land(); }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if(hit.getEntity()==getOwner()) return;
        if(level() instanceof ServerLevel server) { hit.getEntity().hurtServer(server,damageSources().thrown(this,getOwner()),1);if(!(hit.getEntity() instanceof Player)) startRiding(hit.getEntity());land(); }
    }
    private boolean hasSupport() {
        for(net.minecraft.core.Direction direction:net.minecraft.core.Direction.values())if(!level().getBlockState(blockPosition().relative(direction)).canBeReplaced())return true;
        return false;
    }
    @Override protected double getThrownGravity() { return landed() ? .03D : .05D; }
    @Override public void tick() {
        if(landed()) {
            boolean supported=isPassenger() || hasSupport();setNoGravity(supported);
            if(supported)setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        if(level() instanceof ServerLevel server) {
            if(++age>=72000) { discard();return; }
            if(landed()) {
                setFuse(getFuse()-1);
                if(getFuse()%20==0) { var sound=ModSounds.ALL.get(Reference.id("item.c4.beep"));if(sound!=null) server.playSound(null,blockPosition(),sound.get(),SoundSource.HOSTILE,.6F,1F); }
                if(getFuse()<=0) explodeNow();
            }
        }
    }
    public void defuse() { if(!level().isClientSide() && isAlive()) { var stack=new net.minecraft.world.item.ItemStack(LegacyContent.EXPLOSIVE_CHARGE);var item=new net.minecraft.world.entity.item.ItemEntity(level(),getX(),getY(),getZ(),stack);if(level().addFreshEntity(item))discard(); } }
    @Override protected void explode() {
        ServerLevel server=(ServerLevel)level();
        server.sendParticles(ModParticleTypes.BIG_EXPLOSION.get(),getX(),getY()+2,getZ(),1,0,0,0,0);
        server.sendParticles(ModParticleTypes.SMOKE.get(),getX(),getY()+2,getZ(),10,1,1,1,.1);
        server.sendParticles(ModParticleTypes.FIRE.get(),getX(),getY()+2,getZ(),10,1,1,1,.1);
        server.explode(this,getX(),getY(),getZ(),2.5F,Level.ExplosionInteraction.TNT);
    }
    @Override protected void spawnFlightParticles() { if(tickCount>20 && !landed())level().addParticle(ParticleTypes.LAVA,getX(),getY(),getZ(),0,0,0); }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out);out.putBoolean("Landed",landed());out.putInt("Age",age); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);entityData.set(LANDED,in.getBooleanOr("Landed",false));age=Math.clamp(in.getIntOr("Age",0),0,72000);if(landed())land(); }
}
