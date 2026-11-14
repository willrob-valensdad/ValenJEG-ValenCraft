package dev.valen.compat.nerospace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import za.co.neroland.nerospace.registry.ModBlocks;
import za.co.neroland.nerospace.registry.ModItems;
import dev.architectury.fluid.FluidStack;

public final class NeroClientPlugin implements REIClientPlugin {
    @Override public void registerCategories(CategoryRegistry registry) {
        add(registry, "grinding", ModBlocks.NEROSIUM_GRINDER.get());
        add(registry, "refining", ModBlocks.FUEL_REFINERY.get());
        add(registry, "combustion_fuel", ModBlocks.COMBUSTION_GENERATOR.get());
    }
    private static void add(CategoryRegistry registry, String path, ItemLike machine) {
        var category = new MachineCategory(path, machine);
        registry.add(category);
        registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(machine));
    }
    private record MachineCategory(String path, ItemLike machine) implements DisplayCategory<NeroDisplay> {
        @Override public CategoryIdentifier<NeroDisplay> getCategoryIdentifier() {
            return CategoryIdentifier.of("nerospace", path);
        }
        @Override public Component getTitle() { return Component.translatable("rei.valen_nerospace." + path); }
        @Override public Renderer getIcon() { return EntryStacks.of(machine); }
        @Override public int getDisplayHeight() { return 78; }
        @Override public int getDisplayWidth(NeroDisplay display) { return 170; }
        @Override public List<Widget> setupDisplay(NeroDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            int x = bounds.x + 12, y = bounds.y + 10;
            for (var input : display.getInputEntries()) {
                widgets.add(Widgets.createSlot(new Point(x, y)).entries(input).markInput());
                x += 20;
            }
            widgets.add(Widgets.createArrow(new Point(bounds.x + 78, y))
                .animationDurationTicks(display.ticks));
            for (var output : display.getOutputEntries()) {
                var slot = Widgets.createSlot(new Point(bounds.x + 126, y));
                // The author fluid lacks a REI sprite. Keep fluid identity/amount in the display
                // and use its bucket as the visible symbol, with the actual volume below.
                widgets.add((path.equals("refining")
                    ? slot.entry(EntryStacks.of(ModItems.ROCKET_FUEL_BUCKET.get()))
                    : slot.entries(output)).markOutput());
            }
            String energyKey = path.equals("combustion_fuel")
                ? "jei.nerospace.stat.energy_generated" : "jei.nerospace.stat.energy_cost";
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 35),
                Component.translatable(energyKey, String.format(Locale.ROOT, "%,d", display.energy))).noShadow());
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 46),
                Component.translatable("jei.nerospace.stat.time",
                    String.format(Locale.ROOT, "%.1f", display.ticks / 20.0))).noShadow());
            if (path.equals("refining")) {
                var fluid = (FluidStack) display.getOutputEntries().getFirst().getFirst().getValue();
                widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 58),
                    Component.literal(String.format(Locale.ROOT, "%,d mB rocket fuel", fluid.getAmount() / 81))).noShadow());
            }
            if (display.random) widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 58),
                Component.translatable("rei.valen_nerospace.random")).noShadow());
            return widgets;
        }
    }
}
