package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import top.ribs.scguns.entity.monster.HiveEntity;

public class HiveRenderer extends MobRenderer<HiveEntity, ScgunsBasicMobRenderState, HiveModel> {
    public HiveRenderer(EntityRendererProvider.Context context) {
        super(context, new HiveModel(context.bakeLayer(ModModelLayers.HIVE_LAYER)), 0.6F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsBasicMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/hive.png");
    }

    @Override
    public ScgunsBasicMobRenderState createRenderState() {
        return new ScgunsBasicMobRenderState();
    }

    @Override
    public void extractRenderState(HiveEntity entity, ScgunsBasicMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }

    @Override
    protected void scale(ScgunsBasicMobRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }
}
