package top.ribs.scguns.compat;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * Minecraft 26.2 no longer permits constructing an empty vanilla Ingredient.
 * Scorched Guns 1.20.1 used empty ingredients as optional-slot sentinels, so the
 * port uses a valid non-air ingredient that is never exposed as a real recipe
 * requirement and recognizes it explicitly wherever optional slots are handled.
 */
public final class RecipeIngredientCompat {
    public static final Ingredient EMPTY_SLOT = Ingredient.of(Items.BARRIER);

    private RecipeIngredientCompat() {}

    /**
     * Content-based check rather than Ingredient.equals(). This is important
     * because network/codec decoding creates a new Ingredient instance.
     */
    public static boolean isEmpty(Ingredient ingredient) {
        List<Holder<Item>> items = ingredient.items().toList();
        return items.size() == 1 && items.get(0).value() == Items.BARRIER;
    }
}
