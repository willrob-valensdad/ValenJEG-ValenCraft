package dev.valen.compat.energy.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import za.co.neroland.nerospace.world.OxygenManager;

@Mixin(OxygenManager.class)
public interface NeroOxygenAccess {
    @Invoker("isBreathable") static boolean valen$breathable(ServerLevel level, BlockPos pos) { throw new AssertionError(); }
    @Invoker("hazardDrainMultiplier") static int valen$hazardMultiplier(ServerLevel level, Player player) { throw new AssertionError(); }
    @Invoker("hazardFeedback") static void valen$feedback(ServerLevel level, Player player) { throw new AssertionError(); }
    @Invoker("mirrorToAirSupply") static void valen$mirror(Player player, int oxygen, int max) { throw new AssertionError(); }
}
