package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class FallbackItem extends Item {
    public FallbackItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {

        tooltip.accept(Component.translatable("info.scguns.fallback").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        tooltip.accept(Component.translatable("info.scguns.fallback_help").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("info.scguns.fallback_info").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));

    }
}
