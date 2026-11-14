package net.neoforged.neoforge.event.entity.player;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
public class ItemTooltipEvent {
    private final ItemStack stack;
    private final Item.TooltipContext context;
    private final List<Component> tooltip;
    private final TooltipFlag flags;
    public ItemTooltipEvent(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        this.stack = stack; this.context = context; this.tooltip = tooltip; this.flags = flags;
    }
    public ItemStack getItemStack() { return stack; }
    public Item.TooltipContext getContext() { return context; }
    public List<Component> getToolTip() { return tooltip; }
    public TooltipFlag getFlags() { return flags; }
}
