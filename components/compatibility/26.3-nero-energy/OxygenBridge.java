package dev.valen.compat.energy;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Optional dispatch at existing hazard callbacks; adds no ticking or retained player state. */
public final class OxygenBridge {
    private static final boolean SPACE = FabricLoader.getInstance().isModLoaded("spacereloaded");
    public static boolean handle(ServerPlayer player) { return SPACE && SpaceOxygen.handle(player); }
    public static boolean nativeReserveInSpace(ServerPlayer player) { return SPACE && SpaceOxygen.nativeReserveInSpace(player); }
    public static boolean safeZone(ServerLevel level, BlockPos pos) { return SPACE && SpaceOxygen.safeZone(level, pos); }
    public static boolean showNativeHud(Player player) { return SPACE && SpaceOxygenHud.usesNativeReserve(player); }
    public static boolean hasSpaceMask(Player player) { return SPACE && SpaceOxygen.hasMask(player); }
}
