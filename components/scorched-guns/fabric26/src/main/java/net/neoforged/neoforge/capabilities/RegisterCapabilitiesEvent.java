package net.neoforged.neoforge.capabilities;
import java.util.function.BiFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
public class RegisterCapabilitiesEvent extends Event {
    public <T, C, B extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability, BlockEntityType<B> type, BiFunction<B, C, T> provider) {
        Capabilities.registerBlock(capability, type, provider);
    }
    public <T, C> void registerItem(ItemCapability<T, C> capability, BiFunction<ItemStack, C, T> provider, Item... items) {
        Capabilities.registerItem(capability, provider, items);
    }
}
