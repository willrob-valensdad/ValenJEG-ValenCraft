package net.neoforged.neoforge.common.extensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import top.ribs.scguns.fabric.MenuExtras;
public interface IMenuTypeExtension {
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        MenuType.MenuSupplier<T> supplier = (containerId, inventory) -> {
            FriendlyByteBuf extra = MenuExtras.consume();
            try {
                return factory.create(containerId, inventory, extra);
            } finally {
                extra.release();
            }
        };
        return new MenuType<>(supplier, FeatureFlags.VANILLA_SET);
    }
}
