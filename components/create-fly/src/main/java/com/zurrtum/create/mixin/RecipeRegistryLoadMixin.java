package com.zurrtum.create.mixin;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryLoadTask.PendingRegistration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

@Mixin(RegistryLoadTask.class)
public class RecipeRegistryLoadMixin<T> {
    @Shadow @Final private WritableRegistry<T> registry;

    @ModifyVariable(method = "registerElements(Ljava/util/stream/Stream;)V", at = @At("HEAD"), argsOnly = true)
    @SuppressWarnings("unchecked")
    private Stream<PendingRegistration<T>> create$appendAssemblySteps(Stream<PendingRegistration<T>> entries) {
        if (!((Object) this instanceof ResourceManagerRegistryLoadTask<?>)
            || !registry.key().equals(Registries.RECIPE)) return entries;
        // Values in the native registry are not bound until freeze. Use the already decoded
        // registrations and let the existing loader perform all registration and locking.
        var decoded = entries.toList();
        var keys = new HashSet<ResourceKey<T>>();
        decoded.forEach(entry -> keys.add(entry.key()));
        var steps = new ArrayList<PendingRegistration<T>>();
        for (var entry : decoded) {
            if (!(entry.value().left().orElse(null) instanceof SequencedAssemblyRecipe assembly)) continue;
            for (int i = 0; i < assembly.sequence().size(); i++) {
                Identifier parent = entry.key().identifier();
                Identifier id = Identifier.fromNamespaceAndPath("create", "sequenced_assembly/"
                    + parent.getNamespace() + "/" + parent.getPath() + "/" + i);
                ResourceKey<T> key = (ResourceKey<T>) (ResourceKey<?>) ResourceKey.create(Registries.RECIPE, id);
                if (keys.add(key)) {
                    steps.add(new PendingRegistration<>(key, Either.left((T) assembly.sequence().get(i)), entry.registrationInfo()));
                }
            }
        }
        return Stream.concat(decoded.stream(), steps.stream());
    }
}
