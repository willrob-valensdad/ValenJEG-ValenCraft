package dev.valen.compat.nerospace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.alex_melan.spacereloaded.SpaceReloaded;
import org.alex_melan.spacereloaded.machine.recipe.*;
import org.alex_melan.spacereloaded.registry.ModDataComponents;
import org.alex_melan.spacereloaded.registry.ModItems;
import org.alex_melan.spacereloaded.registry.ModRecipes;

/** Loaded only when the optional SpaceReloaded mod is present. */
final class SpaceCommonPlugin {
    static void register(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CrusherRecipe.class).filterType(ModRecipes.CRUSHING).fill(h ->
            new PackRecipeDisplay("spacereloaded:crushing", id(h),
                List.of(EntryIngredients.ofIngredient(h.value().input())),
                List.of(EntryIngredients.of(h.value().resultStack())), SpaceReloaded.config().crusherTicks, ""));
        registry.beginRecipeFiller(ElectricFurnaceRecipe.class).filterType(ModRecipes.ELECTRIC_SMELTING).fill(h ->
            new PackRecipeDisplay("spacereloaded:electric_smelting", id(h),
                List.of(EntryIngredients.ofIngredient(h.value().input())),
                List.of(EntryIngredients.of(h.value().resultStack())), SpaceReloaded.config().electricFurnaceTicks, ""));
        registry.beginRecipeFiller(AssemblyRecipe.class).filterType(ModRecipes.ASSEMBLY).fill(h ->
            new PackRecipeDisplay("spacereloaded:assembly", id(h),
                h.value().ingredients().stream().map(EntryIngredients::ofIngredient).toList(),
                List.of(EntryIngredients.of(h.value().resultStack())), 0,
                "Shapeless; engine quality and alloy follow input parts"));
        registry.beginRecipeFiller(MachiningRecipe.Pressing.class).filterType(ModRecipes.PRESSING)
            .fill(h -> machining(h, "spacereloaded:pressing"));
        registry.beginRecipeFiller(MachiningRecipe.Lathe.class).filterType(ModRecipes.MACHINING)
            .fill(h -> machining(h, "spacereloaded:machining"));
        registry.beginRecipeFiller(ChemicalRecipe.class).filterType(ModRecipes.CHEMICAL).fill(h -> {
            var recipe = h.value();
            List<EntryIngredient> inputs = recipe.inputs().stream().map(reagent ->
                EntryIngredients.ofItemStacks(reagent.ingredient().items()
                    .map(item -> new ItemStack(item, reagent.count())).toList())).toList();
            List<EntryIngredient> outputs = recipe.outputs().stream()
                .map(product -> EntryIngredients.of(product.result().create())).toList();
            List<String> lines = new ArrayList<>();
            lines.add(recipe.energy() + " energy; output yields (in slot order):");
            lines.add(recipe.outputs().stream().map(product ->
                String.format(Locale.ROOT, "%.3g%%", product.yield() * 100)).reduce((a, b) -> a + " / " + b).orElse(""));
            if (recipe.outputs().stream().anyMatch(product -> product.yield() < 1))
                lines.add("Fractional yields accumulate across operations");
            if (recipe.oxygen() > 0) lines.add(String.format(Locale.ROOT, "Also produces %.4g kg oxygen", recipe.oxygen()));
            if (recipe.purity() >= 0) lines.add(String.format(Locale.ROOT, "First output purity: %.4g", recipe.purity()));
            if (recipe.keepPurity()) lines.add("First output retains first reagent purity");
            return new PackRecipeDisplay("spacereloaded:chemical/" + recipe.machine(), id(h),
                inputs, outputs, recipe.ticks(), String.join("\n", lines));
        });
    }
    private static String id(RecipeHolder<?> recipe) { return recipe.id().identifier().toString(); }
    private static PackRecipeDisplay machining(RecipeHolder<? extends MachiningRecipe> holder, String category) {
        var recipe = holder.value();
        var data = recipe.data();
        var inputs = data.ingredient().items().map(item -> {
            var stack = new ItemStack(item);
            if (data.step() != 0) stack.set(ModDataComponents.MACHINING_STEP, data.step());
            return stack;
        }).toList();
        var output = recipe.resultStack();
        if (ModItems.isIntermediatePart(output.getItem()))
            output.set(ModDataComponents.MACHINING_STEP, data.step() + 1);
        String detail = "Requires step " + data.step() + "; shaft power required\n"
            + "Quality and scrap depend on speed / accumulated error";
        if (data.energyJ() > 0) detail += String.format(Locale.ROOT, "\nCutting work: %.4g J", data.energyJ());
        if (data.balance()) detail += "\nBalancing reduces accumulated squared error by 4";
        return new PackRecipeDisplay(category, id(holder),
            List.of(EntryIngredients.ofItemStacks(inputs)), List.of(EntryIngredients.of(output)), 0, detail);
    }
}
