package earth.terrarium.adastra.client.fabric;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.AdAstraClient;
import earth.terrarium.adastra.client.ExtraModels;
import earth.terrarium.adastra.client.dimension.ModDimensionSpecialEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AdAstraClientFabric {
    public static final Map<ResourceKey<Level>, ModDimensionSpecialEffects> EFFECTS = new HashMap<>();

    public static void init() {
        AdAstraClient.init();
        onAddReloadListener();
        ClientTickEvents.START_CLIENT_TICK.register(AdAstraClient::clientTick);
        KeyMappingHelper.registerKeyMapping(AdAstraClient.KEY_TOGGLE_SUIT_FLIGHT);
        KeyMappingHelper.registerKeyMapping(AdAstraClient.KEY_OPEN_RADIO);
        AdAstraClient.onRegisterParticles((particle, provider) -> ParticleProviderRegistry.getInstance().register(particle, provider::create));
        AdAstraClient.onRegisterEntityLayers((location, definition) -> ModelLayerRegistry.registerModelLayer(location, definition::get));
        AdAstraClient.onRegisterHud(hud -> HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "overlay"), hud::renderHud));
        ModelLoadingPlugin.register(ctx -> AdAstraClient.onRegisterModels(id -> ctx.addModel(ExtraModels.key(id), SimpleUnbakedExtraModel.blockStateModel(id))));
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> AdAstraClient.renderOverlays(context.poseStack(), Minecraft.getInstance().gameRenderer.mainCamera()));
        AdAstraClient.registerScreens(MenuScreens::register);
        AdAstraClient.registerBlockEntityRenderers(BlockEntityRenderers::register);
        AdAstraClient.registerEntityRenderers(EntityRendererRegistry::register);
        AdAstraClient.registerItemProperties();
        AdAstraClient.registerSpecialModels();
    }

    public static void onAddReloadListener() {
        AdAstraClient.onAddReloadListener((id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id;
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(@NotNull PreparableReloadListener.PreparationBarrier synchronizer, @NotNull ResourceManager manager, @NotNull ProfilerFiller prepareProfiler, @NotNull ProfilerFiller applyProfiler, @NotNull Executor prepareExecutor, @NotNull Executor applyExecutor) {
                return listener.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        }));
    }

    public static void registerDimensionEffects(Map<ResourceKey<Level>, ModDimensionSpecialEffects> renderers) {
        EFFECTS.putAll(renderers);
    }
}
