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
import top.ribs.scguns.entity.monster.SubjugatorEntity;
import top.ribs.scguns.item.GunItem;

public class SubjugatorRenderer extends MobRenderer<SubjugatorEntity, ScgunsMobRenderState, SubjugatorModel> {
    public SubjugatorRenderer(EntityRendererProvider.Context context) {
        super(context, new SubjugatorModel(context.bakeLayer(ModModelLayers.SUBJUGATOR_LAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/subjugator.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(SubjugatorEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.holdingItem = !entity.getMainHandItem().isEmpty();
        state.holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
    }

    @Override
    public void submit(ScgunsMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.15F, 1.15F, 1.15F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
