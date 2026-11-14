package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import top.ribs.scguns.entity.monster.SignalBeaconEntity;

public class SignalBeaconRenderer extends MobRenderer<SignalBeaconEntity, ScgunsBasicMobRenderState, SignalBeaconModel> {
    public SignalBeaconRenderer(EntityRendererProvider.Context context) {
        super(context, new SignalBeaconModel(context.bakeLayer(ModModelLayers.SIGNAL_BEACON_LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsBasicMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/signal_beacon.png");
    }

    @Override
    public ScgunsBasicMobRenderState createRenderState() {
        return new ScgunsBasicMobRenderState();
    }

    @Override
    public void extractRenderState(SignalBeaconEntity entity, ScgunsBasicMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }
}
