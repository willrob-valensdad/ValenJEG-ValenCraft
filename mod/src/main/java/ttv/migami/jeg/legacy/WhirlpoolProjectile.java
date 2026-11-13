package ttv.migami.jeg.legacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.TimedThrowableItemProjectile;
import ttv.migami.jeg.init.ModItems;
/** Original WhirpoolEntity: water blast underwater, persistent Splash area on land. */
public final class WhirlpoolProjectile extends LegacyFlightProjectile {
    private int fuse=40;
    private boolean impact;
    public int getFuse() { return fuse; }
    public void setFuse(int fuse) { this.fuse=Math.max(1,fuse); }
    public WhirlpoolProjectile(EntityType<? extends WhirlpoolProjectile> type,Level level) { super(type,level);setFuse(40);setNoGravity(true); }
    @Override protected Item getDefaultItem() { return ModItems.AMMO.get(Reference.id("water_bomb")).get(); }
    @Override public void tick() {
        super.tick();
        if(level().isClientSide())spawnFlightParticles();
        else if(isAlive() && --fuse<=0)explodeNow();
    }
    public void explodeNow() { if(!level().isClientSide() && isAlive()){explode();discard();} }
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) { super.addAdditionalSaveData(out);out.putInt("Fuse",fuse); }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) { super.readAdditionalSaveData(in);fuse=Math.clamp(in.getIntOr("Fuse",40),1,40); }
    @Override protected void onHitEntity(EntityHitResult hit) { super.onHitEntity(hit);impact=true;if(legacyEnchantments().collateral()>0){explode();impact=false;}else explodeNow(); }
    @Override protected void onHitBlock(BlockHitResult hit) { super.onHitBlock(hit);impact=true;explodeNow(); }
    protected void spawnFlightParticles() { if(tickCount>2)level().addParticle(isUnderWater()?ParticleTypes.BUBBLE:ParticleTypes.SPLASH,getX(),getY(),getZ(),0,0,0); }
    protected void explode() {
        if(isUnderWater()) { level().explode(this,getX(),getY(),getZ(),2.5F,Level.ExplosionInteraction.TNT);return; }
        // Original expiry creates no land area; only impact does.
        if(!impact) return;
        LandWhirlpool area=new LandWhirlpool(LegacyContent.SPLASH_ENTITY,level());
        area.setOwner(getOwner());area.setPos(net.minecraft.world.phys.Vec3.atCenterOf(blockPosition()).add(0,-.5,0));level().addFreshEntity(area);
    }
}
