/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.client.event;

import net.minecraft.world.entity.player.Player;

public class ComputeFovModifierEvent {
    private final Player player;
    private float newFovModifier;

    public ComputeFovModifierEvent(Player player, float newFovModifier) {
        this.player = player;
        this.newFovModifier = newFovModifier;
    }

    public Player getPlayer() {
        return player;
    }

    public float getNewFovModifier() {
        return newFovModifier;
    }

    public void setNewFovModifier(float newFovModifier) {
        this.newFovModifier = newFovModifier;
    }
}
