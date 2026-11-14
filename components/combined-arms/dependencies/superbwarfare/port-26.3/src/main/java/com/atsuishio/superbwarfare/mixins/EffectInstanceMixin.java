package com.atsuishio.superbwarfare.mixins;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceMixin {

    @Unique
    private static final String SUPERBWARFARE$PROGRAM_PREFIX = "shaders/program/";

    @Redirect(
            method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
            )
    )
    private Identifier superbwarfare$resolveEffectDefinition(String path) {
        return superbwarfare$resolveNamespacedProgram(path);
    }

    @Redirect(
            method = "getOrCreate(Lnet/minecraft/server/packs/resources/ResourceProvider;Lcom/mojang/blaze3d/shaders/Program$Type;Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/EffectProgram;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
            )
    )
    private static Identifier superbwarfare$resolveEffectProgram(String path) {
        return superbwarfare$resolveNamespacedProgram(path);
    }

    @Unique
    private static Identifier superbwarfare$resolveNamespacedProgram(String path) {
        Identifier location = Identifier.parse(path.substring(SUPERBWARFARE$PROGRAM_PREFIX.length()));
        return Identifier.fromNamespaceAndPath(
                location.getNamespace(),
                SUPERBWARFARE$PROGRAM_PREFIX + location.getPath()
        );
    }
}