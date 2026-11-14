// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VegetationPatchFeature.class)
public interface VegetationPatchFeatureAccess {
    @Accessor("vegetationFeature")
    Holder<PlacedFeature> hbm$vegetation();
}
