package net.neoforged.neoforge.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
    T create(int containerId, Inventory inventory, FriendlyByteBuf extra);
}
