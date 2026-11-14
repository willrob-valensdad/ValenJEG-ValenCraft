package ttv.migami.jeg.legacy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.gun.GunStats;
import ttv.migami.jeg.item.GunItem;
public final class AtlanteanSpearItem extends GunItem {
    public AtlanteanSpearItem(Properties properties,GunStats stats) { super(properties,stats); }
    @Override public boolean tryReload(Level level,net.minecraft.world.entity.player.Player player,ItemStack stack,net.minecraft.world.InteractionHand hand,boolean notify) {
        return !level.isClientSide() && SpearReload.begin(player,stack,hand);
    }
    @Override public boolean tryShoot(Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        if(!level.isClientSide())SpearReload.cancel(player,player.getItemInHand(hand));
        return super.tryShoot(level,player,hand);
    }
    @Override public void fireAt(Level level,LivingEntity shooter,ItemStack stack,LivingEntity target) {
        var direction=target==null ? shooter.getLookAngle() : target.getEyePosition().subtract(shooter.getEyePosition()).normalize();
        fireDirectionallyFrom(level,shooter,stack,shooter.getEyePosition(),direction);
    }
    @Override public void fireDirectionallyFrom(Level level,LivingEntity shooter,ItemStack stack,net.minecraft.world.phys.Vec3 origin,net.minecraft.world.phys.Vec3 direction) {
        if(level.isClientSide()) return;
        WhirlpoolProjectile projectile=new WhirlpoolProjectile(LegacyContent.WHIRLPOOL_ENTITY,level);
        projectile.setLegacyEnchantments(ttv.migami.jeg.enchantment.LegacyGunEnchantments.capture(stack));
        projectile.setOwner(shooter);projectile.setPos(origin.add(direction.scale(.35)));projectile.setDeltaMovement(direction.normalize().scale(3));
        level.addFreshEntity(projectile);
    }
}
