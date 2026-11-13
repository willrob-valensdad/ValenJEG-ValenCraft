package ttv.migami.jeg.station.rei;
import java.util.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

/** Categories only: displays are supplied by the server even with forceLocalRecipes=NEVER. */
public final class StationClientPlugin implements REIClientPlugin {
    @Override public void registerCategories(CategoryRegistry registry) {
        for(String station:List.of("mo_workbench","scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","schematic_station","recycler")) {
            var category=new Category(station);registry.add(category);
            registry.addWorkstations(category.getCategoryIdentifier(),EntryStacks.of(category.item()));
        }
    }
    private record Category(String station) implements DisplayCategory<StationDisplay> {
        String namespace(){return station.equals("mo_workbench") ? "mteg" : "jeg";}
        Item item() {return BuiltInRegistries.ITEM.getValue(Identifier.parse(namespace()+":"+station));}
        public CategoryIdentifier<StationDisplay> getCategoryIdentifier() {return CategoryIdentifier.of(namespace(),station);}
        public Component getTitle() {return Component.translatable("block."+namespace()+"."+station);}
        public Renderer getIcon() {return EntryStacks.of(item());}
        public int getDisplayHeight() {return 98;}
        public int getDisplayWidth(StationDisplay display) {return 190;}
        public List<Widget> setupDisplay(StationDisplay display,Rectangle bounds) {
            List<Widget> widgets=new ArrayList<>();widgets.add(Widgets.createRecipeBase(bounds));
            for(int i=0;i<display.getInputEntries().size();i++)
                widgets.add(Widgets.createSlot(new Point(bounds.x+10+(i%4)*22,bounds.y+12+(i/4)*22))
                    .entries(display.getInputEntries().get(i)).markInput());
            var arrow=Widgets.createArrow(new Point(bounds.x+106,bounds.y+25));
            widgets.add(display.ticks>0 ? arrow.animationDurationTicks(display.ticks) : arrow.disableAnimation());
            widgets.add(Widgets.createSlot(new Point(bounds.x+152,bounds.y+25)).entries(display.getOutputEntries().getFirst()).markOutput());
            if(station.equals("recycler"))widgets.add(Widgets.createSlot(new Point(bounds.x+106,bounds.y+51)).entry(EntryStacks.of(Items.COAL)));
            String detail=display.detail+(display.ticks>0 ? " · "+(display.ticks/20.0)+" s" : "");
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(),bounds.getMaxY()-14),Component.literal(detail)).noShadow());
            return widgets;
        }
    }
}
