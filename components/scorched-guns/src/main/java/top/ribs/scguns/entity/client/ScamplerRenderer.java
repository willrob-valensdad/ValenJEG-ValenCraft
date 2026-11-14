package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import top.ribs.scguns.entity.monster.ScamplerEntity;

public class ScamplerRenderer extends MobRenderer<ScamplerEntity, ScgunsBasicMobRenderState, ScamplerModel> {
    public ScamplerRenderer(EntityRendererProvider.Context context) {
        super(context, new ScamplerModel(context.bakeLayer(ModModelLayers.SCAMPLER_LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsBasicMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/scampler.png");
    }

    @Override
    public ScgunsBasicMobRenderState createRenderState() {
        return new ScgunsBasicMobRenderState();
    }

    @Override
    public void extractRenderState(ScamplerEntity entity, ScgunsBasicMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }

    @Override
    protected void scale(ScgunsBasicMobRenderState state, PoseStack poseStack) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
    }
}
