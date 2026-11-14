package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record PotionRecipe(FluidStack result, FluidIngredient fluidIngredient,
                           PotionIngredient reagent) implements BasinRecipe {
    public static final MapCodec<PotionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((RecordCodecBuilder.Instance<PotionRecipe> instance) -> instance.group(
        FluidStack.CODEC.fieldOf("result").forGetter(PotionRecipe::result),
        FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PotionRecipe::fluidIngredient),
        com.mojang.serialization.Codec.withAlternative(PotionIngredient.MAP_CODEC.codec(),
            Ingredient.CODEC.xmap(i -> new PotionIngredient(i, Optional.empty()), PotionIngredient::ingredient))
            .fieldOf("ingredient").forGetter(PotionRecipe::reagent)
    ).apply(instance, PotionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> STREAM_CODEC = StreamCodec.composite(
        FluidStack.PACKET_CODEC,
        PotionRecipe::result,
        FluidIngredient.PACKET_CODEC,
        PotionRecipe::fluidIngredient,
        PotionIngredient.STREAM_CODEC,
        PotionRecipe::reagent,
        PotionRecipe::new
    );
    public static final RecipeSerializer<PotionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void register(Map<Identifier, Recipe<?>> map) {
        // Native 26.3 brewing recipes are data-driven; preserve their reagent predicates.
        List<Reference<Potion>> potions = BuiltInRegistries.POTION.listElements().toList();
        for (Map.Entry<Identifier, Recipe<?>> entry : List.copyOf(map.entrySet())) {
            if (!(entry.getValue() instanceof BrewingRecipe brewing)) continue;
            ItemStack output = brewing.getOutput().create();
            if (!PotionFluidHandler.isPotionItem(output)) continue;
            FluidStack result = PotionFluidHandler.getFluidFromPotionItem(output);
            result.setAmount(81000);
            for (Holder<Item> container : brewing.getInput().ingredient().items().toList()) {
                for (Reference<Potion> potion : potions) {
                    ItemStack input = new ItemStack(container);
                    input.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    if (!PotionFluidHandler.isPotionItem(input) || !brewing.getInput().test(input)) continue;
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(container.value());
                    Identifier potionId = potion.key().identifier();
                    Identifier id = Identifier.fromNamespaceAndPath(MOD_ID,
                        "potion_mixing/" + entry.getKey().getNamespace() + "/" + entry.getKey().getPath()
                        + "/" + itemId.getNamespace() + "/" + itemId.getPath()
                        + "/" + potionId.getNamespace() + "/" + potionId.getPath());
                    map.putIfAbsent(id, new PotionRecipe(result.copy(),
                        PotionFluidHandler.getFluidIngredientFromPotion(new PotionContents(potion),
                            PotionFluidHandler.bottleTypeFromItem(container.value()), 81000),
                        brewing.getReagent()));
                }
            }
        }
    }

    public Ingredient ingredient() {
        return reagent.ingredient();
    }

    @Override
    public int getIngredientSize() {
        return 2;
    }

    @Override
    public List<SizedIngredient> ingredients() {
        return List.of(new SizedIngredient(ingredient(), 1));
    }

    @Override
    public List<FluidIngredient> fluidIngredients() {
        return List.of(fluidIngredient);
    }

    @Override
    public HeatCondition heat() {
        return HeatCondition.HEATED;
    }

    @Override
    public boolean matches(BasinInput input, Level world) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        if (!filter.test(result)) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, reagent);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredient)) {
            return false;
        }
        return input.acceptOutputs(outputs, List.of(result), true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, reagent, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredient, changes)) {
            return false;
        }
        List<FluidStack> fluids = List.of(result);
        if (!input.acceptOutputs(outputs, fluids, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluids, false);
    }

    @Override
    public RecipeSerializer<PotionRecipe> getSerializer() {
        return AllRecipeSerializers.POTION;
    }

    @Override
    public RecipeType<PotionRecipe> getType() {
        return AllRecipeTypes.POTION;
    }

}