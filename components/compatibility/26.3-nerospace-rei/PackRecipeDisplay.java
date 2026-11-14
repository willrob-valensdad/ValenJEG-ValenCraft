package dev.valen.compat.nerospace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** Actual recipe inputs/output components and ID are preserved across REI synchronization. */
public final class PackRecipeDisplay extends BasicDisplay {
    public static final DisplaySerializer<PackRecipeDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.<PackRecipeDisplay>mapCodec(i -> i.group(
            Codec.STRING.fieldOf("category").forGetter(d -> d.category),
            Codec.STRING.fieldOf("recipe").forGetter(d -> d.recipe),
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(PackRecipeDisplay::getInputEntries),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(PackRecipeDisplay::getOutputEntries),
            Codec.INT.fieldOf("ticks").forGetter(d -> d.ticks),
            Codec.STRING.fieldOf("detail").forGetter(d -> d.detail)
        ).apply(i, PackRecipeDisplay::new)),
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, d -> d.category,
            ByteBufCodecs.STRING_UTF8, d -> d.recipe,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), PackRecipeDisplay::getInputEntries,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), PackRecipeDisplay::getOutputEntries,
            ByteBufCodecs.INT, d -> d.ticks,
            ByteBufCodecs.STRING_UTF8, d -> d.detail, PackRecipeDisplay::new));
    final String category, recipe, detail;
    final int ticks;
    public PackRecipeDisplay(String category, String recipe, List<EntryIngredient> inputs,
                             List<EntryIngredient> outputs, int ticks, String detail) {
        super(inputs, outputs, Optional.of(Identifier.parse(recipe)));
        this.category = category;
        this.recipe = recipe;
        this.ticks = ticks;
        this.detail = detail;
    }
    @Override public CategoryIdentifier<PackRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(category);
    }
    @Override public DisplaySerializer<PackRecipeDisplay> getSerializer() { return SERIALIZER; }
}
