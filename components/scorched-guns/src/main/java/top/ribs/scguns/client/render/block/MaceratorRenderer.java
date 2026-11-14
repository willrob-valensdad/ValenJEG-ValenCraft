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
import top.ribs.scguns.block.MaceratorBlock;
import top.ribs.scguns.blockentity.MaceratorBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.block.state.ScgunsRotationRenderState;

@OnlyIn(Dist.CLIENT)
public class MaceratorRenderer implements BlockEntityRenderer<MaceratorBlockEntity, ScgunsRotationRenderState> {

    public MaceratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ScgunsRotationRenderState createRenderState() {
        return new ScgunsRotationRenderState();
    }

    @Override
    public void extractRenderState(MaceratorBlockEntity macerator, ScgunsRotationRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(macerator, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = macerator.getBlockState().getValue(MaceratorBlock.LIT)
                ? macerator.getWheelRotation(partialTicks)
                : 0.0F;
    }

    @Override
    public void submit(ScgunsRotationRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        submitWheel(poseStack, collector, state, SpecialModels.MACERATOR_WHEEL_1, 0.1875, 0.5, 0.1875);
        submitWheel(poseStack, collector, state, SpecialModels.MACERATOR_WHEEL_2, 0.8125, 0.5, 0.1875);
        submitWheel(poseStack, collector, state, SpecialModels.MACERATOR_WHEEL_3, 0.8125, 0.5, 0.8125);
        submitWheel(poseStack, collector, state, SpecialModels.MACERATOR_WHEEL_4, 0.1875, 0.5, 0.8125);
    }

    private static void submitWheel(PoseStack poseStack, SubmitNodeCollector collector,
                                    ScgunsRotationRenderState state, SpecialModels model,
                                    double x, double y, double z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.rotation)));
        poseStack.translate(-x, -y, -z);
        StandaloneModelRenderUtil.submit(model.getModel(), poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
