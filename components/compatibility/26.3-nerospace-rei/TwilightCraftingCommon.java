package dev.valen.compat.nerospace;

import java.util.*;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.tags.TFItemTags;
import twilightforest.item.recipe.CasketRepairRecipe;
import twilightforest.item.recipe.ComponentAttachingSmithingRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.recipe.travellers.TravellersVestGlovesMergeRecipe;

/** Native crafting layouts and assemble results for the author's special JEI extensions. */
final class TwilightCraftingCommon {
    static void register(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CasketRepairRecipe.class).fill(h -> {
            var damaged = new ItemStack(TFItems.KEEPSAKE_CASKET);
            damaged.set(TFDataComponents.CASKET_DAMAGE,2);
            var repairs = EntryIngredients.ofItemTag(TFItemTags.KEEPSAKE_CASKET_REPAIR_ITEMS);
            var result = h.value().assemble(CraftingInput.of(1,1,List.of(damaged)));
            return new DefaultCustomShapelessDisplay(List.of(EntryIngredients.of(damaged),repairs),
                List.of(EntryIngredients.of(result)),Optional.of(h.id().identifier()));
        });
        registry.beginRecipeFiller(TravellersVestGlovesMergeRecipe.class).fill(h -> {
            var vest = new ItemStack(TFItems.TRAVELLERS_VEST);
            var gloves = new ItemStack(TFItems.TRAVELLERS_GLOVES);
            return new DefaultCustomShapelessDisplay(List.of(EntryIngredients.of(vest),EntryIngredients.of(gloves)),
                List.of(EntryIngredients.of(h.value().assemble(CraftingInput.of(2,1,List.of(vest,gloves))))),
                Optional.of(h.id().identifier()));
        });
        registry.beginRecipeFiller(ComponentAttachingSmithingRecipe.class).fill(h -> {
            var recipe = h.value();
            var context = EntryIngredients.slotDisplayContext();
            var template = recipe.templateIngredient().map(i -> i.display().resolveForFirstStack(context)).orElse(ItemStack.EMPTY);
            var addition = recipe.additionIngredient().map(i -> i.display().resolveForFirstStack(context)).orElse(ItemStack.EMPTY);
            var outputs = recipe.baseIngredient().display().resolveForStacks(context).stream()
                .map(base -> recipe.assemble(new SmithingRecipeInput(template,base,addition))).filter(s -> !s.isEmpty()).toList();
            return new DefaultSmithingDisplay(List.of(
                recipe.templateIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty()),
                EntryIngredients.ofIngredient(recipe.baseIngredient()),
                recipe.additionIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())),
                List.of(EntryIngredients.ofItemStacks(outputs)),Optional.of(h.id().identifier()));
        });
        registry.beginRecipeFiller(TravellersGearModifierRecipe.class).fillMultiple(h -> {
            var recipe = h.value();
            var placement = recipe.placementInfo();
            var context = EntryIngredients.slotDisplayContext();
            List<List<ItemStack>> choices = placement.slotsToIngredientIndex().intStream()
                .mapToObj(i -> i == PlacementInfo.EMPTY_SLOT ? List.<ItemStack>of()
                    : placement.ingredients().get(i).display().resolveForStacks(context)).toList();
            var representatives = choices.stream().map(c -> c.isEmpty() ? ItemStack.EMPTY : c.getFirst()).toList();
            var input = CraftingInput.of(1,representatives.size(),representatives);
            List<ItemStack> outputs = new ArrayList<>();
            for (var alternatives : choices) for (var stack : alternatives)
                if (stack.has(TFDataComponents.IS_TRAVELLERS_GEAR) && !stack.is(TFItems.TRAVELLERS_BELT)) {
                    var result = recipe.applyModifier(stack.copy(),input);
                    if (!result.isEmpty()) outputs.add(result);
                }
            if (outputs.isEmpty()) return List.of();
            var inputs = choices.stream().map(EntryIngredients::ofItemStacks).toList();
            var result = List.of(EntryIngredients.ofItemStacks(outputs));
            var id = Optional.of(h.id().identifier());
            return List.of(recipe.isShapeless()
                ? new DefaultCustomShapelessDisplay(inputs,result,id)
                : new DefaultCustomShapedDisplay(inputs,result,id,recipe.getWidth(),recipe.getHeight()));
        });
    }
}
