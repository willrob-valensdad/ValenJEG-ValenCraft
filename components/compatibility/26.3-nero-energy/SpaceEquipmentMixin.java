package dev.valen.compat.energy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import za.co.neroland.nerospace.world.OxygenManager;

@Pseudo
@Mixin(targets="org.alex_melan.spacereloaded.sealing.VacuumHazard", remap=false)
public abstract class SpaceEquipmentMixin {
    @WrapOperation(method="tick", at=@At(value="INVOKE", target="Lorg/alex_melan/spacereloaded/sealing/VacuumHazard;applyEquipmentHazard(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private static void valen$nativeNeroEquipment(ServerLevel level, LivingEntity entity, Operation<Void> original) {
        if (entity instanceof ServerPlayer player && (OxygenManager.suitTier(player) != OxygenManager.SuitTier.NONE
                || player.getVehicle() instanceof za.co.neroland.nerospace.rocket.RocketEntity
                || OxygenManager.isAirless(level.dimension()))) return;
        original.call(level, entity);
    }
    @WrapOperation(method="applyEquipmentHazard", at=@At(value="INVOKE", target="Lorg/alex_melan/spacereloaded/sealing/VacuumHazard;consumeOxygen(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean valen$temperatureCost(Player player, Operation<Boolean> original) {
        // Generic Space equipment is not Nero's specialized heat/cold set.
        int cost = OxygenManager.isAirless(player.level().dimension())
                ? NeroOxygenAccess.valen$hazardMultiplier((ServerLevel) player.level(), player) : 1;
        for (int i=0; i<cost; i++) if (!original.call(player)) return false;
        return true;
    }
}
