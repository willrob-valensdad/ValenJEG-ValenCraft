package dev.valen.compat.energy;

import dev.valen.compat.energy.mixin.NeroOxygenAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.alex_melan.spacereloaded.SpaceReloaded;
import org.alex_melan.spacereloaded.registry.ModItems;
import org.alex_melan.spacereloaded.rocket.RocketEntity;
import org.alex_melan.spacereloaded.sealing.VacuumHazard;
import org.alex_melan.spacereloaded.sealing.ZoneManager;
import za.co.neroland.nerospace.platform.Services;
import za.co.neroland.nerospace.world.OxygenManager;

/** Optional references stay outside classes loaded by the pack without SpaceReloaded. */
final class SpaceOxygen {
    static boolean hasMask(Player player) { return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.OXYGEN_MASK); }
    static boolean nativeReserveInSpace(ServerPlayer player) {
        return (OxygenManager.suitTier(player) != OxygenManager.SuitTier.NONE
                || player.getVehicle() instanceof za.co.neroland.nerospace.rocket.RocketEntity)
                && ZoneManager.isVacuumWorld(player.level());
    }
    static boolean safeZone(ServerLevel level, BlockPos pos) { return VacuumHazard.isInsidePressurizedZone(level, pos); }
    static boolean handle(ServerPlayer player) {
        if (player.getVehicle() instanceof RocketEntity rocket && rocket.hasPressurizedCabin()) {
            int max = OxygenManager.maxOxygenFor(player);
            Services.PLATFORM.setOxygen(player, max);
            NeroOxygenAccess.valen$mirror(player, max, max);
            return true;
        }
        if (!hasMask(player) || !(OxygenManager.isAirless(player.level().dimension())
                || player.getVehicle() instanceof za.co.neroland.nerospace.rocket.RocketEntity && ZoneManager.isVacuumWorld(player.level()))) return false;
        if (player.isCreative() || player.isSpectator()) return true;
        // Keep Nero's actual finite cabin consumption rather than replacing it with a free exemption.
        if (player.getVehicle() instanceof za.co.neroland.nerospace.rocket.RocketEntity rocket && rocket.getOxygen() > 0) return false;
        var level = player.level();
        if (level.getGameTime() % SpaceReloaded.config().vacuumCheckIntervalTicks != 0) return true;
        if (!NeroOxygenAccess.valen$breathable(level, player.blockPosition())) {
            VacuumHazard.applyEquipmentHazard(level, player);
            NeroOxygenAccess.valen$feedback(level, player);
        }
        return true;
    }
}
