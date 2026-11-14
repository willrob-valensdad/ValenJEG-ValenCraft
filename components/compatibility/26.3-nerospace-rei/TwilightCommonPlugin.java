package dev.valen.compat.nerospace;

import java.util.*;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.*;
import twilightforest.block.OminousFireBlock;
import twilightforest.config.TFConfig;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.item.CrumbleHornItem;
import twilightforest.item.TransformPowderItem;
import twilightforest.item.recipe.DryingRecipe;
import twilightforest.item.recipe.UncraftingRecipe;
import twilightforest.tags.TFItemTags;

/** Loaded only with Twilight Forest; uses the existing server display/reload lifecycle. */
final class TwilightCommonPlugin {
    static void registerComparators(me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry registry) {
        registry.registerComponents(TFItems.KEEPSAKE_CASKET,TFItems.TRAVELLERS_GOGGLES,TFItems.TRAVELLERS_VEST,
            TFItems.TRAVELLERS_WINGS,TFItems.TRAVELLERS_BOOTS,TFItems.TRAVELLERS_GLOVES,TFItems.TRAVELLERS_BELT);
    }
    static void register(ServerDisplayRegistry registry) {
        TwilightCraftingCommon.register(registry);
        var registries = EntryIngredients.slotDisplayContext().getOrThrow(net.minecraft.world.item.crafting.display.SlotDisplayContext.REGISTRIES);
        for (var modified : twilightforest.compat.util.DefaultModifiedTravellersGearGetter.getDefaultModifiedTravellersGear(registries))
            registry.add(new PackRecipeDisplay("twilightforest:modifier_removal",
                "twilightforest:rei/grindstone/" + BuiltInRegistries.ITEM.getKey(modified.getItem()).getPath(),
                List.of(EntryIngredients.of(modified)),
                List.of(EntryIngredients.of(twilightforest.compat.util.DefaultModifiedTravellersGearGetter.getDemodifiedStack(modified))),
                0, "Removes traveller modifiers"));
        registry.beginRecipeFiller(DryingRecipe.class).filterType(TFRecipes.DRYING_RECIPE)
            .filter(h -> !h.id().identifier().getPath().contains("stale_bread"))
            .fill(h -> new PackRecipeDisplay("twilightforest:drying", h.id().identifier().toString(),
                List.of(EntryIngredients.ofIngredient(h.value().getInput())),
                List.of(EntryIngredients.of(h.value().getResult())), h.value().getDryingTime(), ""));
        if (!TFConfig.disableEntireTable)
            registry.beginRecipeFiller(CraftingRecipe.class).fillMultiple(TwilightCommonPlugin::uncrafting);

        // Retain one directed entry per actual native mapping, including both reversible directions.
        TransformPowderItem.TRANSFORMATION_POWDER.forEach((input, output) ->
            registry.add(entityTransform("transformation_powder", input, output,
                EntryIngredients.of(TFItems.TRANSFORMATION_POWDER))));
        OminousFireBlock.OMINOUS_FIRE.forEach((input, output) ->
            registry.add(entityTransform("ominous_fire", input, output,
                EntryIngredients.of(TFItems.EXANIMATE_ESSENCE))));
        CrumbleHornItem.CRUMBLE_HORN.forEach((input, output) ->
            registry.add(new PackRecipeDisplay("twilightforest:crumble_horn",
                "twilightforest:rei/crumble/" + BuiltInRegistries.BLOCK.getKey(input).toString().replace(':', '/'),
                List.of(EntryIngredients.of(input)), List.of(output.getFirst() == net.minecraft.world.level.block.Blocks.AIR
                    ? EntryIngredient.empty() : EntryIngredients.of(output.getFirst())),
                0, String.format(Locale.ROOT, "%.0f%% chance per application", output.getSecond() * 100))));
    }

    private static PackRecipeDisplay entityTransform(String category, EntityType<?> input,
                                                      EntityType<?> output, EntryIngredient catalyst) {
        String from = BuiltInRegistries.ENTITY_TYPE.getKey(input).toString();
        String to = BuiltInRegistries.ENTITY_TYPE.getKey(output).toString();
        return new PackRecipeDisplay("twilightforest:" + category,
            "twilightforest:rei/" + category + "/" + from.replace(':', '/'),
            List.of(catalyst), List.of(EntryIngredient.empty()), 0, from + "\n" + to);
    }

    static List<PackRecipeDisplay> uncrafting(RecipeHolder<CraftingRecipe> holder) {
        CraftingRecipe recipe = holder.value();
        boolean explicit = recipe instanceof UncraftingRecipe;
        if (!explicit && (recipe.getType() != RecipeType.CRAFTING || TFConfig.disableUncraftingOnly
                || (recipe instanceof ShapelessRecipe && !TFConfig.allowShapelessUncrafting)
                || TFConfig.reverseRecipeBlacklist != TFConfig.disableUncraftingRecipes.contains(holder.id().identifier().toString())
                || TFConfig.flipUncraftingModIdList != TFConfig.blacklistedUncraftingModIds.contains(holder.id().identifier().getNamespace())))
            return List.of();
        if (recipe.display().isEmpty()) return List.of();
        RecipeDisplay display = recipe.display().getFirst();
        List<SlotDisplay> slots;
        int width;
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            if (shaped.width() > 3 || shaped.height() > 3) return List.of();
            slots = shaped.ingredients(); width = shaped.width();
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            slots = shapeless.ingredients(); width = 3;
            if (slots.size() > 9) return List.of();
        } else return List.of();
        var context = EntryIngredients.slotDisplayContext();
        List<ItemStack> inputs = explicit
            ? ((UncraftingRecipe) recipe).getInput().items()
                .map(item -> new ItemStack(item, ((UncraftingRecipe) recipe).getCount())).toList()
            : display.result().resolveForStacks(context);
        inputs = inputs.stream().filter(stack -> !stack.isEmpty() && !stack.is(TFItemTags.BANNED_UNCRAFTABLES)).toList();
        if (inputs.isEmpty()) return List.of();
        List<EntryIngredient> outputs = new ArrayList<>(Collections.nCopies(9, EntryIngredient.empty()));
        int minCost = 0, maxCost = 0;
        for (int i = 0; i < slots.size(); i++) {
            var alternatives = slots.get(i).resolveForStacks(context).stream()
                .filter(stack -> !stack.isEmpty() && !stack.is(TFItemTags.BANNED_UNCRAFTING_INGREDIENTS)
                    && !UncraftingMenu.isIngredientProblematic(stack))
                .map(stack -> stack.copyWithCount(1)).toList();
            if (!alternatives.isEmpty()) {
                outputs.set(i / width * 3 + i % width, EntryIngredients.ofItemStacks(alternatives));
                if (alternatives.stream().allMatch(UncraftingMenu::isDamageableComponent)) minCost++;
                if (alternatives.stream().anyMatch(UncraftingMenu::isDamageableComponent)) maxCost++;
            }
        }
        if (outputs.stream().allMatch(List::isEmpty)) return List.of();
        minCost = (int)Math.round(minCost * TFConfig.uncraftingXpCostMultiplier);
        maxCost = (int)Math.round(maxCost * TFConfig.uncraftingXpCostMultiplier);
        String cost = explicit ? Integer.toString(((UncraftingRecipe) recipe).getCost())
            : minCost == maxCost ? Integer.toString(minCost) : minCost + "–" + maxCost;
        return List.of(new PackRecipeDisplay("twilightforest:uncrafting", holder.id().identifier().toString(),
            List.of(EntryIngredients.ofItemStacks(inputs)), outputs, 0, "Undamaged input · " + cost + " XP"));
    }
}
