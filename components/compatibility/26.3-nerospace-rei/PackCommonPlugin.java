package dev.valen.compat.nerospace;

import java.util.ArrayList;
import net.fabricmc.loader.api.FabricLoader;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import com.tiviacz.travelersbackpack.common.recipes.BackpackUpgradeRecipe;
import com.crispytwig.naturalist.world.item.crafting.BugNetInteractionRecipe;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AlloyingRecipeInput;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.AnvilRecipeInput;
import org.betterx.betterend.recipe.builders.InfusionRecipe;
import org.betterx.betterend.rituals.InfusionRitual.InfusionInput;
import fr.factionbedrock.aerialhell.Recipe.FreezingRecipe;
import fr.factionbedrock.aerialhell.Recipe.OscillatingRecipe;

public final class PackCommonPlugin implements REICommonPlugin {
    @Override public void registerItemComparators(me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("twilightforest")) TwilightCommonPlugin.registerComparators(registry);
        if (FabricLoader.getInstance().isModLoaded("jeg"))
            registry.registerComponents(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:vehicle_container")));
    }
    @Override public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Identifier.fromNamespaceAndPath("valen_nerospace_rei", "pack_recipe"),
                          PackRecipeDisplay.SERIALIZER);
    }
    @Override public void registerDisplays(ServerDisplayRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("twilightforest")) TwilightCommonPlugin.register(registry);
        if (FabricLoader.getInstance().isModLoaded("spacereloaded")) SpaceCommonPlugin.register(registry);
        if (FabricLoader.getInstance().isModLoaded("workingvillagers")) WorkerCommonPlugin.register(registry);
        if (FabricLoader.getInstance().isModLoaded("jeg")) JegCommonPlugin.register(registry);
        registry.beginRecipeFiller(AlloyingRecipe.class).filterType(AlloyingRecipe.TYPE).fill(h -> {
            var r = h.value();
            var inputs = r.getIngredients().stream()
                .map(i -> i.map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())).toList();
            return new PackRecipeDisplay("bclib:alloying", h.id().identifier().toString(), inputs,
                List.of(EntryIngredients.of(r.assemble((AlloyingRecipeInput) null))), r.getSmeltTime(),
                String.format(Locale.ROOT, "%.2f XP", r.experience()));
        });
        registry.beginRecipeFiller(AnvilRecipe.class).filterType(AnvilRecipe.TYPE).fill(h -> {
            var r = h.value();
            var hammer = r.getIngredients().getFirst().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty());
            var material = EntryIngredients.ofItemStacks(r.getMainIngredient().items()
                .map(i -> new ItemStack(i.value(), r.getInputCount())).toList());
            return new PackRecipeDisplay("bclib:smithing", h.id().identifier().toString(),
                List.of(hammer, material), List.of(EntryIngredients.of(r.assemble((AnvilRecipeInput) null))),
                0, "Anvil tier " + r.getAnvilLevel() + "; tool damage " + r.getDamage());
        });
        registry.beginRecipeFiller(InfusionRecipe.class).filterType(InfusionRecipe.TYPE).fill(h -> {
            var r = h.value();
            List<EntryIngredient> inputs = new ArrayList<>();
            inputs.add(EntryIngredients.ofIngredient(r.getInput()));
            // Keep all eight clockwise positions, including empty pedestals.
            for (var catalyst : r.getCatalysts())
                inputs.add(catalyst == null ? EntryIngredient.empty() : EntryIngredients.ofIngredient(catalyst));
            return new PackRecipeDisplay("betterend:infusion", h.id().identifier().toString(), inputs,
                List.of(EntryIngredients.of(r.assemble((InfusionInput) null))), r.getInfusionTime(), "");
        });
        registry.beginRecipeFiller(BackpackUpgradeRecipe.class).fillMultiple(h ->
            h.value().display().stream().filter(SmithingRecipeDisplay.class::isInstance)
                .map(SmithingRecipeDisplay.class::cast).map(d -> new DefaultSmithingDisplay(
                    List.of(EntryIngredients.ofSlotDisplay(d.template()), EntryIngredients.ofSlotDisplay(d.base()),
                            EntryIngredients.ofSlotDisplay(d.addition())),
                    List.of(EntryIngredients.ofSlotDisplay(d.result())), Optional.of(h.id().identifier()))).toList());
        registry.beginRecipeFiller(BugNetInteractionRecipe.class).fill(h -> {
            var r = h.value();
            List<EntryIngredient> inputs = new ArrayList<>();
            inputs.add(EntryIngredients.of(BuiltInRegistries.ITEM.getValue(Identifier.parse("naturalist:capture_net"))));
            r.ingredient().ifPresent(i -> inputs.add(EntryIngredients.ofIngredient(i)));
            return new PackRecipeDisplay("naturalist:net", h.id().identifier().toString(), inputs,
                List.of(EntryIngredients.of(r.dropStack())), 0,
                "Use net on " + BuiltInRegistries.ENTITY_TYPE.getKey(r.entityType()));
        });
        registry.beginRecipeFiller(FreezingRecipe.class).fill(h -> cooking(h, "aerialhell:freezing"));
        registry.beginRecipeFiller(OscillatingRecipe.class).fill(h -> cooking(h, "aerialhell:oscillating"));
    }
    private static PackRecipeDisplay cooking(RecipeHolder<? extends AbstractCookingRecipe> holder, String category) {
        var r = holder.value();
        return new PackRecipeDisplay(category, holder.id().identifier().toString(),
            List.of(EntryIngredients.ofIngredient(r.input())),
            List.of(EntryIngredients.of(r.assemble((SingleRecipeInput) null))), r.cookingTime(),
            String.format(Locale.ROOT, "%.2f XP", r.experience()));
    }
}
