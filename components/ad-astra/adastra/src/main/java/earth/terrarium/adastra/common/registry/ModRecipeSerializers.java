package earth.terrarium.adastra.common.registry;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.common.recipes.SpaceStationRecipe;
import earth.terrarium.adastra.common.recipes.machines.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipeSerializers {

    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, AdAstra.MOD_ID);

    public static final RegistryEntry<RecipeSerializer<CompressingRecipe>> COMPRESSING = RECIPE_SERIALIZERS.register("compressing", () ->
        new RecipeSerializer<>(CompressingRecipe.CODEC, CompressingRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<AlloyingRecipe>> ALLOYING = RECIPE_SERIALIZERS.register("alloying", () ->
        new RecipeSerializer<>(AlloyingRecipe.CODEC, AlloyingRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<OxygenLoadingRecipe>> OXYGEN_LOADING = RECIPE_SERIALIZERS.register("oxygen_loading", () ->
        new RecipeSerializer<>(OxygenLoadingRecipe.CODEC, OxygenLoadingRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<RefiningRecipe>> REFINING = RECIPE_SERIALIZERS.register("refining", () ->
        new RecipeSerializer<>(RefiningRecipe.CODEC, RefiningRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<CryoFreezingRecipe>> CRYO_FREEZING = RECIPE_SERIALIZERS.register("cryo_freezing", () ->
        new RecipeSerializer<>(CryoFreezingRecipe.CODEC, CryoFreezingRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<NasaWorkbenchRecipe>> NASA_WORKBENCH_SERIALIZER = RECIPE_SERIALIZERS.register("nasa_workbench", () ->
        new RecipeSerializer<>(NasaWorkbenchRecipe.CODEC, NasaWorkbenchRecipe.NETWORK_CODEC));

    public static final RegistryEntry<RecipeSerializer<SpaceStationRecipe>> SPACE_STATION_SERIALIZER = RECIPE_SERIALIZERS.register("space_station_recipe", () ->
        new RecipeSerializer<>(SpaceStationRecipe.CODEC, SpaceStationRecipe.NETWORK_CODEC));
}
