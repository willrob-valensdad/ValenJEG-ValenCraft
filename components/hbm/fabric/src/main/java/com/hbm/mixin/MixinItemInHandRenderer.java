// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.mixin;

import com.hbm.items.weapon.ItemCrucible;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class MixinItemInHandRenderer {

    @Shadow private ItemStack mainHandItem;
    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;

    @Inject(method = "tick", at = @At("TAIL"))
    private void hbm$crucibleEquipProgress(LocalPlayer player, CallbackInfo ci) {
        if (!(mainHandItem.getItem() instanceof ItemCrucible)) return;
        if (mainHandHeight < oMainHandHeight) mainHandHeight = oMainHandHeight = 0;
        else if (mainHandHeight > oMainHandHeight) mainHandHeight = oMainHandHeight = 1;
    }
}
