package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.blockentity.BasicTurretBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.block.state.ScgunsTurretRenderState;

@OnlyIn(Dist.CLIENT)
public class BasicTurretRenderer implements BlockEntityRenderer<BasicTurretBlockEntity, ScgunsTurretRenderState> {

    public BasicTurretRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ScgunsTurretRenderState createRenderState() {
        return new ScgunsTurretRenderState();
    }

    @Override
    public void extractRenderState(BasicTurretBlockEntity turret, ScgunsTurretRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(turret, state, partialTicks, cameraPosition, breakProgress);

        double previousYaw = turret.getPreviousYaw();
        double yaw = turret.getYaw();
        state.yaw = (float) (previousYaw + Mth.wrapDegrees(yaw - previousYaw) * partialTicks);
        double previousPitch = turret.getPreviousPitch();
        double pitch = turret.getPitch();
        float interpolatedPitch = (float) (previousPitch + (pitch - previousPitch) * partialTicks);
        state.pitch = interpolatedPitch + turret.getRecoilPitchOffset();
    }

    @Override
    public void submit(ScgunsTurretRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.yaw)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(state.pitch)));
        poseStack.translate(-0.5, -1.0, -0.5);
        StandaloneModelRenderUtil.submit(
                SpecialModels.BASIC_TURRET_TOP.getModel(),
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
