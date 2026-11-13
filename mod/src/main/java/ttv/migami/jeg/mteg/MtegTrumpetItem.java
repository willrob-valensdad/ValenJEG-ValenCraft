package ttv.migami.jeg.mteg;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.init.ModItems;

/** Exact original TrumpetItem use path, MTEG5d7fb0f (GPLv3), native26.3 API. */
public final class MtegTrumpetItem extends Item {
    public static final Identifier SOUND_ID=Identifier.fromNamespaceAndPath("mteg","item.trumpet.doot");
    public MtegTrumpetItem(Properties properties) {
        super(properties.stacksTo(1).durability(64).repairable(ModItems.REPAIR_KIT.get()));
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(level instanceof ServerLevel server) {
            var start=player.getEyePosition();
            var end=player.pick(25,0,false).getLocation();
            var difference=end.subtract(start);
            var normal=difference.normalize();
            for(int i=1;i<(int)Math.floor(difference.length())+7;i++) {
                var position=start.add(normal.scale(i));
                server.sendParticles(ParticleTypes.SONIC_BOOM,position.x,position.y,position.z,1,0,0,0,0);
            }
            player.getCooldowns().addCooldown(player.getItemInHand(hand),40);
            var hit=ProjectileUtil.getEntityHitResult(player,start,end,new AABB(start,end),e->e instanceof LivingEntity,start.distanceToSqr(end));
            if(hit!=null && hit.getEntity() instanceof LivingEntity target) {
                target.hurtServer(server,player.damageSources().sonicBoom(player),10F);
                double resistance=1-target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                target.push(normal.x*2.5*resistance,normal.y*.5*resistance,normal.z*2.5*resistance);
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.DEAFENED.get()),100,0,false,false));
                if(target instanceof Warden) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,200,2));
                }
            }
            server.playSound(null,player.blockPosition(),BuiltInRegistries.SOUND_EVENT.getValue(
                    SOUND_ID),SoundSource.PLAYERS,3F,1F);
            // Original deliberately bottoms out at maxDamage-1 and plays ITEM_BREAK thereafter;
            // it checks the main hand even when the trumpet is used from the offhand.
            ItemStack held=player.getMainHandItem();
            if(!player.getAbilities().instabuild && held.isDamageableItem()) {
                if(held.getDamageValue()>=held.getMaxDamage()-1)
                    server.playSound(null,player.blockPosition(),SoundEvents.ITEM_BREAK.value(),SoundSource.PLAYERS,1F,1F);
                else held.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
            }
        }
        return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) {return 72000;}
}
