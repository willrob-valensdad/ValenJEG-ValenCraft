package top.ribs.scguns.mixin.common.compat.guardvillagers;

import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.config.GunnerMobSpawner;

/**
 * Delays Scorched Guns' generated Guard Villagers loadout check until the end
 * of Guard#aiStep. Guard Villagers creates its vanilla/loot-table equipment in
 * the first aiStep, so running after it avoids fighting its own initialization.
 */
@Pseudo
@Mixin(targets = "tallestegg.guardvillagers.common.entities.Guard", remap = false)
public class GuardGunnerEquipmentMixin {
    @Inject(method = "aiStep", at = @At("TAIL"), remap = false, require = 0)
    private void scguns$checkGeneratedGuardEquipment(CallbackInfo ci) {
        GunnerMobSpawner.checkGuardVillager((PathfinderMob) (Object) this);
    }
}
