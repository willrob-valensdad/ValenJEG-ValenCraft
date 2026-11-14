package dev.valen.compat.nerospace;

import java.util.ArrayList;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import org.betterx.betterend.registry.block.EndFunctionalBlocks;
import fr.factionbedrock.aerialhell.Registry.AerialHellBlocks;
import fr.factionbedrock.aerialhell.Registry.AerialHellItems;

public final class PackClientPlugin implements REIClientPlugin {
    @Override public void registerCategories(CategoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("twilightforest")) TwilightClientPlugin.register(registry);
        if (FabricLoader.getInstance().isModLoaded("spacereloaded")) SpaceClientPlugin.register(registry);
        if (FabricLoader.getInstance().isModLoaded("workingvillagers")) {
            add(registry, "workingvillagers:milling", "Milling", BuiltInRegistries.ITEM.getValue(Identifier.parse("workingvillagers:mining_bench")));
            add(registry, "workingvillagers:woodcutting", "Woodcutting", BuiltInRegistries.ITEM.getValue(Identifier.parse("workingvillagers:woodcutter")));
        }
        if (FabricLoader.getInstance().isModLoaded("jeg"))
            add(registry, "jeg:vehicle_assembly", "Vehicle Assembly", BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:vehicle_assembling_table")));
        add(registry, "bclib:alloying", "Alloying", EndFunctionalBlocks.END_STONE_SMELTER);
        add(registry, "bclib:smithing", "Hammer and Anvil", EndFunctionalBlocks.AETERNIUM_ANVIL);
        add(registry, "betterend:infusion", "End Infusion", EndFunctionalBlocks.INFUSION_PEDESTAL);
        add(registry, "naturalist:net", "Capture Net Interactions", BuiltInRegistries.ITEM.getValue(Identifier.parse("naturalist:capture_net")));
        add(registry, "aerialhell:freezing", "Freezing", AerialHellBlocks.FREEZER);
        add(registry, "aerialhell:oscillating", "Oscillating", AerialHellBlocks.OSCILLATOR);
    }
    private static void add(CategoryRegistry registry, String id, String title, ItemLike machine) {
        var category = new Category(id, title, machine);
        registry.add(category);
        registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(machine));
    }
    private record Category(String id, String title, ItemLike machine) implements DisplayCategory<PackRecipeDisplay> {
        @Override public CategoryIdentifier<PackRecipeDisplay> getCategoryIdentifier() { return CategoryIdentifier.of(id); }
        @Override public Component getTitle() { return Component.literal(title); }
        @Override public Renderer getIcon() { return EntryStacks.of(machine); }
        @Override public int getDisplayHeight() { return id.equals("betterend:infusion") ? 132 : 88; }
        @Override public int getDisplayWidth(PackRecipeDisplay display) { return id.equals("jeg:vehicle_assembly") ? 230 : 190; }
        @Override public List<Widget> setupDisplay(PackRecipeDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            if (id.equals("betterend:infusion")) {
                // Author order: north, northeast, east, southeast, south, southwest, west, northwest.
                int[][] ring = {{52,18},{78,28},{90,54},{78,80},{52,92},{26,80},{14,54},{26,28}};
                String[] compass = {"N","NE","E","SE","S","SW","W","NW"};
                widgets.add(Widgets.createSlot(new Point(bounds.x + 52, bounds.y + 54))
                    .entries(display.getInputEntries().getFirst()).markInput());
                for (int i = 0; i < 8; i++) {
                    Point point = new Point(bounds.x + ring[i][0], bounds.y + ring[i][1]);
                    widgets.add(Widgets.createSlot(point).entries(display.getInputEntries().get(i + 1)).markInput());
                    widgets.add(Widgets.createLabel(new Point(point.x + 8, point.y - 9),
                        Component.literal(compass[i])).noShadow());
                }
                widgets.add(Widgets.createArrow(new Point(bounds.x + 120, bounds.y + 54))
                    .animationDurationTicks(display.ticks));
                widgets.add(Widgets.createSlot(new Point(bounds.x + 158, bounds.y + 54))
                    .entries(display.getOutputEntries().getFirst()).markOutput());
            } else {
                for (int i = 0; i < display.getInputEntries().size(); i++)
                    widgets.add(Widgets.createSlot(new Point(bounds.x + 14 + 22 * i, bounds.y + 14))
                        .entries(display.getInputEntries().get(i)).markInput());
                var arrow = Widgets.createArrow(new Point(bounds.x + (id.equals("jeg:vehicle_assembly") ? 128 : 92), bounds.y + 14));
                widgets.add(display.ticks > 0 ? arrow.animationDurationTicks(display.ticks) : arrow.disableAnimation());
                widgets.add(Widgets.createSlot(new Point(bounds.x + (id.equals("jeg:vehicle_assembly") ? 192 : 152), bounds.y + 14))
                    .entries(display.getOutputEntries().getFirst()).markOutput());
                if (id.startsWith("aerialhell:")) {
                    ItemLike fuel = id.endsWith("freezing") ? AerialHellItems.MAGMATIC_GEL : AerialHellItems.FLUORITE;
                    widgets.add(Widgets.createSlot(new Point(bounds.x + 14, bounds.y + 44)).entry(EntryStacks.of(fuel)));
                    widgets.add(Widgets.createLabel(new Point(bounds.x + 40, bounds.y + 48),
                        Component.literal("Machine fuel")).leftAligned().noShadow());
                }
            }
            String detail = display.detail;
            if (display.ticks > 0) detail = String.format(Locale.ROOT, "%.1f s", display.ticks / 20.0)
                + (detail.isEmpty() ? "" : " · " + detail);
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getMaxY() - 14),
                Component.literal(detail)).noShadow());
            return widgets;
        }
    }
}
