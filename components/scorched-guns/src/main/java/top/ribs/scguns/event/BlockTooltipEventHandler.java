package top.ribs.scguns.event;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.interfaces.ISCGunsBlockTooltip;

/**
 * Routes BlockItem tooltip generation to Scorched Guns blocks that used the
 * pre-26.2 Block#appendHoverText hook.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class BlockTooltipEventHandler {
    private BlockTooltipEventHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ISCGunsBlockTooltip tooltipBlock) {
            tooltipBlock.appendScgunsTooltip(
                    stack,
                    event.getContext(),
                    event.getToolTip(),
                    event.getFlags()
            );
        }
    }
}
