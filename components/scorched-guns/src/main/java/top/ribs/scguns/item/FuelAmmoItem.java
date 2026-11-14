package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 *
 * Author: MrCrayfish
 */
public class FuelAmmoItem extends Item implements IAmmo, StackCraftingRemainder {
    private final int burnTime;
    private final List<MobEffectInstance> potionEffects;
    private final Supplier<Item> containerItem;

    public FuelAmmoItem(Properties properties, int burnTime, Supplier<Item> containerItem, MobEffectInstance... potionEffects) {
        properties = properties.component(net.minecraft.core.component.DataComponents.COOKING_FUEL, new net.minecraft.world.item.component.CookingFuel(new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(burnTime), new net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant(1.0F)));
        super(properties);
        this.burnTime = burnTime;
        this.containerItem = containerItem;
        this.potionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
    }

    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return new ItemStackTemplate(this.containerItem.get());
    }


    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && !world.isClientSide()) {
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }

            if (!player.getAbilities().instabuild) {
                ItemStack containerStack = new ItemStack(containerItem.get());
                if (stack.getCount() > 1) {
                    stack.shrink(1);
                    if (!player.getInventory().add(containerStack)) {
                        player.drop(containerStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                    }
                    return stack;
                } else {
                    return containerStack;
                }
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
        return ItemUseAnimation.DRINK;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!potionEffects.isEmpty()) {
            tooltip.accept(Component.translatable("item.tooltip.fuel_effects").withStyle(ChatFormatting.GRAY));
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
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
