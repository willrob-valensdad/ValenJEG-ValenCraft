package earth.terrarium.adastra.client.renderers.entities.mobs.features;

import earth.terrarium.adastra.client.models.entities.mobs.SulfurCreeperModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class SulfurCreeperChargeFeatureRenderer extends EnergySwirlLayer<CreeperRenderState, SulfurCreeperModel> {

    private static final Identifier SKIN = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    private final SulfurCreeperModel model;

    public SulfurCreeperChargeFeatureRenderer(RenderLayerParent<CreeperRenderState, SulfurCreeperModel> context, EntityModelSet loader) {
        super(context);
        this.model = new SulfurCreeperModel(loader.bakeLayer(SulfurCreeperModel.LAYER_LOCATION));
    }

    @Override
    protected boolean isPowered(CreeperRenderState state) {
        return state.isPowered;
    }

    @Override
    protected float xOffset(float partialAge) {
        return partialAge * 0.01f;
    }

    @Override
    protected Identifier getTextureLocation() {
        return SKIN;
    }

    @Override
    protected SulfurCreeperModel model() {
        return this.model;
    }
}
