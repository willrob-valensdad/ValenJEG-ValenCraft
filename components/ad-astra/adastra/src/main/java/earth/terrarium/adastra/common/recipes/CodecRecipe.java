package earth.terrarium.adastra.common.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Resourceful Lib 6 no longer ships CodecRecipe. Machine recipes keep the same
 * codec fields and match behavior through this 26.3 Recipe adapter.
 */
public interface CodecRecipe<C extends RecipeInput> extends Recipe<C> {
    RecipeBookCategory MACHINE_BOOK_CATEGORY = new RecipeBookCategory();

    RecipeSerializer<?> serializer();

    @Override
    default RecipeSerializer<?> getSerializer() {
        return serializer();
    }

    @Override
    default ItemStack assemble(C input) {
        return ItemStack.EMPTY;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return MACHINE_BOOK_CATEGORY;
    }
}
