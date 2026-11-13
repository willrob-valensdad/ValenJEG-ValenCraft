package ttv.migami.jeg.tech.astra.oxygen.ui;
import java.util.List;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import ttv.migami.jeg.tech.astra.oxygen.*;
public final class OxygenReiCommonPlugin implements REICommonPlugin {
 public void registerDisplaySerializer(DisplaySerializerRegistry registry){registry.register(Identifier.parse("jeg:oxygen_processing_display"),OxygenRecipeDisplay.SERIALIZER);}
 public void registerDisplays(ServerDisplayRegistry registry){
  var recipe=OxygenLoadingRecipe.active();if(recipe!=null){
  var water=EntryIngredient.of(EntryStacks.of(FluidStack.create(Fluids.WATER,recipe.input().amount()*OxygenLoaderProcess.DROPLETS_PER_MB)));
  registry.add(new OxygenRecipeDisplay(List.of(water),recipe.energy(),recipe.cookingTime(),(int)recipe.input().amount(),(int)recipe.result().amount(),"minecraft:water",recipe.result().id().toString(),"jeg:water"),recipe);}
  var gas=OxygenPassthroughRecipe.active();if(gas!=null)registry.add(new OxygenRecipeDisplay(List.of(),gas.energy(),gas.cookingTime(),(int)gas.input().amount(),(int)gas.result().amount(),"nerospace:oxygen",gas.result().id().toString(),"jeg:oxygen"),gas);
 }
}
