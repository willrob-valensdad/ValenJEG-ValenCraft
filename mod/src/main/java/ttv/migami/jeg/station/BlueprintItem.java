package ttv.migami.jeg.station;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import ttv.migami.jeg.init.ModDataComponents;
public final class BlueprintItem extends Item {
    public BlueprintItem(Properties p) {super(p);}
    public void appendHoverText(ItemStack stack,Item.TooltipContext context,TooltipDisplay display,Consumer<Component> tooltip,TooltipFlag flag) {
        String target=stack.get(ModDataComponents.BLUEPRINT_GUN.get());
        if(target!=null && Identifier.tryParse(target)!=null) {Identifier id=Identifier.parse(target);tooltip.accept(Component.translatable("info.jeg.blueprinted_gun").append(" ").append(Component.translatable("item."+id.getNamespace()+"."+id.getPath())).withStyle(ChatFormatting.GOLD));}
        else tooltip.accept(Component.translatable("info.jeg.tooltip_item.firearm_blueprint_2").withStyle(ChatFormatting.GRAY));
    }
}
