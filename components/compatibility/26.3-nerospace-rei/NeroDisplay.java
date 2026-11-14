package dev.valen.compat.nerospace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Server-owned inputs, outputs and machine settings, synchronized by REI. */
public final class NeroDisplay extends BasicDisplay {
    public static final DisplaySerializer<NeroDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.<NeroDisplay>mapCodec(i -> i.group(
            Codec.STRING.fieldOf("category").forGetter(d -> d.category),
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(NeroDisplay::getInputEntries),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(NeroDisplay::getOutputEntries),
            Codec.INT.fieldOf("ticks").forGetter(d -> d.ticks),
            Codec.LONG.fieldOf("energy").forGetter(d -> d.energy),
            Codec.BOOL.fieldOf("random").forGetter(d -> d.random)
        ).apply(i, NeroDisplay::new)),
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, d -> d.category,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), NeroDisplay::getInputEntries,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), NeroDisplay::getOutputEntries,
            ByteBufCodecs.INT, d -> d.ticks,
            ByteBufCodecs.LONG, d -> d.energy,
            ByteBufCodecs.BOOL, d -> d.random, NeroDisplay::new));
    final String category;
    final int ticks;
    final long energy;
    final boolean random;

    public NeroDisplay(String category, List<EntryIngredient> inputs, List<EntryIngredient> outputs,
                       int ticks, long energy, boolean random) {
        super(inputs, outputs);
        this.category = category;
        this.ticks = ticks;
        this.energy = energy;
        this.random = random;
    }
    @Override public CategoryIdentifier<NeroDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of("nerospace", category);
    }
    @Override public DisplaySerializer<NeroDisplay> getSerializer() { return SERIALIZER; }
}
