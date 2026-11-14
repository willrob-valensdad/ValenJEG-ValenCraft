package top.ribs.scguns.client.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import top.ribs.scguns.Reference;
import top.ribs.scguns.compat.RecipeIngredientCompat;

import java.util.Iterator;
import java.util.List;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
public class PoweredMechanicalPressRecipe implements Recipe<PoweredMechanicalPressRecipe.Input> {
    private final NonNullList<Ingredient> inputItems;
    private final Ingredient moldItem;
    private final ItemStackTemplate output;
    private final int processingTime;
    private final int energyUse;

    public PoweredMechanicalPressRecipe(NonNullList<Ingredient> inputItems, Ingredient moldItem, ItemStackTemplate output, int processingTime, int energyUse) {
        this.inputItems = inputItems;
        this.moldItem = moldItem;
        this.output = output;
        this.processingTime = processingTime;
        this.energyUse = energyUse;
    }

    public boolean requiresMold() {
        return !RecipeIngredientCompat.isEmpty(moldItem);
    }

    @Override
    public boolean matches(Input inv, Level world) {
        if (world.isClientSide()) {
            return false;
        }

        NonNullList<Ingredient> requiredIngredients = NonNullList.create();
        requiredIngredients.addAll(inputItems);

        boolean moldMatched = RecipeIngredientCompat.isEmpty(moldItem) || moldItem.test(inv.getItem(Input.MOLD_INDEX));

        for (int i = 0; i < inv.size(); i++) {
            if (i == Input.MOLD_INDEX) continue;
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                boolean matched = false;
                Iterator<Ingredient> iterator = requiredIngredients.iterator();
                while (iterator.hasNext()) {
                    Ingredient ingredient = iterator.next();
                    if (ingredient.test(stackInSlot)) {
                        iterator.remove();
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    continue;
                }
            }
        }

        return requiredIngredients.isEmpty() && moldMatched;
    }

    @Override
    public ItemStack assemble(Input inv) {
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

    public int getEnergyUse() {
        return energyUse;
    }
    @Override
    public String group() {
        return "";
    }


    @Override
    public RecipeSerializer<PoweredMechanicalPressRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<PoweredMechanicalPressRecipe> getType() {
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

    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    public Ingredient getMoldItem() {
        return moldItem;
    }

    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> ingredients) {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    public record Input(List<ItemStack> items) implements RecipeInput {
        public static final int MOLD_INDEX = 3;

        public Input {
            items = List.copyOf(items);
        }

        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    public static class Type implements RecipeType<PoweredMechanicalPressRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "powered_mechanical_pressing";
    }

    public static class Serializer {
public static final Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "powered_mechanical_pressing");
        private static final MapCodec<PoweredMechanicalPressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> List.copyOf(recipe.inputItems)),
                Ingredient.CODEC.optionalFieldOf("mold", RecipeIngredientCompat.EMPTY_SLOT).forGetter(recipe -> recipe.moldItem),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                Codec.INT.optionalFieldOf("processingTime", 200).forGetter(recipe -> recipe.processingTime),
                Codec.INT.optionalFieldOf("energyUse", 1000).forGetter(recipe -> recipe.energyUse)
        ).apply(instance, (ingredients, moldItem, output, processingTime, energyUse) ->
                new PoweredMechanicalPressRecipe(toNonNullList(ingredients), moldItem, output, processingTime, energyUse)));
        private static final StreamCodec<RegistryFriendlyByteBuf, PoweredMechanicalPressRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );



        public static final RecipeSerializer<PoweredMechanicalPressRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static PoweredMechanicalPressRecipe decode(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            Ingredient moldItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            int processingTime = buffer.readInt();
            int energyUse = buffer.readInt();

            return new PoweredMechanicalPressRecipe(ingredients, moldItem, output, processingTime, energyUse);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, PoweredMechanicalPressRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getMoldItem());
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeInt(recipe.getProcessingTime());
            buffer.writeInt(recipe.getEnergyUse());
        }
    }
}
