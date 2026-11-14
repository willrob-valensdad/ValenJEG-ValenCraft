package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.TraumaUnitEntity;

public class TraumaUnitRenderer extends MobRenderer<TraumaUnitEntity, ScgunsMobRenderState, TraumaUnitModel> {
    public TraumaUnitRenderer(EntityRendererProvider.Context context) {
        super(context, new TraumaUnitModel(context.bakeLayer(ModModelLayers.TRAUMA_UNIT_LAYER)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/trauma_unit.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(TraumaUnitEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
    }
}
