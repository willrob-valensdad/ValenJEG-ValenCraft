package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;

public class ZombifiedHornlinRenderer extends MobRenderer<ZombifiedHornlinEntity, ScgunsBasicMobRenderState, ZombifiedHornlinModel> {
    public ZombifiedHornlinRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombifiedHornlinModel(context.bakeLayer(ModModelLayers.ZOMBIFIED_HORNLIN_LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsBasicMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/zombified_hornlin.png");
    }

    @Override
    public ScgunsBasicMobRenderState createRenderState() {
        return new ScgunsBasicMobRenderState();
    }

    @Override
    public void extractRenderState(ZombifiedHornlinEntity entity, ScgunsBasicMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.muzzleFlashVisible = entity.isMuzzleFlashVisible();
    }
}
