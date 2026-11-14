package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeManager.IngredientExtractor;
import net.minecraft.world.item.crafting.RecipePropertySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow @Final @Mutable private RecipeMap recipes;

    @Inject(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At("HEAD"))
    private void create$derivePotionRecipes(CallbackInfo ci) {
        // Native item components and tags are bound now. Derive brewing automation once per reload,
        // before the existing manager builds its property sets and displays.
        Map<Identifier, Recipe<?>> expanded = new TreeMap<>();
        recipes.values().forEach(holder -> expanded.put(holder.id().identifier(), holder.value()));
        PotionRecipe.register(expanded);
        // Use the native factory: Fabric attaches its serializer-sync cache at create() RETURN.
        // This temporary lookup is not published as a world registry; recipe IDs and values are unchanged.
        MappedRegistry<Recipe<?>> lookup = new MappedRegistry<>(Registries.RECIPE, Lifecycle.stable());
        expanded.forEach((id, recipe) ->
            lookup.register(ResourceKey.create(Registries.RECIPE, id), recipe, RegistrationInfo.BUILT_IN));
        recipes = RecipeMap.create(lookup.freeze());
    }

    @WrapOperation(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    public Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> registerRecipeSet(
        Set<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> instance,
        Operation<Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>>> original
    ) {
        return Stream.concat(original.call(instance), AllRecipeSets.ALL.entrySet().stream());
    }
}
