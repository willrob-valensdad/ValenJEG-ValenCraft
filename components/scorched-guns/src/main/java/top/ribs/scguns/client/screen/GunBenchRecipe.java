package top.ribs.scguns.client.screen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.compat.RecipeIngredientCompat;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
public class GunBenchRecipe implements Recipe<GunBenchRecipe.Input> {
    private final ItemStackTemplate output;
    private final NonNullList<Ingredient> recipeItems;
    private final Ingredient blueprint;

    public GunBenchRecipe(ItemStackTemplate output, NonNullList<Ingredient> recipeItems, Ingredient blueprint) {
        this.output = output;
        this.recipeItems = recipeItems;
        this.blueprint = blueprint;
    }

    @Override
    public boolean matches(Input input, Level level) {
        ItemStack blueprintStack = input.getItem(GunBenchMenu.SLOT_BLUEPRINT);
        if (RecipeIngredientCompat.isEmpty(blueprint)) {
            if (!blueprintStack.isEmpty()) {
                return false;
            }
        } else if (!blueprint.test(blueprintStack)) {
            return false;
        }

        for (int i = 0; i < recipeItems.size(); i++) {
            ItemStack stackInSlot = input.getItem(i);
            Ingredient requiredIngredient = recipeItems.get(i);
            if (!RecipeIngredientCompat.isEmpty(requiredIngredient) && !requiredIngredient.test(stackInSlot)) {
                return false;
            } else if (RecipeIngredientCompat.isEmpty(requiredIngredient) && !stackInSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Input input) {
        return output.create();
    }

        public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

        public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return output.create();
    }
    @Override
    public String group() {
        return "";
    }


    @Override
    public RecipeSerializer<GunBenchRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<GunBenchRecipe> getType() {
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
        return recipeItems;
    }

    public Ingredient getBlueprint() {
        return blueprint;
    }

    private ItemStackTemplate getResultTemplate() {
        return output;
    }

    public record Input(Container container) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < container.getContainerSize() ? container.getItem(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return container.getContainerSize();
        }
    }

    public static class Type implements RecipeType<GunBenchRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "gun_bench";
    }

    public static class Serializer {
public static final Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "gun_bench");

        private static final MapCodec<Ingredients> INGREDIENTS_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("gun_top_internal_1", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(0)),
                Ingredient.CODEC.optionalFieldOf("gun_top_internal_2", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(1)),
                Ingredient.CODEC.optionalFieldOf("gun_top_barrel_1", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(2)),
                Ingredient.CODEC.optionalFieldOf("gun_top_barrel_2", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(3)),
                Ingredient.CODEC.optionalFieldOf("gun_internal_1", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(4)),
                Ingredient.CODEC.optionalFieldOf("gun_internal_2", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(5)),
                Ingredient.CODEC.optionalFieldOf("gun_barrel_1", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(6)),
                Ingredient.CODEC.optionalFieldOf("gun_barrel_2", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(7)),
                Ingredient.CODEC.optionalFieldOf("gun_grip", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(8)),
                Ingredient.CODEC.optionalFieldOf("gun_magazine", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.recipeItems.get(9)),
                Ingredient.CODEC.optionalFieldOf("blueprint", RecipeIngredientCompat.EMPTY_SLOT).forGetter(ingredients -> ingredients.blueprint)
        ).apply(instance, Ingredients::new));

        private static final MapCodec<GunBenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(Serializer::ingredientsFor),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(GunBenchRecipe::getResultTemplate)
        ).apply(instance, (ingredients, output) -> new GunBenchRecipe(output, ingredients.recipeItems(), ingredients.blueprint())));

        private static final StreamCodec<RegistryFriendlyByteBuf, GunBenchRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );



        public static final RecipeSerializer<GunBenchRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static GunBenchRecipe decode(RegistryFriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, RecipeIngredientCompat.EMPTY_SLOT);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            Ingredient blueprint = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buffer);

            return new GunBenchRecipe(output, inputs, blueprint);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, GunBenchRecipe recipe) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.blueprint);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static Ingredients ingredientsFor(GunBenchRecipe recipe) {
            return new Ingredients(recipe.recipeItems, recipe.blueprint);
        }

        private record Ingredients(NonNullList<Ingredient> recipeItems, Ingredient blueprint) {
            private Ingredients(
                    Ingredient gunTopInternal1,
                    Ingredient gunTopInternal2,
                    Ingredient gunTopBarrel1,
                    Ingredient gunTopBarrel2,
                    Ingredient gunInternal1,
                    Ingredient gunInternal2,
                    Ingredient gunBarrel1,
                    Ingredient gunBarrel2,
                    Ingredient gunGrip,
                    Ingredient gunMagazine,
                    Ingredient blueprint
            ) {
                this(toList(
                        gunTopInternal1,
                        gunTopInternal2,
                        gunTopBarrel1,
                        gunTopBarrel2,
                        gunInternal1,
                        gunInternal2,
                        gunBarrel1,
                        gunBarrel2,
                        gunGrip,
                        gunMagazine
                ), blueprint);
            }

            private static NonNullList<Ingredient> toList(Ingredient... ingredients) {
                NonNullList<Ingredient> list = NonNullList.withSize(10, RecipeIngredientCompat.EMPTY_SLOT);
                for (int i = 0; i < ingredients.length; i++) {
                    list.set(i, ingredients[i]);
                }
                return list;
            }
        }
    }
}
