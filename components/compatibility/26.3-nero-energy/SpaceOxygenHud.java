package dev.valen.compat.energy;

import net.minecraft.world.entity.player.Player;
import org.alex_melan.spacereloaded.client.VacuumAmbience;
import org.alex_melan.spacereloaded.registry.ModRegistries;
import za.co.neroland.nerospace.world.OxygenManager;

/** Optional client references, reached only while SpaceReloaded is loaded. */
final class SpaceOxygenHud {
    static boolean usesNativeReserve(Player player) {
        if (OxygenManager.suitTier(player) == OxygenManager.SuitTier.NONE
                && !(player.getVehicle() instanceof za.co.neroland.nerospace.rocket.RocketEntity)) return false;
        if (VacuumAmbience.isExposed()) return true;
        // Planet profiles already synchronize to the client; pressure refills remain visible.
        for (var profile : player.level().registryAccess().lookupOrThrow(ModRegistries.PLANETS)) {
            if (profile.dimension().equals(player.level().dimension().identifier())) return !profile.breathable();
        }
        return false;
    }
}
