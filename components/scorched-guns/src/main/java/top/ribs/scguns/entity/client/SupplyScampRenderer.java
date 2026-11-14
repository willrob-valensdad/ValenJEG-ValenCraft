package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.SupplyScampEntity;

public class SupplyScampRenderer extends MobRenderer<SupplyScampEntity, ScgunsMobRenderState, SupplyScampModel> {
    public SupplyScampRenderer(EntityRendererProvider.Context context) {
        super(context, new SupplyScampModel(context.bakeLayer(ModModelLayers.SUPPLY_SCAMP_LAYER)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/supply_scamp.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(SupplyScampEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.panicAnimationState.copyFrom(entity.panicAnimationState);
        state.sitAnimationState.copyFrom(entity.sitAnimationState);
        state.maskColor = entity.getMaskColor();
        state.tame = entity.isTame();
    }
}
