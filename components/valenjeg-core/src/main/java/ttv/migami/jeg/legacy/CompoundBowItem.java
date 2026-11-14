package ttv.migami.jeg.legacy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.gun.GunStats;
import ttv.migami.jeg.item.AnimatedGunItem;
/** Original release-fire, 20-tick charge, inventory-fed arrow weapon. */
public final class CompoundBowItem extends AnimatedGunItem {
    private static boolean clientHolding;
    public static void setClientHolding(boolean holding) { clientHolding=holding; }
    @Override public void registerControllers(com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new com.geckolib.animation.AnimationController<>("legacy_bow_charge",0,test->clientHolding ? test.setAndContinue(com.geckolib.animation.RawAnimation.begin().thenPlay("hold_fire").thenLoop("hold")) : com.geckolib.animation.object.PlayState.STOP));
    }
    @Override public boolean canBeEnchantedWith(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment,net.fabricmc.fabric.api.item.v1.EnchantingContext context) {
        if(enchantment.is(net.minecraft.world.item.enchantment.Enchantments.FLAME))return true;
        if(enchantment.is(net.minecraft.world.item.enchantment.Enchantments.POWER) || enchantment.is(net.minecraft.world.item.enchantment.Enchantments.PUNCH) || enchantment.is(net.minecraft.world.item.enchantment.Enchantments.INFINITY))return false;
        return super.canBeEnchantedWith(stack,enchantment,context);
    }
    public CompoundBowItem(net.minecraft.world.item.Item.Properties properties,GunStats stats) { super(properties,stats); }
    @Override public boolean tryShoot(Level level,Player player,InteractionHand hand) { return false; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.BOW; }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) { return 72000; }
    public static float charge(int ticks) { return Math.clamp(ticks/20F,0F,1F); }
    public boolean canStart(Player player,ItemStack stack) {
        return !player.isSpectator() && player.isAlive() && !player.getCooldowns().isOnCooldown(stack) && !isDrawOperationLocked(stack) && !isReloading(stack) && (player.getAbilities().instabuild || player.getInventory().hasAnyMatching(s->s.is(Items.ARROW)));
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || !canStart(player,stack)) return false;
        float charge=charge(getUseDuration(stack,user)-remaining);
        if(charge<=0F) return false;
        int ammo=-1;
        if(!player.getAbilities().instabuild) {
            for(int i=0;i<player.getInventory().getContainerSize();i++) if(player.getInventory().getItem(i).is(Items.ARROW)) { ammo=i; break; }
            if(ammo<0) return false;
        }
        LegacyArrowEntity arrow=new LegacyArrowEntity(server,player,stack,charge);
        if(!server.addFreshEntity(arrow)) return false;
        if(ammo>=0) player.getInventory().getItem(ammo).shrink(1);
        if(!player.getAbilities().instabuild) stack.hurtAndBreak(1,player,player.getUsedItemHand()==InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.getCooldowns().addCooldown(stack,10);
        player.awardStat(Stats.ITEM_USED.get(this));
        getStats().fireSoundEvent().ifPresent(sound->server.playSound(null,player.getX(),player.getY(),player.getZ(),sound,SoundSource.PLAYERS,1F,1F));
        triggerShoot(server,player,stack);
        return true;
    }
}
