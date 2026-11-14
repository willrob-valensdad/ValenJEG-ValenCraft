package earth.terrarium.common_storage_lib.resources.fluid.ingredient.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.common_storage_lib.resources.ResourceLib;
import earth.terrarium.common_storage_lib.resources.fluid.FluidResource;
import earth.terrarium.common_storage_lib.resources.fluid.ingredient.FluidIngredient;
import earth.terrarium.common_storage_lib.resources.fluid.ingredient.FluidIngredientType;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ComponentFluidIngredient(FluidIngredient ingredient, DataComponentExactPredicate predicate) implements FluidIngredient {
    public static final MapCodec<ComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(ComponentFluidIngredient::ingredient),
            DataComponentExactPredicate.CODEC.fieldOf("predicate").forGetter(ComponentFluidIngredient::predicate)
    ).apply(instance, ComponentFluidIngredient::new));

    public static final FluidIngredientType<ComponentFluidIngredient> TYPE = new FluidIngredientType<>(Identifier.fromNamespaceAndPath(ResourceLib.MOD_ID, "component"), CODEC);

    @Override
    public boolean test(FluidResource fluidResource) {
        return ingredient.test(fluidResource) && predicate.test(fluidResource.getComponents());
    }

    @Override
    public List<FluidResource> getMatchingFluids() {
        return ingredient().getMatchingFluids().stream().map(fluidResource -> fluidResource.modify(predicate.asPatch())).toList();
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }
}
