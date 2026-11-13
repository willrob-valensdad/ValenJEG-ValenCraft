package ttv.migami.jeg.legacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import ttv.migami.jeg.gun.GunHeadshotHelper;
/** ArrowProjectileEntity port: original 8 speed, 1000 life, charged damage, 2.5 headshots and flame. */
public final class LegacyArrowEntity extends LegacyFlightProjectile {
    private float charge=1F;
    private boolean flaming;
    private int life=1000;
    public LegacyArrowEntity(EntityType<? extends LegacyArrowEntity> type,Level level) { super(type,level); }
    public LegacyArrowEntity(Level level,LivingEntity owner,ItemStack weapon,float charge) {
        this(LegacyContent.ARROW_ENTITY,level); setOwner(owner); this.charge=charge;setLegacyEnchantments(ttv.migami.jeg.enchantment.LegacyGunEnchantments.capture(weapon));
        var enchant=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FLAME);
        flaming=EnchantmentHelper.getItemEnchantmentLevel(enchant,weapon)>0;
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(.35)));
        setDeltaMovement(owner.getLookAngle().scale(8));
    }
    @Override protected Item getDefaultItem() { return Items.ARROW; }
    @Override protected double originalGravity() { return -.04; }
    @Override public void tick() {
        super.tick();
        if(level() instanceof ServerLevel server) {
            if(--life<=0) { discard(); return; }
            if(charge==1F) server.sendParticles(ParticleTypes.CRIT,getX(),getY(),getZ(),1,0,0,0,0);
            if(flaming) server.sendParticles(ParticleTypes.LAVA,getX(),getY(),getZ(),1,0,0,0,0);
        }
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if(level() instanceof ServerLevel server) {
            var critical=ttv.migami.jeg.enchantment.LegacyGunEnchantments.roll(legacyEnchantments(),random,10F*charge);
            float damage=critical.amount();
            if(hit.getEntity() instanceof LivingEntity living && GunHeadshotHelper.isHeadshotHit(hit,living)) damage*=2.5F;
            hit.getEntity().hurtServer(server,damageSources().thrown(this,getOwner()),damage);
            if(flaming) hit.getEntity().igniteForSeconds(5);
            if(critical.critical()&&getOwner() instanceof net.minecraft.server.level.ServerPlayer player)ttv.migami.jeg.network.NetworkHandler.sendHitMarker(player,true);
            if(legacyEnchantments().collateral()==0)discard();
        }
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if(level() instanceof ServerLevel server) {
            var pos=hit.getBlockPos().relative(hit.getDirection());
            if(flaming && server.getBlockState(pos).isAir() && BaseFireBlock.canBePlacedAt(server,pos,hit.getDirection())) server.setBlock(pos,BaseFireBlock.getState(server,pos),11);
            discard();
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putFloat("Charge",charge);out.putBoolean("Flaming",flaming);out.putInt("Life",life); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);charge=Math.clamp(in.getFloatOr("Charge",1F),0F,1F);flaming=in.getBooleanOr("Flaming",false);life=Math.clamp(in.getIntOr("Life",1000),1,1000); }
}
