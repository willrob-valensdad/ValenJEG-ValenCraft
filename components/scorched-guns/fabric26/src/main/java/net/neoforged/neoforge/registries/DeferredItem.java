package net.neoforged.neoforge.registries;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    public DeferredItem(Supplier<T> supplier) { super(supplier); }
    public ItemStack toStack() { return new ItemStack(get()); }
    public ItemStack toStack(int count) { return new ItemStack(get(), count); }
    public ItemStack stacksTo(int count) { return new ItemStack(get(), count); }
}
