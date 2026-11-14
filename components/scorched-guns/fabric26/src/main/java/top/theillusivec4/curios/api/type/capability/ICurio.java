package top.theillusivec4.curios.api.type.capability;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
public interface ICurio {
    ItemStack getStack();
    default void curioTick(SlotContext slotContext) {}
}
