package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.render.entity.state.ScgunsItemRenderState;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.ModItems;

public class TurretProjectileRenderer
        extends EntityRenderer<TurretProjectileEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public TurretProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(TurretProjectileEntity entity, ScgunsItemRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderItem = true;
        state.stack = new ItemStack(ModItems.SHOTGUN_SHELL.get());
        this.itemModelResolver.updateForTopItem(
                state.item,
                state.stack,
                ItemDisplayContext.NONE,
                entity.level(),
                entity,
                entity.getId()
        );
    }

    @Override
    public void submit(ScgunsItemRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(90.0F)));
        state.item.submit(
                poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
