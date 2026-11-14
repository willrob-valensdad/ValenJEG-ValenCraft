package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.ViventrumEntity;
import top.ribs.scguns.item.GunItem;

public class ViventrumRenderer extends MobRenderer<ViventrumEntity, ScgunsMobRenderState, ViventrumModel> {
    public ViventrumRenderer(EntityRendererProvider.Context context) {
        super(context, new ViventrumModel(context.bakeLayer(ModModelLayers.VIVENTRUM_LAYER)), 0.3F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/viventrum.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(ViventrumEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.holdingItem = !entity.getMainHandItem().isEmpty();
        state.holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.tame = entity.isTame();
        state.defensive = entity.isDefensive();
        state.partying = entity.isPartying();
    }

    @Override
    public void submit(ScgunsMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.45D, 0.0D);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
