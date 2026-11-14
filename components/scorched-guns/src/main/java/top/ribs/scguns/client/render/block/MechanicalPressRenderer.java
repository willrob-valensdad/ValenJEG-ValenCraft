package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.block.MechanicalPressBlock;
import top.ribs.scguns.blockentity.MechanicalPressBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.block.state.ScgunsPressRenderState;

@OnlyIn(Dist.CLIENT)
public class MechanicalPressRenderer implements BlockEntityRenderer<MechanicalPressBlockEntity, ScgunsPressRenderState> {

    public MechanicalPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ScgunsPressRenderState createRenderState() {
        return new ScgunsPressRenderState();
    }

    @Override
    public void extractRenderState(MechanicalPressBlockEntity press, ScgunsPressRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(press, state, partialTicks, cameraPosition, breakProgress);
        boolean lit = press.getBlockState().getValue(MechanicalPressBlock.LIT);
        state.pressPosition = press.getPressPosition(partialTicks, lit);
    }

    @Override
    public void submit(ScgunsPressRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0, state.pressPosition, 0.0);
        StandaloneModelRenderUtil.submit(
                SpecialModels.MECHANICAL_PRESS_PRESS.getModel(),
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }
}
