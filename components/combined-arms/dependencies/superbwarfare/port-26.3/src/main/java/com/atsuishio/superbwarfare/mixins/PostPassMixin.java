package com.atsuishio.superbwarfare.mixins;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 26.3 uploads post-effect uniforms into a named GPU buffer at pass construction.
 * The handsome-goggles rainbow used to write the RainbowTime uniform every frame
 * through the removed EffectInstance. This writes the same monotonic seconds
 * value into the HandsomeConfig block before the pass is submitted.
 */
@Mixin(PostPass.class)
public class PostPassMixin {

    @Shadow
    @Final
    private Map<String, GpuBuffer> customUniforms;

    @Unique
    private static long superbwarfare$lastMillis = 0;

    @Unique
    private static float superbwarfare$rainbowSeconds = 0;

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void superbwarfare$setRainbowTime(CallbackInfo ci) {
        GpuBuffer buffer = this.customUniforms.get("HandsomeConfig");
        if (buffer == null || buffer.isClosed()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (superbwarfare$lastMillis == 0) {
            superbwarfare$lastMillis = now;
        }
        float delta = (now - superbwarfare$lastMillis) / 1000.0f;
        superbwarfare$lastMillis = now;
        superbwarfare$rainbowSeconds += delta;
        if (superbwarfare$rainbowSeconds > 7200.0f) {
            superbwarfare$rainbowSeconds -= 3600.0f;
        }
        try (var view = buffer.map(false, true)) {
            ByteBuffer data = view.data();
            data.putFloat(0, superbwarfare$rainbowSeconds);
        }
    }
}
