/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class PlayerEvent extends Event {
    private final Player entity;

    protected PlayerEvent(Player entity) {
        this.entity = entity;
    }

    public Player getEntity() {
        return entity;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player entity) {
            super(entity);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player entity) {
            super(entity);
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {
        public PlayerRespawnEvent(Player entity) { super(entity); }
    }

    public static class ItemCraftedEvent extends PlayerEvent {
        private final ItemStack crafting;
        public ItemCraftedEvent(Player entity, ItemStack crafting) {
            super(entity);
            this.crafting = crafting;
        }
        public ItemStack getCrafting() { return crafting; }
    }
}
