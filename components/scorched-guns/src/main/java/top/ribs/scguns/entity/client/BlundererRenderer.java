package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import top.ribs.scguns.entity.monster.BlundererEntity;

public class BlundererRenderer extends MobRenderer<BlundererEntity, ScgunsBasicMobRenderState, BlundererModel> {
    public BlundererRenderer(EntityRendererProvider.Context context) {
        super(context, new BlundererModel(context.bakeLayer(ModModelLayers.BLUNDERER_LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsBasicMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/blunderer.png");
    }

    @Override
    public ScgunsBasicMobRenderState createRenderState() {
        return new ScgunsBasicMobRenderState();
    }

    @Override
    public void extractRenderState(BlundererEntity entity, ScgunsBasicMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.muzzleFlashVisible = entity.isMuzzleFlashVisible();
    }

    @Override
    protected void scale(ScgunsBasicMobRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }
}
