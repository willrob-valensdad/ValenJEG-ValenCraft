package top.ribs.scguns.client;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.client.screen.LightningBatteryRecipe;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;
import top.ribs.scguns.client.screen.PoweredMaceratorRecipe;
import top.ribs.scguns.client.screen.PoweredMechanicalPressRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Client-side cache of the complete custom recipe objects synced by NeoForge.
 * JEI 30 can then register exactly the same recipe categories that Scorched Guns
 * exposed on 1.20.1, without relying on the removed client RecipeManager access.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class ScorchedClientRecipes {
    private static final List<RecipeHolder<GunBenchRecipe>> GUN_BENCH = new ArrayList<>();
    private static final List<RecipeHolder<MaceratorRecipe>> MACERATOR = new ArrayList<>();
    private static final List<RecipeHolder<PoweredMaceratorRecipe>> POWERED_MACERATOR = new ArrayList<>();
    private static final List<RecipeHolder<MechanicalPressRecipe>> MECHANICAL_PRESS = new ArrayList<>();
    private static final List<RecipeHolder<PoweredMechanicalPressRecipe>> POWERED_MECHANICAL_PRESS = new ArrayList<>();
    private static final List<RecipeHolder<LightningBatteryRecipe>> LIGHTNING_BATTERY = new ArrayList<>();

    private ScorchedClientRecipes() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        RecipeMap recipeMap = event.getRecipeMap();

        replace(GUN_BENCH, recipeMap.byType(GunBenchRecipe.Type.INSTANCE));
        replace(MACERATOR, recipeMap.byType(MaceratorRecipe.Type.INSTANCE));
        replace(POWERED_MACERATOR, recipeMap.byType(PoweredMaceratorRecipe.Type.INSTANCE));
        replace(MECHANICAL_PRESS, recipeMap.byType(MechanicalPressRecipe.Type.INSTANCE));
        replace(POWERED_MECHANICAL_PRESS, recipeMap.byType(PoweredMechanicalPressRecipe.Type.INSTANCE));
        replace(LIGHTNING_BATTERY, recipeMap.byType(LightningBatteryRecipe.Type.INSTANCE));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> void replace(
            List<RecipeHolder<T>> target,
            Collection<RecipeHolder<T>> source
    ) {
        target.clear();
        target.addAll(source);
    }

    private static void clear() {
        GUN_BENCH.clear();
        MACERATOR.clear();
        POWERED_MACERATOR.clear();
        MECHANICAL_PRESS.clear();
        POWERED_MECHANICAL_PRESS.clear();
        LIGHTNING_BATTERY.clear();
    }

    public static List<RecipeHolder<GunBenchRecipe>> gunBench() {
        return List.copyOf(GUN_BENCH);
    }

    public static List<RecipeHolder<MaceratorRecipe>> macerator() {
        return List.copyOf(MACERATOR);
    }

    public static List<RecipeHolder<PoweredMaceratorRecipe>> poweredMacerator() {
        return List.copyOf(POWERED_MACERATOR);
    }

    public static List<RecipeHolder<MechanicalPressRecipe>> mechanicalPress() {
        return List.copyOf(MECHANICAL_PRESS);
    }

    public static List<RecipeHolder<PoweredMechanicalPressRecipe>> poweredMechanicalPress() {
        return List.copyOf(POWERED_MECHANICAL_PRESS);
    }

    public static List<RecipeHolder<LightningBatteryRecipe>> lightningBattery() {
        return List.copyOf(LIGHTNING_BATTERY);
    }
}
