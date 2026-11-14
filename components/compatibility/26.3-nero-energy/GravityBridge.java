package dev.valen.compat.energy;

import net.fabricmc.api.ModInitializer;
import dev.valen.compat.energy.mixin.NeroGravityInvoker;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import za.co.neroland.nerospace.world.gravity.GravityManager;

/** Optional SpaceReloaded integration; updates only each mod's own gravity modifier. */
public final class GravityBridge implements ModInitializer {
    private static final boolean SPACE = FabricLoader.getInstance().isModLoaded("spacereloaded");
    @Override public void onInitialize() {
        if (!SPACE) return;
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, oldLevel, newLevel) -> refresh(player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> refresh(newPlayer));
        ServerPlayerEvents.JOIN.register(GravityBridge::refresh);
    }
    public static void refresh(ServerPlayer player) {
        // Invoker still passes through the same ring-aware modifier hook as Nero's periodic update.
        NeroGravityInvoker.valen$apply(player, GravityManager.factorAt(player.level(), player.blockPosition()));
    }
    public static double adaptAmbient(LivingEntity entity, double factor) {
        return SPACE && entity instanceof ServerPlayer player && SpaceGravity.refresh(player) ? 1.0 : factor;
    }
}
