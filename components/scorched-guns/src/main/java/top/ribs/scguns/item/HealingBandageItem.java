package top.ribs.scguns.item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class HealingBandageItem extends Item {
    int healingAmount;
    List<MobEffectInstance> potionEffects;

    public HealingBandageItem(Item.Properties properties, int healingAmount, MobEffectInstance... potionEffects) {
        super(properties);
        this.potionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
        this.healingAmount = healingAmount;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && !world.isClientSide()) {
            player.heal(healingAmount);
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BRUSH;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.scguns.healing_bandage.heal", healingAmount).withStyle(ChatFormatting.GREEN));
        if (!potionEffects.isEmpty()) {
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null && effect.getEffect() != null) {
                    Component effectName = Component.translatable(effect.getEffect().value().getDescriptionId()).withStyle(ChatFormatting.BLUE);
                    int durationInSeconds = effect.getDuration() / 20;
                    int minutes = durationInSeconds / 60;
                    int seconds = durationInSeconds % 60;
                    String formattedDuration = String.format(" (%02d:%02d)", minutes, seconds);
                    Component effectDuration = Component.literal(formattedDuration).withStyle(ChatFormatting.BLUE);
                    tooltip.accept(Component.empty().append(effectName).append(effectDuration));
                }
            }
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }


}
