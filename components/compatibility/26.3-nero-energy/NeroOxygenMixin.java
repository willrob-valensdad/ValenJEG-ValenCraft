package dev.valen.compat.energy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.valen.compat.energy.OxygenBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import za.co.neroland.nerospace.world.OxygenManager;

@Mixin(OxygenManager.class)
public abstract class NeroOxygenMixin {
    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    private static void valen$spaceEquipment(ServerPlayer player, CallbackInfo ci) {
        if (OxygenBridge.handle(player)) ci.cancel();
    }
    @ModifyExpressionValue(method="tick", at=@At(value="INVOKE", target="Lza/co/neroland/nerospace/world/OxygenManager;isAirless(Lnet/minecraft/resources/ResourceKey;)Z"))
    private static boolean valen$nativeReserveInSpace(boolean airless, @Local(argsOnly=true) ServerPlayer player) {
        return airless || OxygenBridge.nativeReserveInSpace(player);
    }
    @Inject(method="isBreathable", at=@At("RETURN"), cancellable=true)
    private static void valen$spacePressure(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && OxygenBridge.safeZone(level, pos)) cir.setReturnValue(true);
    }
}
