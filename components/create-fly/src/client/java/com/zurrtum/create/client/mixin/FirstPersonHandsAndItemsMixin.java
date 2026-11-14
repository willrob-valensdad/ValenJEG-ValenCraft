package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.CreateHandsRenderState;
import com.zurrtum.create.client.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void create$extract(LocalPlayer player, float partialTicks,
                               FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        CreateHandsRenderState extra = CreateHandsRenderState.of(state);
        extra.backtankTexture = NetheriteBacktankFirstPersonRenderer.getHandTexture(player);
        Create.ZAPPER_RENDER_HANDLER.extractAnimation(extra.zapper, partialTicks);
        Create.POTATO_CANNON_RENDER_HANDLER.extractAnimation(extra.potatoCannon, partialTicks);
        ExtendoGripRenderHandler.extractRenderState(player, partialTicks, state, extra);
    }
}
