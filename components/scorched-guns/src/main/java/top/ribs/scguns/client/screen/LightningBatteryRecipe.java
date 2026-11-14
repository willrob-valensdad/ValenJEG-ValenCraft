package top.ribs.scguns.client.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import top.ribs.scguns.compat.RecipeIngredientCompat;

import java.util.List;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
public class LightningBatteryRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStackTemplate output;
    private final int processingTime;
    private final int energyUse;

    public LightningBatteryRecipe(Ingredient input, ItemStackTemplate output, int processingTime, int energyUse) {
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.energyUse = energyUse;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return output.create();
    }

        public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

        public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.create();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public Ingredient getInput() {
        return input;
    }

    public int getEnergyUse() {
        return energyUse;
    }
    @Override
    public String group() {
        return "";
    }


    @Override
    public RecipeSerializer<LightningBatteryRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<LightningBatteryRecipe> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<LightningBatteryRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "lightning_battery";
    }

    public static class Serializer {
public static final Identifier ID = Identifier.fromNamespaceAndPath("scguns", "lightning_battery");
        private static final MapCodec<LightningBatteryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> List.of(recipe.input)),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                Codec.INT.fieldOf("processingTime").forGetter(recipe -> recipe.processingTime),
                Codec.INT.fieldOf("requiredEnergy").forGetter(recipe -> recipe.energyUse)
        ).apply(instance, (ingredients, output, processingTime, requiredEnergy) ->
                new LightningBatteryRecipe(ingredients.isEmpty() ? RecipeIngredientCompat.EMPTY_SLOT : ingredients.get(0), output, processingTime, requiredEnergy)));
        private static final StreamCodec<RegistryFriendlyByteBuf, LightningBatteryRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );



        public static final RecipeSerializer<LightningBatteryRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static LightningBatteryRecipe decode(RegistryFriendlyByteBuf buffer) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            int processingTime = buffer.readInt();
            int requiredEnergy = buffer.readInt();
            return new LightningBatteryRecipe(input, output, processingTime, requiredEnergy);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, LightningBatteryRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInput());
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeInt(recipe.getProcessingTime());
            buffer.writeInt(recipe.getEnergyUse());
        }
    }
}
