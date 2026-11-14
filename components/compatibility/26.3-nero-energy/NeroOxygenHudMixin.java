package dev.valen.compat.energy.mixin;

import dev.valen.compat.energy.OxygenBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.neroland.nerospace.client.OxygenHud;

@Mixin(OxygenHud.class)
public abstract class NeroOxygenHudMixin {
    @Inject(method="render", at=@At("HEAD"), cancellable=true)
    private static void valen$useActualCanisterHud(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (client.gui.screen() != null || player != null && OxygenBridge.hasSpaceMask(player)) ci.cancel();
    }
    @ModifyExpressionValue(method="render", at=@At(value="INVOKE", target="Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean valen$showNativeReserveInSpace(boolean original) {
        var player = Minecraft.getInstance().player;
        return original || player != null && OxygenBridge.showNativeHud(player);
    }
}
