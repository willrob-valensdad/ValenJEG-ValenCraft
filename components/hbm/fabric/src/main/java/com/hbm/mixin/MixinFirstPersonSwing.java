// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.mixin;

import com.hbm.items.tool.ItemChainsaw;
import com.hbm.items.weapon.ItemCrucible;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class MixinFirstPersonSwing {

    @ModifyVariable(method = "submitArmWithItem", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float hbm$toolFirstPersonSwing(float attack, @Local(argsOnly = true) ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemChainsaw || itemStack.getItem() instanceof ItemCrucible
                ? 0.0F
                : attack;
    }
}
