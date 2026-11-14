package top.ribs.scguns.fabric.mixin;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.fabric.ScorchedFabric;
@Mixin(LayerDefinitions.class)
public class ScorchedLayerDefinitionsMixin {
    @Inject(method = "createRoots", at = @At("RETURN"), cancellable = true)
    private static void scguns$layers(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> callback) {
        ScorchedFabric.MOD_BUS.post(new EntityRenderersEvent.RegisterLayerDefinitions());
        if (EntityRenderersEvent.RegisterLayerDefinitions.LAYERS.isEmpty()) return;
        ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder = ImmutableMap.builder();
        builder.putAll(callback.getReturnValue());
        EntityRenderersEvent.RegisterLayerDefinitions.LAYERS.forEach((location, supplier) -> builder.put(location, supplier.get()));
        callback.setReturnValue(builder.buildOrThrow());
    }
}
