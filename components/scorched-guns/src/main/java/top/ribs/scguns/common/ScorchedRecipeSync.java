package top.ribs.scguns.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.client.screen.LightningBatteryRecipe;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;
import top.ribs.scguns.client.screen.PoweredMaceratorRecipe;
import top.ribs.scguns.client.screen.PoweredMechanicalPressRecipe;

/**
 * Requests the complete Scorched Guns custom recipe objects from the server.
 *
 * Minecraft 26.2 no longer synchronizes every RecipeHolder to the logical client.
 * JEI needs the full custom recipe objects, so NeoForge's recipe-sync API is used
 * for the six Scorched Guns machine recipe types.
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public final class ScorchedRecipeSync {
    private ScorchedRecipeSync() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(
                GunBenchRecipe.Type.INSTANCE,
                MaceratorRecipe.Type.INSTANCE,
                PoweredMaceratorRecipe.Type.INSTANCE,
                MechanicalPressRecipe.Type.INSTANCE,
                PoweredMechanicalPressRecipe.Type.INSTANCE,
                LightningBatteryRecipe.Type.INSTANCE
        );
    }
}
