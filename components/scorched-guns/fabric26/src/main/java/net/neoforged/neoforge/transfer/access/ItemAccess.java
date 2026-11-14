package net.neoforged.neoforge.transfer.access;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
/** Mutable view of an item stack the caller already owns. */
public final class ItemAccess {
    private final ItemStack stack;
    private ItemAccess(ItemStack stack) { this.stack = stack; }
    public static ItemAccess forStack(ItemStack stack) { return new ItemAccess(stack); }
    public ItemResource getResource() { return ItemResource.of(stack); }
    public int getAmount() { return stack.getCount(); }
    public EnergyHandler getCapability(Object capability) {
        return Capabilities.lookupItem(stack, Capabilities.Energy.ITEM, this);
    }
    public int exchange(ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack next = resource.toStack(amount);
        CustomData data = next.get(DataComponents.CUSTOM_DATA);
        if (data != null) stack.set(DataComponents.CUSTOM_DATA, data);
        return amount;
    }
}
