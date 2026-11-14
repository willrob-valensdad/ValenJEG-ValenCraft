package top.ribs.scguns.interfaces;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Compatibility bridge for block-item tooltips after Block#appendHoverText
 * was removed from the 26.2 block API.
 */
public interface ISCGunsBlockTooltip {
    void appendScgunsTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    );
}
