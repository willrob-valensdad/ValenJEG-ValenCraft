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
import top.ribs.scguns.client.screen.LightningBatteryRecipe;
import top.ribs.scguns.init.ModBlocks;

public class LightningBatteryCategory extends AbstractRecipeCategory<RecipeHolder<LightningBatteryRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "lightning_battery");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/lightning_battery_gui.png");
    public static final RecipeType<RecipeHolder<LightningBatteryRecipe>> LIGHTNING_BATTERY_TYPE = RecipeType.createRecipeHolderType(UID);

    private final IDrawable background;

    public LightningBatteryCategory(IGuiHelper helper) {
        super(
                LIGHTNING_BATTERY_TYPE,
                Component.translatable("block.scguns.lightning_battery"),
                helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.LIGHTNING_BATTERY.get())),
                96,
                54
        );
        this.background = helper.createDrawable(TEXTURE, 41, 16, 96, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LightningBatteryRecipe> holder, IFocusGroup focuses) {
        LightningBatteryRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 19)
                .addIngredients(recipe.getInput());

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 19)
                    .addItemStack(recipe.getResultItem(minecraft.level.registryAccess()));
        }
    }

    @Override
    public void draw(RecipeHolder<LightningBatteryRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<LightningBatteryRecipe> holder, IFocusGroup focuses) {
        LightningBatteryRecipe recipe = holder.value();
        if (recipe.getProcessingTime() > 0) {
            builder.addText(Component.literal((recipe.getProcessingTime() / 20) + "s"), 24, 9)
                    .setPosition(38, 43)
                    .setColor(0x808080)
                    .setShadow(false);
        }
        builder.addText(Component.translatable("tooltip.lightning_battery.energy", recipe.getEnergyUse()), 55, 9)
                .setPosition(2, 1)
                .setColor(0x808080)
                .setShadow(false);
    }
}
