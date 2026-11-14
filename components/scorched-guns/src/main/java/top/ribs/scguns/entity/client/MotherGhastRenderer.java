package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.MotherGhastEntity;

public class MotherGhastRenderer extends MobRenderer<MotherGhastEntity, ScgunsMobRenderState, MotherGhastModel> {
    public MotherGhastRenderer(EntityRendererProvider.Context context) {
        super(context, new MotherGhastModel(context.bakeLayer(ModModelLayers.MOTHER_GHAST_LAYER)), 3.5F);
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/mother_ghast.png");
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(MotherGhastEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.turretFlashVisible = entity.isTurretFlashVisible();
        state.hasAimTarget = false;
        LivingEntity target = entity.getTarget();
        if (target == null) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isSpectator() && entity.distanceToSqr(player) < 10000.0D) target = player;
        }
        if (target != null) {
            double dx = target.getX() - entity.getX();
            double dz = target.getZ() - entity.getZ();
            double dy = target.getEyeY() - (entity.getY() + entity.getBbHeight());
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
        poseStack.translate(0.0D, 2.0D, 0.0D);
        poseStack.scale(3.5F, 3.5F, 3.5F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
