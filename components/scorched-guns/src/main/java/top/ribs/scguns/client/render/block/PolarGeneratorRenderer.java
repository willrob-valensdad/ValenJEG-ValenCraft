package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.block.PolarGeneratorBlock;
import top.ribs.scguns.blockentity.PolarGeneratorBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.block.state.ScgunsRotationRenderState;

@OnlyIn(Dist.CLIENT)
public class PolarGeneratorRenderer implements BlockEntityRenderer<PolarGeneratorBlockEntity, ScgunsRotationRenderState> {

    public PolarGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ScgunsRotationRenderState createRenderState() {
        return new ScgunsRotationRenderState();
    }

    @Override
    public void extractRenderState(PolarGeneratorBlockEntity generator, ScgunsRotationRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(generator, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = generator.getBlockState().getValue(PolarGeneratorBlock.LIT)
                ? generator.getWheelRotation(partialTicks)
                : 0.0F;
    }

    @Override
    public void submit(ScgunsRotationRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        submitWheel(SpecialModels.POLAR_GENERATOR_WHEEL_1, state.rotation, state, poseStack, collector);
        submitWheel(SpecialModels.POLAR_GENERATOR_WHEEL_2, -state.rotation, state, poseStack, collector);
    }

    private static void submitWheel(SpecialModels model, float rotation, ScgunsRotationRenderState state,
                                    PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(rotation)));
        poseStack.translate(-0.5, -0.5, -0.5);
        StandaloneModelRenderUtil.submit(model.getModel(), poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
