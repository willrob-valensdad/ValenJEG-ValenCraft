package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class TooltipAmmo extends AmmoItem {
    private final int armorBypassAmount;
    private final String descriptionKey;


    public TooltipAmmo(Properties properties, int armorBypassAmount) {
        super(properties);
        this.armorBypassAmount = armorBypassAmount;
        this.descriptionKey = null;
    }
    public TooltipAmmo(Properties properties, String descriptionKey) {
        super(properties);
        this.armorBypassAmount = -1;
        this.descriptionKey = descriptionKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (armorBypassAmount >= 0) {
            tooltip.accept(getArmorBypassTooltip());
        }
        if (descriptionKey != null) {
            tooltip.accept(getDescriptionTooltip());
        }
    }

    private Component getArmorBypassTooltip() {
        return Component.translatable("tooltip.scguns.armor_bypass", armorBypassAmount)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    private Component getDescriptionTooltip() {
        assert descriptionKey != null;
        return Component.translatable(descriptionKey)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
