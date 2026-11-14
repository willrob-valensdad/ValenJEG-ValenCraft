package top.ribs.scguns.fabric.mixin;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Item.class)
public class ScorchedItemMixin {
    @Inject(method = "appendHoverText", at = @At("RETURN"))
    private void scguns$tooltip(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag, CallbackInfo callback) {
        NeoForge.EVENT_BUS.post(new ItemTooltipEvent(stack, context, lines, flag));
    }
}
