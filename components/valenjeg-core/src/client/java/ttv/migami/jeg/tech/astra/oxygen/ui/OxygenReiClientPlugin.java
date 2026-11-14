package ttv.migami.jeg.tech.astra.oxygen.ui;
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
public final class OxygenReiClientPlugin implements REIClientPlugin {
 public void registerCategories(CategoryRegistry registry){if(!BuiltInRegistries.ITEM.containsKey(Identifier.parse("jeg:valen_oxygen_loader")))return;var category=new Category();registry.add(category);registry.addWorkstations(category.getCategoryIdentifier(),EntryStacks.of(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:valen_oxygen_loader"))));}
 private record Category() implements DisplayCategory<OxygenRecipeDisplay>{
  public CategoryIdentifier<OxygenRecipeDisplay> getCategoryIdentifier(){return OxygenRecipeDisplay.CATEGORY;}
  public Component getTitle(){return Component.literal("Oxygen Loading");}
  public Renderer getIcon(){return EntryStacks.of(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:valen_oxygen_loader")));}
  public int getDisplayWidth(OxygenRecipeDisplay d){return 220;}
  public int getDisplayHeight(){return 92;}
  public List<Widget> setupDisplay(OxygenRecipeDisplay d,Rectangle bounds){
   List<Widget> out=new ArrayList<>();out.add(Widgets.createRecipeBase(bounds));
   if(!d.getInputEntries().isEmpty())out.add(Widgets.createSlot(new Point(bounds.x+12,bounds.y+16)).entries(d.getInputEntries().getFirst()).markInput());
   else out.add(Widgets.createLabel(new Point(bounds.x+23,bounds.y+17),Component.literal("Oxygen")).noShadow());
   out.add(Widgets.createArrow(new Point(bounds.x+51,bounds.y+16)).animationDurationTicks(d.ticks));
   out.add(Widgets.createLabel(new Point(bounds.x+144,bounds.y+16),Component.literal(d.gasMb+" mB native oxygen")).noShadow());
   out.add(Widgets.createLabel(new Point(bounds.x+144,bounds.y+31),Component.literal(d.gasId)).noShadow());
   out.add(Widgets.createLabel(new Point(bounds.getCenterX(),bounds.y+49),Component.literal(d.inputMb+" mB "+(d.inputId.equals("minecraft:water")?"water":"native oxygen")+" + "+d.energy+" E/t · "+d.ticks+" tick")).noShadow());
   out.add(Widgets.createLabel(new Point(bounds.getCenterX(),bounds.y+66),Component.literal("Output: adjacent Nero gas tank")).noShadow());
   return out;
  }
 }
}
