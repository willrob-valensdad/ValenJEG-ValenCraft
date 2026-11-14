package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;

public class SulfurheadGelLayer extends RenderLayer<ScgunsMobRenderState, SulfurheadModel> {
    private static final Identifier GEL_TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/sulfurhead_gel.png");

    public SulfurheadGelLayer(RenderLayerParent<ScgunsMobRenderState, SulfurheadModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       ScgunsMobRenderState state, float yRot, float xRot) {
        int color = ARGB.colorFromFloat(0.4F, 1.0F, 1.0F, 0.3F);
        collector.order(1).submitModel(
                this.getParentModel(), state, poseStack, RenderTypes.entityTranslucent(GEL_TEXTURE),
                lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
    }
}
