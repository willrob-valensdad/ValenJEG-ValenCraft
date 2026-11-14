package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.PraetorEntity;

public class PraetorRenderer extends MobRenderer<PraetorEntity, ScgunsMobRenderState, PraetorModel> {
    public PraetorRenderer(EntityRendererProvider.Context context) {
        super(context, new PraetorModel(context.bakeLayer(ModModelLayers.PRAETOR_LAYER)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/praetor.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(PraetorEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.secondPhase = entity.isInSecondPhase();
        state.roaring = entity.isRoaring();
        state.roarTick = entity.getRoarTick();
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
        state.attackVariation = entity.getAttackVariation();
    }

    @Override
    public void submit(ScgunsMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.1F, 1.1F, 1.1F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
