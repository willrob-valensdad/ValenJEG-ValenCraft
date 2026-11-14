package dev.valen.compat.nerospace;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import za.co.neroland.nerolandcore.meteor.MeteorMaterialTags;
import za.co.neroland.nerospace.config.NerospaceConfig;
import za.co.neroland.nerospace.fluid.ModFluids;
import za.co.neroland.nerospace.machine.CombustionGeneratorBlockEntity;
import za.co.neroland.nerospace.machine.FuelRefineryBlockEntity;
import za.co.neroland.nerospace.machine.GrinderRecipes;
import za.co.neroland.nerospace.machine.NerosiumGrinderBlockEntity;
import za.co.neroland.nerospace.registry.ModBlocks;

public final class NeroCommonPlugin implements REICommonPlugin {
    @Override public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Identifier.fromNamespaceAndPath("valen_nerospace_rei", "machine"), NeroDisplay.SERIALIZER);
    }
    @Override public void registerDisplays(ServerDisplayRegistry registry) {
        displays().forEach(registry::add);
    }
    public static List<NeroDisplay> displays() {
        List<NeroDisplay> displays = new ArrayList<>();
        List<GrinderRecipes.Grinding> grinding = new ArrayList<>(GrinderRecipes.all());
        grinding.add(GrinderRecipes.meteor());
        int grindTicks = NerospaceConfig.scaleInterval(NerosiumGrinderBlockEntity.MAX_PROGRESS,
                                                      NerospaceConfig.machineSpeedMultiplier());
        for (GrinderRecipes.Grinding recipe : grinding) {
            EntryIngredient result = recipe.meteor()
                ? EntryIngredients.ofItemTag(MeteorMaterialTags.GRINDABLE)
                : EntryIngredients.of(recipe.output());
            displays.add(new NeroDisplay("grinding", List.of(EntryIngredients.of(recipe.input())),
                List.of(result), grindTicks, (long) grindTicks * NerospaceConfig.scale(NerosiumGrinderBlockEntity.ENERGY_PER_TICK,
                    NerospaceConfig.fuelCostMultiplier()),
                recipe.meteor()));
        }
        // This one hardcoded machine has no recipe object. Ask its actual slot filters;
        // no second input list or copied recipe table is maintained here.
        FuelRefineryBlockEntity refinery = new FuelRefineryBlockEntity(
            BlockPos.ZERO, ModBlocks.FUEL_REFINERY.get().defaultBlockState());
        List<ItemStack> carbon = new ArrayList<>(), catalyst = new ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (refinery.canPlaceItem(FuelRefineryBlockEntity.CARBON_SLOT, stack)) carbon.add(stack);
            if (refinery.canPlaceItem(FuelRefineryBlockEntity.CATALYST_SLOT, stack)) catalyst.add(stack);
        }
        int refineTicks = NerospaceConfig.scaleInterval(FuelRefineryBlockEntity.WORK_TICKS,
                                                       NerospaceConfig.machineSpeedMultiplier());
        displays.add(new NeroDisplay("refining",
            List.of(EntryIngredients.ofItemStacks(carbon), EntryIngredients.ofItemStacks(catalyst)),
            List.of(EntryIngredients.of(ModFluids.ROCKET_FUEL.get(), FuelRefineryBlockEntity.MB_PER_BATCH * 81L)),
            refineTicks, (long) refineTicks * NerospaceConfig.scale(FuelRefineryBlockEntity.FE_PER_TICK,
                NerospaceConfig.fuelCostMultiplier()), false));
        for (ItemStack fuel : CombustionGeneratorBlockEntity.knownFuels()) {
            int ticks = CombustionGeneratorBlockEntity.fuelValue(fuel);
            int rate = NerospaceConfig.scale(CombustionGeneratorBlockEntity.FE_PER_TICK,
                                            NerospaceConfig.energyRateMultiplier());
            displays.add(new NeroDisplay("combustion_fuel", List.of(EntryIngredients.of(fuel)),
                List.of(), ticks, (long) ticks * rate, false));
        }
        return List.copyOf(displays);
    }
}
