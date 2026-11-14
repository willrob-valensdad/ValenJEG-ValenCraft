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
import top.ribs.scguns.entity.monster.AdjudicatorEntity;
import top.ribs.scguns.item.GunItem;

public class AdjudicatorRenderer extends MobRenderer<AdjudicatorEntity, ScgunsMobRenderState, AdjudicatorModel> {
    public AdjudicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdjudicatorModel(context.bakeLayer(ModModelLayers.ADJUDICATOR_LAYER)), 0.6F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/adjudicator.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(AdjudicatorEntity entity, ScgunsMobRenderState state, float partialTicks) {
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
