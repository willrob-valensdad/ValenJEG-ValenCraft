/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.world.item.Item;

public class RegisterClientExtensionsEvent {
    public void registerItem(IClientItemExtensions extensions, Item... items) {
        if (items == null) {
            return;
        }
        for (Item item : items) {
            IClientItemExtensions.register(item, extensions);
        }
    }
}
