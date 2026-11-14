/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.client.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class EntityRenderersEvent {
    private EntityRenderersEvent() {}

    public static class RegisterRenderers {
        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
            EntityRenderers.register(type, provider);
        }
    }

    public static class RegisterLayerDefinitions {
        public static final Map<ModelLayerLocation, Supplier<LayerDefinition>> LAYERS = new LinkedHashMap<>();
        public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
            LAYERS.put(location, supplier);
        }
    }
}
