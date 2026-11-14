package earth.terrarium.adastra.client.renderers.entities.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.mobs.SulfurCreeperModel;
import earth.terrarium.adastra.client.renderers.entities.mobs.features.SulfurCreeperChargeFeatureRenderer;
import earth.terrarium.adastra.common.entities.mob.SulfurCreeper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class SulfurCreeperRenderer extends MobRenderer<SulfurCreeper, CreeperRenderState, SulfurCreeperModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/sulfur_creeper.png");

    public SulfurCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new SulfurCreeperModel(context.bakeLayer(SulfurCreeperModel.LAYER_LOCATION)), 0.7f);
        this.addLayer(new SulfurCreeperChargeFeatureRenderer(this, context.getModelSet()));
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public void extractRenderState(SulfurCreeper entity, CreeperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.swelling = entity.getSwelling(partialTick);
        state.isPowered = entity.isPowered();
    }

    @Override
    protected void scale(CreeperRenderState state, PoseStack poseStack) {
        float g = state.swelling;
        float h = 1.0f + Mth.sin(g * 100.0f) * g * 0.01f;
        g = Mth.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        poseStack.scale(i, j, i);
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperRenderState state) {
        float g = state.swelling;
        if ((int) (g * 10.0f) % 2 == 0) {
            return 0.0f;
        }
        return Mth.clamp(g, 0.5f, 1.0f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
