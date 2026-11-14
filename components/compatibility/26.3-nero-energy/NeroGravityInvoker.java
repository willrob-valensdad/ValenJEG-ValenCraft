package dev.valen.compat.energy.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import za.co.neroland.nerospace.world.gravity.GravityManager;

@Mixin(GravityManager.class)
public interface NeroGravityInvoker {
    @Invoker("applyToLiving")
    static void valen$apply(LivingEntity entity, double factor) { throw new AssertionError("Mixin invoker unavailable"); }
}
