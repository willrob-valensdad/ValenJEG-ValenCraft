/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.network.handling;

import net.minecraft.world.entity.player.Player;

public interface IPayloadContext {
    default void enqueueWork(Runnable runnable) {
        runnable.run();
    }

    Player player();
}
