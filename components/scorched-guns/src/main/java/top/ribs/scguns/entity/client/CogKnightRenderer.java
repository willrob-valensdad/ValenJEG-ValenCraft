package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.CogKnightEntity;

public class CogKnightRenderer extends MobRenderer<CogKnightEntity, ScgunsMobRenderState, CogKnightModel> {
    public CogKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new CogKnightModel(context.bakeLayer(ModModelLayers.COG_KNIGHT_LAYER)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/cog_knight.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(CogKnightEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
        state.charging = entity.isCharging();
    }

    @Override
    public void submit(ScgunsMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.35D, 0.0D);
        poseStack.scale(0.9F, 0.9F, 0.9F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
