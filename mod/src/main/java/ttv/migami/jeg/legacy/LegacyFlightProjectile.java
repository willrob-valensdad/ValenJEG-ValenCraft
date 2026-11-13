package ttv.migami.jeg.legacy;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
/** Original ProjectileEntity flight: move unmodified velocity, then gravity; no air/water drag.
 * Collision/ownership stay native26.3, without inheriting snowball inertia or gravity ordering. */
public abstract class LegacyFlightProjectile extends Projectile implements ItemSupplier {
    private ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot legacyEnchantments=ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot.NONE;
    private boolean legacyTrailSent;
    public void setLegacyEnchantments(ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot snapshot){legacyEnchantments=snapshot;}
    public ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot legacyEnchantments(){return legacyEnchantments;}
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);ttv.migami.jeg.enchantment.LegacyGunEnchantments.write(legacyEnchantments,out);}
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);legacyEnchantments=ttv.migami.jeg.enchantment.LegacyGunEnchantments.read(in);}
    @Override protected void onHitEntity(net.minecraft.world.phys.EntityHitResult hit){ttv.migami.jeg.enchantment.LegacyGunEnchantments.igniteEntity(legacyEnchantments,hit.getEntity());super.onHitEntity(hit);}
    @Override protected void onHitBlock(net.minecraft.world.phys.BlockHitResult hit){if(level() instanceof net.minecraft.server.level.ServerLevel server)ttv.migami.jeg.enchantment.LegacyGunEnchantments.igniteBlock(legacyEnchantments,server,hit,random);super.onHitBlock(hit);}
    protected LegacyFlightProjectile(EntityType<? extends LegacyFlightProjectile> type,Level level) { super(type,level); }
    protected abstract Item getDefaultItem();
    protected double originalGravity() { return 0; }
    @Override public ItemStack getItem() { return new ItemStack(getDefaultItem()); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    private HitResult originalHit(net.minecraft.world.phys.Vec3 motion){return originalHit(motion,java.util.Set.of());}
    private HitResult originalHit(net.minecraft.world.phys.Vec3 motion,java.util.Set<Integer> ignored) {
        var start=position();var end=start.add(motion);
        var context=new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this);
        net.minecraft.world.phys.BlockHitResult block=net.minecraft.world.level.BlockGetter.traverseBlocks(start,end,context,(ray,pos)->{
            var state=level().getBlockState(pos);
            // Original gameplay.ignoreLeaves default=true, preserved for these restored projectiles.
            if(state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)return null;
            return level().clipWithInteractionOverride(ray.getFrom(),ray.getTo(),pos,ray.getBlockShape(state,level(),pos),state);
        },ray->net.minecraft.world.phys.BlockHitResult.miss(end,net.minecraft.core.Direction.getApproximateNearest(start.subtract(end)),net.minecraft.core.BlockPos.containing(end)));
        var clipped=block.getType()==HitResult.Type.MISS ? end : block.getLocation();
        var entity=ProjectileUtil.getEntityHitResult(level(),this,start,clipped,getBoundingBox().expandTowards(motion).inflate(1),candidate->!ignored.contains(candidate.getId())&&canHitEntity(candidate));
        return entity==null ? block : entity;
    }
    @Override public void tick() {
        super.tick();
        var motion=getDeltaMovement();
        if(!level().isClientSide()&&!legacyTrailSent){
            ttv.migami.jeg.enchantment.LegacyEnchantmentTrailPayload.send(this,legacyEnchantments,this instanceof LegacyArrowEntity?1000:40,originalGravity(),true);legacyTrailSent=true;
        }
        if(!level().isClientSide()&&legacyEnchantments.collateral()>0){
            var start=position();java.util.Set<Integer> hitIds=new java.util.HashSet<>();
            while(isAlive()){
                setPos(start);HitResult next=originalHit(motion,hitIds);
                setPos(next.getType()==HitResult.Type.MISS?start.add(motion):next.getLocation());
                if(next.getType()==HitResult.Type.MISS)break;
                if(next instanceof net.minecraft.world.phys.EntityHitResult entity)hitIds.add(entity.getEntity().getId());
                hitTargetOrDeflectSelf(next);
                if(!(next instanceof net.minecraft.world.phys.EntityHitResult))break;
            }
            if(isAlive()){setPos(start.add(motion));updateRotation();setDeltaMovement(motion.add(0,originalGravity(),0));}
            return;
        }
        HitResult hit=originalHit(motion);
        setPos(hit.getType()==HitResult.Type.MISS ? position().add(motion) : hit.getLocation());
        updateRotation();
        if(hit.getType()!=HitResult.Type.MISS && isAlive())hitTargetOrDeflectSelf(hit);
        if(isAlive())setDeltaMovement(motion.add(0,originalGravity(),0));
    }
}
