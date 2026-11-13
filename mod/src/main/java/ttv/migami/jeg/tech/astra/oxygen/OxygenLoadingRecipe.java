package ttv.migami.jeg.tech.astra.oxygen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** MIT Ad Astra water recipe fields; output binds to the existing Nero gas identity. */
public record OxygenLoadingRecipe(int cookingTime, int energy, Input input, Result result) {
    public record Values(Identifier tag) {
        static final Codec<Values> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("tag").forGetter(Values::tag)).apply(i, Values::new));
    }
    public record Input(long amount, Values values) {
        static final Codec<Input> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, 3000).xmap(Integer::longValue, Math::toIntExact).fieldOf("amount").forGetter(Input::amount),
            Values.CODEC.fieldOf("values").forGetter(Input::values)).apply(i, Input::new));
    }
    public record Result(long amount, Identifier id) {
        static final Codec<Result> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, 3000).xmap(Integer::longValue, Math::toIntExact).fieldOf("amount").forGetter(Result::amount),
            Identifier.CODEC.fieldOf("id").forGetter(Result::id)).apply(i, Result::new));
    }
    public static final Codec<OxygenLoadingRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.intRange(1, 20000).fieldOf("cookingtime").forGetter(OxygenLoadingRecipe::cookingTime),
        Codec.intRange(1, 20000).fieldOf("energy").forGetter(OxygenLoadingRecipe::energy),
        Input.CODEC.fieldOf("input").forGetter(OxygenLoadingRecipe::input),
        Result.CODEC.fieldOf("result").forGetter(OxygenLoadingRecipe::result)).apply(i, OxygenLoadingRecipe::new));
    private static volatile OxygenLoadingRecipe active;
    public static OxygenLoadingRecipe active() { return active; }
    public void validateBinding() {
        if (!input.values.tag.equals(Identifier.parse("minecraft:water"))
            || !result.id.equals(Identifier.parse("nerospace:oxygen")))
            throw new IllegalArgumentException("First oxygen loader supports only native water -> existing nerospace:oxygen");
    }
    public static final class Loader extends SimpleJsonResourceReloadListener<OxygenLoadingRecipe> implements IdentifiableResourceReloadListener {
        public Loader() { super(CODEC, FileToIdConverter.json("valen_oxygen_loading")); }
        @Override public Identifier getFabricId() { return Identifier.parse("jeg:valen_oxygen_loading"); }
        @Override protected void apply(Map<Identifier, OxygenLoadingRecipe> recipes, ResourceManager manager, ProfilerFiller profiler) {
            OxygenLoadingRecipe water = recipes.get(Identifier.parse("jeg:water"));
            if (water != null) water.validateBinding();
            active = water;
        }
    }
}
