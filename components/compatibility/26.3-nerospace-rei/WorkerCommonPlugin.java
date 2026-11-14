package dev.valen.compat.nerospace;

import java.util.List;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.teajay.workingvillagers.WorkingVillagers;
import me.teajay.workingvillagers.crafting.MillingRecipe;
import me.teajay.workingvillagers.crafting.WoodcuttingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;

/** Loaded only when Working Villagers is installed. */
final class WorkerCommonPlugin {
    static void register(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(MillingRecipe.class).filterType(WorkingVillagers.MILLING.get())
            .fill(h -> display(h, "workingvillagers:milling"));
        registry.beginRecipeFiller(WoodcuttingRecipe.class).filterType(WorkingVillagers.WOODCUTTING.get())
            .fill(h -> display(h, "workingvillagers:woodcutting"));
    }
    private static PackRecipeDisplay display(RecipeHolder<? extends SingleItemRecipe> holder, String category) {
        var recipe = holder.value();
        return new PackRecipeDisplay(category, holder.id().identifier().toString(),
            List.of(EntryIngredients.ofIngredient(recipe.input())),
            List.of(EntryIngredients.of(recipe.assemble((net.minecraft.world.item.crafting.SingleRecipeInput) null))), 0, "");
    }
}
