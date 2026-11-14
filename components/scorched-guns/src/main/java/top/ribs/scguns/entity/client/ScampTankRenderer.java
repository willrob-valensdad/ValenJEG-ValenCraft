package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.ScampTankEntity;

public class ScampTankRenderer extends MobRenderer<ScampTankEntity, ScgunsMobRenderState, ScampTankModel> {
    public ScampTankRenderer(EntityRendererProvider.Context context) {
        super(context, new ScampTankModel(context.bakeLayer(ModModelLayers.SCAMP_TANK_LAYER)), 2.8F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/scamp_tank.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(ScampTankEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.secondPhase = entity.isInSecondPhase();
        state.charging = entity.isCharging();
        state.mainTurretFlashVisible = entity.isMainTurretFlashVisible();
        state.machineGunFlashVisible = entity.isMachineGunFlashVisible();
        state.hasAimTarget = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && entity.distanceToSqr(player) <= 45.0D * 45.0D) {
            double dx = player.getX() - entity.getX();
            double dz = player.getZ() - entity.getZ();
            double dy = player.getEyeY() - (entity.getY() + entity.getEyeHeight());
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz)) - entity.getYRot();
            while (yaw > 180.0F) yaw -= 360.0F;
            while (yaw < -180.0F) yaw += 360.0F;
            state.aimYaw = yaw;
            state.aimPitch = Mth.clamp((float)(-Math.toDegrees(Math.atan2(dy, horizontal))), -30.0F, 60.0F);
            state.hasAimTarget = true;
        }
    }

    @Override
    public void submit(ScgunsMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
