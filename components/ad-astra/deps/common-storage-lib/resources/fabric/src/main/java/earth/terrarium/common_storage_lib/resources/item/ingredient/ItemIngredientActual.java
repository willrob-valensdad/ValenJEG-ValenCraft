package earth.terrarium.common_storage_lib.resources.item.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.common_storage_lib.resources.util.CodecUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.msrandom.multiplatform.annotations.Actual;

import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class ItemIngredientActual {
    @Actual
    public static Ingredient all(Ingredient... ingredients) {
        return DefaultCustomIngredients.all(ingredients);
    }

    @Actual
    public static Ingredient any(Ingredient... ingredients) {
        return DefaultCustomIngredients.any(ingredients);
    }

    @Actual
    public static Ingredient difference(Ingredient base, Ingredient subtracted) {
        return DefaultCustomIngredients.difference(base, subtracted);
    }

    @Actual
    public static Ingredient components(Ingredient base, DataComponentExactPredicate components) {
        return DefaultCustomIngredients.components(base, components.asPatch());
    }

    @Actual
    public static Ingredient components(ItemStack stack) {
        return DefaultCustomIngredients.components(stack);
    }

    @Actual
    private static MapCodec<Ingredient> getNonEmptyMapCodec() {
        // 26.3 Ingredient.CODEC already accepts vanilla values and Fabric custom ingredients.
        return MapCodec.assumeMapUnsafe(Ingredient.CODEC);
    }
}
