package top.ribs.scguns.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.init.ModBlocks;

public class GunBenchCategory extends AbstractRecipeCategory<RecipeHolder<GunBenchRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "gun_bench");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/gun_bench_gui.png");
    public static final RecipeType<RecipeHolder<GunBenchRecipe>> GUN_BENCH_TYPE = RecipeType.createRecipeHolderType(UID);

    private final IDrawable background;
    private static final int SLOT_OFFSET_X = -22;
    private static final int SLOT_OFFSET_Y = -12;

    public GunBenchCategory(IGuiHelper helper) {
        super(
                GUN_BENCH_TYPE,
                Component.translatable("block.scguns.gun_bench"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.GUN_BENCH.get())),
                141,
                59
        );
        this.background = helper.createDrawable(TEXTURE, 22, 12, 141, 59);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GunBenchRecipe> holder, IFocusGroup focuses) {
        GunBenchRecipe recipe = holder.value();
        int[] slotX = {26, 44, 62, 80, 26, 44, 62, 80, 26, 62, 116};
        int[] slotY = {17, 17, 17, 17, 35, 35, 35, 35, 53, 53, 17};

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size() && i < 10; i++) {
            Ingredient ingredient = ingredients.get(i);
            if (!RecipeIngredientCompat.isEmpty(ingredient)) {
                builder.addSlot(RecipeIngredientRole.INPUT, slotX[i] + SLOT_OFFSET_X, slotY[i] + SLOT_OFFSET_Y)
                        .addIngredients(ingredient);
            }
        }

        Ingredient blueprint = recipe.getBlueprint();
        if (!RecipeIngredientCompat.isEmpty(blueprint)) {
            builder.addSlot(RecipeIngredientRole.INPUT, slotX[10] + SLOT_OFFSET_X, slotY[10] + SLOT_OFFSET_Y)
                    .addIngredients(blueprint);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 140 + SLOT_OFFSET_X, 44 + SLOT_OFFSET_Y)
                    .addItemStack(recipe.getResultItem(minecraft.level.registryAccess()));
        }
    }

    @Override
    public void draw(RecipeHolder<GunBenchRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
    }
}
