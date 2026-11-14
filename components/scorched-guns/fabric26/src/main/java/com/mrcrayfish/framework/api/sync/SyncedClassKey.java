package com.mrcrayfish.framework.api.sync;
import net.minecraft.world.entity.player.Player;
public final class SyncedClassKey<T> {
    public static final SyncedClassKey<Player> PLAYER = new SyncedClassKey<>();
    private SyncedClassKey() {}
}
