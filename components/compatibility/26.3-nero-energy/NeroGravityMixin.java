package dev.valen.compat.energy.mixin;

import net.minecraft.world.entity.LivingEntity;
import dev.valen.compat.energy.GravityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import za.co.neroland.nerospace.world.gravity.GravityManager;

@Mixin(GravityManager.class)
public abstract class NeroGravityMixin {
    @ModifyVariable(method = "applyToLiving", at = @At("HEAD"), argsOnly = true)
    private static double valen$ringAmbient(double factor, LivingEntity entity, double originalFactor) {
        return GravityBridge.adaptAmbient(entity, factor);
    }
}
