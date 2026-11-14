package top.ribs.scguns.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;
import top.ribs.scguns.init.ModBlocks;

public class MechanicalPressCategory extends AbstractRecipeCategory<RecipeHolder<MechanicalPressRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "mechanical_pressing");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/mechanical_press_gui.png");
    public static final RecipeType<RecipeHolder<MechanicalPressRecipe>> MECHANICAL_PRESS_TYPE = RecipeType.createRecipeHolderType(UID);

    private final IDrawable background;

    public MechanicalPressCategory(IGuiHelper helper) {
        super(
                MECHANICAL_PRESS_TYPE,
                Component.translatable("block.scguns.mechanical_press"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MECHANICAL_PRESS.get())),
                120,
                54
        );
        this.background = helper.createDrawable(TEXTURE, 20, 26, 120, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MechanicalPressRecipe> holder, IFocusGroup focuses) {
        MechanicalPressRecipe recipe = holder.value();
        int inputBaseX = 24;
        int inputBaseY = 1;
        int inputSpacing = 18;

        if (!recipe.getIngredients().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX - inputSpacing, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(0));
        }
        if (recipe.getIngredients().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(1));
        }
        if (recipe.getIngredients().size() > 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX + inputSpacing, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(2));
        }
        if (!RecipeIngredientCompat.isEmpty(recipe.getMoldItem())) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY + inputSpacing)
                    .addIngredients(recipe.getMoldItem());
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 1)
                    .addItemStack(recipe.getResultItem(minecraft.level.registryAccess()));
        }
    }

    @Override
    public void draw(RecipeHolder<MechanicalPressRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MechanicalPressRecipe> holder, IFocusGroup focuses) {
        int ticks = holder.value().getProcessingTime();
        if (ticks > 0) {
            builder.addText(Component.literal((ticks / 20) + "s"), 24, 9)
                    .setPosition(89, 43)
                    .setColor(0x808080)
                    .setShadow(false);
        }
    }
}
