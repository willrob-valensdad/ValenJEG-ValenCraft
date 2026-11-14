package dev.valen.compat.nerospace;

import java.util.*;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import twilightforest.config.TFConfig;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.tags.TFBlockTags;

/** Optional client presentation; all displayed recipes arrive from the server. */
final class TwilightClientPlugin {
    static void register(CategoryRegistry registry) {
        add(registry, "drying", "Drying", TFBlocks.SORTING_DRYING_RACK);
        add(registry, "modifier_removal", "Traveller Modifier Removal", net.minecraft.world.level.block.Blocks.GRINDSTONE);
        for (var block : BuiltInRegistries.BLOCK.getTagOrEmpty(TFBlockTags.DRYING_RACKS))
            registry.addWorkstations(CategoryIdentifier.of("twilightforest:drying"), EntryStacks.of(block.value()));
        if (!TFConfig.disableEntireTable) add(registry, "uncrafting", "Uncrafting", TFBlocks.UNCRAFTING_TABLE);
        add(registry, "transformation_powder", "Transformation Powder", TFItems.TRANSFORMATION_POWDER);
        add(registry, "ominous_fire", "Ominous Fire", TFItems.EXANIMATE_ESSENCE);
        add(registry, "crumble_horn", "Crumble Horn", TFItems.CRUMBLE_HORN);
    }
    private static void add(CategoryRegistry registry, String path, String title, ItemLike icon) {
        var category = new Category("twilightforest:" + path, title, icon);
        registry.add(category);
        registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(icon));
    }
    private record Category(String id, String title, ItemLike icon) implements DisplayCategory<PackRecipeDisplay> {
        public CategoryIdentifier<PackRecipeDisplay> getCategoryIdentifier() { return CategoryIdentifier.of(id); }
        public Component getTitle() { return Component.literal(title); }
        public Renderer getIcon() { return EntryStacks.of(icon); }
        public int getDisplayHeight() { return 96; }
        public int getDisplayWidth(PackRecipeDisplay display) { return 220; }
        public List<Widget> setupDisplay(PackRecipeDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            widgets.add(Widgets.createSlot(new Point(bounds.x + 15, bounds.y + 27))
                .entries(display.getInputEntries().getFirst()).markInput());
            if (id.endsWith("uncrafting")) {
                widgets.add(Widgets.createArrow(new Point(bounds.x + 62, bounds.y + 27)).disableAnimation());
                for (int i = 0; i < 9; i++)
                    widgets.add(Widgets.createSlot(new Point(bounds.x + 115 + i % 3 * 18, bounds.y + 9 + i / 3 * 18))
                        .entries(display.getOutputEntries().get(i)).markOutput());
            } else if (id.endsWith("transformation_powder") || id.endsWith("ominous_fire")) {
                String[] entities = display.detail.split("\n");
                for (int i = 0; i < 2; i++)
                    widgets.add(Widgets.createLabel(new Point(bounds.x + 135, bounds.y + 12 + 42 * i),
                        BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entities[i])).getDescription()).noShadow());
                widgets.add(Widgets.createLabel(new Point(bounds.x + 135, bounds.y + 33), Component.literal("↓")).noShadow());
                return widgets;
            } else {
                var arrow = Widgets.createArrow(new Point(bounds.x + 84, bounds.y + 27));
                widgets.add(display.ticks > 0 ? arrow.animationDurationTicks(display.ticks) : arrow.disableAnimation());
                if (id.endsWith("crumble_horn") && display.getOutputEntries().getFirst().isEmpty())
                    widgets.add(Widgets.createLabel(new Point(bounds.x + 164,bounds.y + 31),Component.literal("Break block")).noShadow());
                else widgets.add(Widgets.createSlot(new Point(bounds.x + 160, bounds.y + 27))
                    .entries(display.getOutputEntries().getFirst()).markOutput());
            }
            String detail = display.ticks > 0 ? String.format(Locale.ROOT, "%.1f s (%d ticks)", display.ticks / 20.0, display.ticks) : display.detail;
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getMaxY() - 15), Component.literal(detail)).noShadow());
            return widgets;
        }
    }
}
