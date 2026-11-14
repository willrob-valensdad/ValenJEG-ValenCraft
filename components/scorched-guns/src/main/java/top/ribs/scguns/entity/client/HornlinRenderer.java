package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.HornlinEntity;

public class HornlinRenderer extends MobRenderer<HornlinEntity, ScgunsMobRenderState, HornlinModel> {
    public HornlinRenderer(EntityRendererProvider.Context context) {
        super(context, new HornlinModel(context.bakeLayer(ModModelLayers.HORNLIN_LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/hornlin.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(HornlinEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.muzzleFlashVisible = entity.isMuzzleFlashVisible();
        state.lookYaw = state.yRot;
        state.lookPitch = state.xRot;
        if ((entity.isEatingGold() || entity.isPreparingToEat()) && entity.getTargetGoldItem() != null) {
            double dx = entity.getTargetGoldItem().getX() - entity.getX();
            double dz = entity.getTargetGoldItem().getZ() - entity.getZ();
            double dy = entity.getTargetGoldItem().getY() - entity.getEyeY();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float goldYaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float goldPitch = (float)(Math.atan2(dy, horizontal) * 180.0 / Math.PI);
            state.lookYaw = goldYaw - entity.getYRot();
            state.lookPitch = -goldPitch;
            if (entity.isEatingGold()) {
                state.lookPitch += (float)Math.sin(state.ageInTicks * 0.8F) * 0.15F * (180.0F / (float)Math.PI);
                state.lookYaw += (float)Math.cos(state.ageInTicks * 0.56F) * 0.05F * (180.0F / (float)Math.PI);
            }
        }
    }
}
