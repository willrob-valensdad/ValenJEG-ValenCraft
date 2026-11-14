package dev.valen.compat.nerospace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import org.alex_melan.spacereloaded.registry.ModBlocks;

/** Each chemical machine has its own category and workstation, using server-provided displays. */
final class SpaceClientPlugin {
    static void register(CategoryRegistry registry) {
        add(registry, "crushing", "Crushing", ModBlocks.CRUSHER);
        add(registry, "electric_smelting", "Electric Smelting", ModBlocks.ELECTRIC_FURNACE);
        add(registry, "assembly", "Assembly", ModBlocks.ASSEMBLY_TABLE);
        add(registry, "pressing", "Mechanical Pressing", ModBlocks.MECHANICAL_PRESS);
        add(registry, "machining", "Lathe Machining", ModBlocks.LATHE);
        add(registry, "chemical/chemical_reactor", "Chemical Reactor", ModBlocks.CHEMICAL_REACTOR);
        add(registry, "chemical/electric_furnace", "Furnace Chemistry", ModBlocks.ELECTRIC_FURNACE);
        add(registry, "chemical/sabatier_reactor", "Sabatier Reactor", ModBlocks.SABATIER_REACTOR);
        add(registry, "chemical/electrolyzer", "Electrolysis", ModBlocks.ELECTROLYZER);
        add(registry, "chemical/deposition_reactor", "Deposition", ModBlocks.DEPOSITION_REACTOR);
        add(registry, "chemical/regolith_reactor", "Regolith Reactor", ModBlocks.REGOLITH_REACTOR);
    }
    private static void add(CategoryRegistry registry, String path, String title, ItemLike machine) {
        var category = new Category("spacereloaded:" + path, title, machine);
        registry.add(category);
        registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(machine));
    }
    private record Category(String id, String title, ItemLike machine) implements DisplayCategory<PackRecipeDisplay> {
        @Override public CategoryIdentifier<PackRecipeDisplay> getCategoryIdentifier() { return CategoryIdentifier.of(id); }
        @Override public Component getTitle() { return Component.literal(title); }
        @Override public Renderer getIcon() { return EntryStacks.of(machine); }
        @Override public int getDisplayHeight() { return 160; }
        @Override public int getDisplayWidth(PackRecipeDisplay display) { return 300; }
        @Override public List<Widget> setupDisplay(PackRecipeDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            for (int i = 0; i < display.getInputEntries().size(); i++)
                widgets.add(Widgets.createSlot(new Point(bounds.x + 14 + 20 * (i % 3), bounds.y + 12 + 20 * (i / 3)))
                    .entries(display.getInputEntries().get(i)).markInput());
            var arrow = Widgets.createArrow(new Point(bounds.x + 108, bounds.y + 32));
            widgets.add(display.ticks > 0 ? arrow.animationDurationTicks(display.ticks) : arrow.disableAnimation());
            for (int i = 0; i < display.getOutputEntries().size(); i++)
                widgets.add(Widgets.createSlot(new Point(bounds.x + 170 + 22 * i, bounds.y + 32))
                    .entries(display.getOutputEntries().get(i)).markOutput());
            String detail = display.ticks > 0
                ? String.format(Locale.ROOT, "%.1f s", display.ticks / 20.0) + "\n" + display.detail : display.detail;
            int y = bounds.y + 78;
            for (String line : detail.split("\n")) {
                widgets.add(Widgets.createLabel(new Point(bounds.x + 10, y), Component.literal(line)).leftAligned().noShadow());
                y += 11;
            }
            return widgets;
        }
    }
}
