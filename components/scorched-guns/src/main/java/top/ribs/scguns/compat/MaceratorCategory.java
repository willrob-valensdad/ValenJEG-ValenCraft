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
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.init.ModBlocks;

public class MaceratorCategory extends AbstractRecipeCategory<RecipeHolder<MaceratorRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "macerating");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/macerator_gui.png");
    public static final RecipeType<RecipeHolder<MaceratorRecipe>> MACERATING_TYPE = RecipeType.createRecipeHolderType(UID);

    private final IDrawable background;

    public MaceratorCategory(IGuiHelper helper) {
        super(
                MACERATING_TYPE,
                Component.translatable("block.scguns.macerator"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MACERATOR.get())),
                93,
                54
        );
        this.background = helper.createDrawable(TEXTURE, 40, 26, 93, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MaceratorRecipe> holder, IFocusGroup focuses) {
        MaceratorRecipe recipe = holder.value();
        int inputBaseX = 4;
        int inputBaseY = 1;
        int inputSpacing = 18;
        for (int i = 0; i < 4 && i < recipe.getIngredients().size(); i++) {
            int row = i / 2;
            int col = i % 2;
            builder.addSlot(RecipeIngredientRole.INPUT,
                            inputBaseX + col * inputSpacing,
                            inputBaseY + row * inputSpacing)
                    .addIngredients(recipe.getIngredients().get(i));
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 1)
                    .addItemStack(recipe.getResultItem(minecraft.level.registryAccess()));
        }
    }

    @Override
    public void draw(RecipeHolder<MaceratorRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MaceratorRecipe> holder, IFocusGroup focuses) {
        int ticks = holder.value().getProcessingTime();
        if (ticks > 0) {
            builder.addText(Component.literal((ticks / 20) + "s"), 24, 9)
                    .setPosition(32, 43)
                    .setColor(0x808080)
                    .setShadow(false);
        }
    }
}
