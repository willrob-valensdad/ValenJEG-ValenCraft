package ttv.migami.jeg.legacy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.entity.TimedThrowableItemProjectile;
import ttv.migami.jeg.item.ThrowableWeaponItem;
/** Non-cookable original charge: ten-second timer starts only on landing. */
public final class ExplosiveChargeItem extends ThrowableWeaponItem {
    public ExplosiveChargeItem(Properties properties) { super(properties,72000); }
    @Override protected boolean canCook() { return false; }
    @Override protected TimedThrowableItemProjectile createProjectile(Level level,LivingEntity owner,int ignored) { var charge=new ExplosiveChargeEntity(LegacyContent.CHARGE_ENTITY,level);charge.setOwner(owner);return charge; }
    @Override protected void onThrown(Level level,LivingEntity owner,TimedThrowableItemProjectile projectile,ItemStack stack) { level.playSound(null,projectile.getX(),projectile.getY(),projectile.getZ(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,1F,1F); }
}
