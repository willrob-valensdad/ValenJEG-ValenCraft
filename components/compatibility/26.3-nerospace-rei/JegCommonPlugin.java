package dev.valen.compat.nerospace;

import java.util.List;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.vehicle.block.entity.VehicleContainerBlockEntity;
import ttv.migami.jeg.vehicle.recipe.VehicleAssemblyRecipeManager;

/** Uses the author's enabled, server-loaded assembly recipes and actual container output. */
final class JegCommonPlugin {
    static void register(ServerDisplayRegistry registry) {
        for (var recipe : VehicleAssemblyRecipeManager.recipes()) {
            var inputs = recipe.ingredients().stream().map(ingredient -> EntryIngredients.of(
                new ItemStack(BuiltInRegistries.ITEM.getValue(ingredient.item()), ingredient.count()))).toList();
            registry.add(new PackRecipeDisplay("jeg:vehicle_assembly", recipe.id().toString(),
                inputs, List.of(EntryIngredients.of(VehicleContainerBlockEntity.createItemForVehicle(recipe.resultVehicle()))),
                0, "Contains " + recipe.resultVehicle()), recipe);
        }
    }
}
