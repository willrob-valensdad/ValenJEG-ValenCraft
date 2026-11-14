package top.ribs.scguns.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import top.ribs.scguns.client.screen.GunBenchRecipe;

import java.util.Optional;

public final class BlueprintRecipeData {
    public static final String ACTIVE_RECIPE_KEY = "ActiveRecipe";

    private BlueprintRecipeData() {
    }

    public static void saveActiveRecipe(ItemStack blueprint, Identifier recipeId) {
        CustomData.update(DataComponents.CUSTOM_DATA, blueprint, tag -> tag.putString(ACTIVE_RECIPE_KEY, recipeId.toString()));
    }

    public static void saveActiveRecipe(ItemStack blueprint, String recipeId) {
        CustomData.update(DataComponents.CUSTOM_DATA, blueprint, tag -> tag.putString(ACTIVE_RECIPE_KEY, recipeId));
    }

    public static void clearActiveRecipe(ItemStack blueprint) {
        CompoundTag tag = blueprint.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ACTIVE_RECIPE_KEY)) {
            return;
        }

        tag.remove(ACTIVE_RECIPE_KEY);
        if (tag.isEmpty()) {
            blueprint.remove(DataComponents.CUSTOM_DATA);
        } else {
            blueprint.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static Identifier getActiveRecipe(ItemStack blueprint) {
        CustomData customData = blueprint.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ACTIVE_RECIPE_KEY)) {
            return null;
        }

        try {
            return Identifier.parse(tag.getStringOr(ACTIVE_RECIPE_KEY, ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getActiveRecipeName(ItemStack blueprint, Level level) {
        Identifier recipeId = getActiveRecipe(blueprint);
        if (recipeId == null || level == null) {
            return null;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
        Optional<RecipeHolder<?>> recipe = serverLevel.recipeAccess().byKey(recipeKey);

        return recipe
                .filter(holder -> holder.value() instanceof GunBenchRecipe)
                .map(holder -> ((GunBenchRecipe) holder.value()).getResultItem(serverLevel.registryAccess()).getDisplayName().getString())
                .orElse(null);
    }
}
