package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;

public class SulfurheadPrimeOverlayLayer extends RenderLayer<ScgunsMobRenderState, SulfurheadModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/sulfurhead.png");

    public SulfurheadPrimeOverlayLayer(RenderLayerParent<ScgunsMobRenderState, SulfurheadModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       ScgunsMobRenderState state, float yRot, float xRot) {
        if (!state.primed) return;
        float swell = state.swelling;
        if ((int)(swell * 10.0F) % 2 == 0 || swell <= 0.01F) return;
        float intensity = Mth.clamp(swell, 0.3F, 0.9F);
        int color = ARGB.colorFromFloat(0.7F, 1.0F, intensity * 0.8F, 0.2F);
        collector.order(2).submitModel(
                this.getParentModel(), state, poseStack, RenderTypes.entityTranslucent(TEXTURE),
                lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), color, null, state.outlineColor);
    }
}
