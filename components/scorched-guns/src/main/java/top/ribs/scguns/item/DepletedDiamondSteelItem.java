package top.ribs.scguns.item;

import top.ribs.scguns.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class DepletedDiamondSteelItem extends Item {

    private static final int XP_COST_PER_INGOT = 3;

    public DepletedDiamondSteelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return convertToRegularSteel(level, player, itemStack);
    }

    private InteractionResult convertToRegularSteel(Level level, Player player, ItemStack depletedStack) {
        int stackSize = depletedStack.getCount();
        int playerXP = getTotalExperience(player);
        int maxConvertible = playerXP / XP_COST_PER_INGOT;
        int actualConvertible = Math.min(stackSize, maxConvertible);

        if (actualConvertible <= 0) {
            return InteractionResult.FAIL;
        }

        ItemStack diamondSteelStack = new ItemStack(ModItems.DIAMOND_STEEL_INGOT.get(), actualConvertible);

        boolean addedToInventory = player.getInventory().add(diamondSteelStack);

        if (!addedToInventory) {
            player.drop(diamondSteelStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }

        int xpToRemove = actualConvertible * XP_COST_PER_INGOT;
        removeExperience(player, xpToRemove);
        depletedStack.shrink(actualConvertible);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.5f, 1.0f + (level.getRandom().nextFloat() - 0.5f) * 0.2f);

        return InteractionResult.SUCCESS_SERVER;
    }

    private int getTotalExperience(Player player) {
        int experience = 0;
        int level = player.experienceLevel;

        if (level <= 16) {
            experience = level * level + 6 * level;
        } else if (level <= 31) {
            experience = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            experience = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        experience += Math.round(player.experienceProgress * player.getXpNeededForNextLevel());

        return experience;
    }

    private void removeExperience(Player player, int xpToRemove) {
        int currentXP = getTotalExperience(player);
        int newXP = Math.max(0, currentXP - xpToRemove);

        player.experienceLevel = 0;
        player.experienceProgress = 0.0f;
        player.totalExperience = 0;
        player.giveExperiencePoints(newXP);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.scguns.depleted_diamond_steel_ingot.tooltip.usage"));
        tooltip.accept(Component.translatable("item.scguns.depleted_diamond_steel_ingot.tooltip.cost", XP_COST_PER_INGOT));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
