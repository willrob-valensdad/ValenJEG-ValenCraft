package ttv.migami.jeg.legacy;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AmmoBoxMenu extends AbstractContainerMenu {
    private final Container container;
    public AmmoBoxMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(AmmoBoxBlockEntity.SIZE)); }
    public AmmoBoxMenu(int id, Inventory inventory, Container container) {
        super(LegacyContent.AMMO_BOX_MENU, id);
        checkContainerSize(container, AmmoBoxBlockEntity.SIZE);
        this.container = container;
        container.startOpen(inventory.player);
        for (int row=0;row<3;row++) for (int col=0;col<5;col++) addSlot(new Slot(container, col+row*5, 43+col*18, 18+row*18) { @Override public boolean mayPlace(ItemStack stack) { return AmmoBoxBlockEntity.isAmmo(stack); } });
        for (int row=0;row<3;row++) for (int col=0;col<9;col++) addSlot(new Slot(inventory,col+row*9+9,8+col*18,91+row*18));
        for (int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,149));
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), copy=stack.copy();
        int size=AmmoBoxBlockEntity.SIZE;
        if (index<size ? !moveItemStackTo(stack,size,slots.size(),true) : !moveItemStackTo(stack,0,size,false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
