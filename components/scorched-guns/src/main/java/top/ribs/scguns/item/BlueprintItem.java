// Enhanced BlueprintItem.java
package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.compat.RecipeIngredientCompat;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageClearBlueprintRecipe;
import top.ribs.scguns.network.message.S2CMessageOpenBlueprint;
import top.ribs.scguns.util.BlueprintRecipeData;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class BlueprintItem extends Item implements StackCraftingRemainder {

    public BlueprintItem(Properties properties) {
        this(properties, "");
    }

    public BlueprintItem(Properties properties, String blueprintType) {
        super(properties);
    }

    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        if (instance instanceof ItemStack stack) {
            return ItemStackTemplate.fromStack(stack);
        }
        return (ItemStackTemplate) instance;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                clearBlueprintRecipe(hand);
            }
        } else if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            List<RecipeHolder<GunBenchRecipe>> recipes = collectAvailableRecipes(serverLevel, itemstack);
            PacketHandler.getPlayChannel().sendToPlayer(
                    () -> serverPlayer,
                    new S2CMessageOpenBlueprint(hand, recipes)
            );
        }

        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    private static List<RecipeHolder<GunBenchRecipe>> collectAvailableRecipes(ServerLevel level, ItemStack blueprintStack) {
        List<RecipeHolder<GunBenchRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            if (holder.value() instanceof GunBenchRecipe recipe
                    && (blueprintStack.isEmpty()
                    || (!RecipeIngredientCompat.isEmpty(recipe.getBlueprint()) && recipe.getBlueprint().test(blueprintStack)))) {
                recipes.add(new RecipeHolder<>(holder.id(), recipe));
            }
        }
        return recipes;
    }

    @OnlyIn(Dist.CLIENT)
    private void clearBlueprintRecipe(InteractionHand hand) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageClearBlueprintRecipe(hand));
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, TooltipDisplay display, Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, display, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.accept(Component.translatable("item.scguns.blueprint.tooltip.right_click")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));

        if (top.ribs.scguns.fabric.ClientWorlds.current() != null && top.ribs.scguns.fabric.ClientWorlds.current().isClientSide()) {
            String activeRecipeName = BlueprintRecipeData.getActiveRecipeName(pStack, top.ribs.scguns.fabric.ClientWorlds.current());
            if (activeRecipeName != null) {
                pTooltipComponents.accept(Component.translatable("item.scguns.blueprint.tooltip.active_recipe", activeRecipeName)
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));

                pTooltipComponents.accept(Component.translatable("item.scguns.blueprint.tooltip.shift_right_click_clear")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            }
        }
    }
}
