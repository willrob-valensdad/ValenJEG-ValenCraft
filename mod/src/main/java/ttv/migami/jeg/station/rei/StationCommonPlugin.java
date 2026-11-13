package ttv.migami.jeg.station.rei;
import java.util.*;
import me.shedaniel.rei.api.common.display.*;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.station.*;

/** Same server-owned display registration contract as the accepted valen-pack-recipes plugin. */
public final class StationCommonPlugin implements REICommonPlugin {
    @Override public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Identifier.parse("jeg:station_display"),StationDisplay.SERIALIZER);
    }
    @Override public void registerItemComparators(me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry registry) {
        registry.registerComponents(ModItems.STATION_MATERIALS.get("firearm_blueprint").get());
        registry.registerComponents(ModItems.STATION_MATERIALS.get("advanced_firearm_blueprint").get());
    }
    private static ItemStack blueprint(String type,String target) {
        ItemStack stack=new ItemStack(ModItems.STATION_MATERIALS.get(type).get());
        stack.set(ModDataComponents.BLUEPRINT_GUN.get(),target);return stack;
    }
    private static EntryIngredient ingredient(StationRecipe.Material material) {
        if(!material.tag())return EntryIngredients.of(new ItemStack(BuiltInRegistries.ITEM.getValue(material.id()),material.count()));
        List<ItemStack> choices=new ArrayList<>();
        for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,material.id())))
            choices.add(new ItemStack(holder.value(),material.count()));
        return EntryIngredients.ofItemStacks(choices);
    }
    @Override public void registerDisplays(ServerDisplayRegistry registry) {
        for(String station:List.of("mo_workbench","scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycler")) {
            for(StationRecipe recipe:StationRecipes.forStation(station)) {
                List<EntryIngredient> inputs=new ArrayList<>();
                String detail=station.equals("recycler") ? "Fuel required; "+recipe.experience()+" XP" : "";
                if(station.equals("blueprint_workbench")) {
                    List<ItemStack> blueprints=new ArrayList<>();
                    if(StationBlockEntity.blueprintTarget(new ItemStack(BuiltInRegistries.ITEM.getValue(recipe.result())))!=null)
                        blueprints.add(blueprint("firearm_blueprint",recipe.result().toString()));
                    blueprints.add(blueprint("advanced_firearm_blueprint",recipe.result().toString()));
                    inputs.add(EntryIngredients.ofItemStacks(blueprints));detail="Blueprint retained";
                }
                recipe.materials().forEach(m->inputs.add(ingredient(m)));
                registry.add(new StationDisplay((station.equals("mo_workbench") ? "mteg:" : "jeg:")+station,recipe.id().toString(),inputs,
                    List.of(EntryIngredients.of(recipe.output())),station.equals("recycler") ? recipe.ticks() : 0,detail),recipe);
            }
        }
        for(StationRecipe recipe:StationRecipes.forStation("blueprint_workbench")) {
            ItemStack gun=new ItemStack(BuiltInRegistries.ITEM.getValue(recipe.result()));
            if(StationBlockEntity.blueprintTarget(gun)==null)continue;
            ItemStack blank=new ItemStack(ModItems.STATION_MATERIALS.get("firearm_blueprint").get());
            ItemStack written=StationBlockEntity.copyBlueprint(gun,blank);
            List<EntryIngredient> source=List.of(EntryIngredients.ofItemStacks(List.of(gun,written)),EntryIngredients.of(blank));
            registry.add(new StationDisplay("jeg:schematic_station","jeg:schematic/"+recipe.result().getPath(),source,
                List.of(EntryIngredients.of(written)),0,"Source retained; one blank consumed"),recipe);
        }
    }
}
