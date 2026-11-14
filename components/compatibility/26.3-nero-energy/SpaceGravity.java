package dev.valen.compat.energy;

import net.minecraft.server.level.ServerPlayer;
import org.alex_melan.spacereloaded.planet.PlanetEffects;

/** Keep optional mod references outside classes loaded by the base pack. */
final class SpaceGravity {
    static boolean refresh(ServerPlayer player) { return PlanetEffects.refreshGravity(player); }
}
