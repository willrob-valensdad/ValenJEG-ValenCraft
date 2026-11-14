package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.block.GunShelfBlock;
import top.ribs.scguns.blockentity.GunShelfBlockEntity;
import top.ribs.scguns.client.render.block.state.ScgunsGunShelfRenderState;

public class GunShelfRenderer implements BlockEntityRenderer<GunShelfBlockEntity, ScgunsGunShelfRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GunShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public ScgunsGunShelfRenderState createRenderState() {
        return new ScgunsGunShelfRenderState();
    }

    @Override
    public void extractRenderState(GunShelfBlockEntity shelf, ScgunsGunShelfRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(shelf, state, partialTicks, cameraPosition, breakProgress);
        state.facing = shelf.getBlockState().getValue(GunShelfBlock.FACING);

        ItemStack displayedItem = shelf.getDisplayedItem();
        state.hasItem = displayedItem != null && !displayedItem.isEmpty();
        if (state.hasItem) {
            this.itemModelResolver.updateForTopItem(
                    state.item,
                    displayedItem,
                    ItemDisplayContext.FIXED,
                    shelf.getLevel(),
                    null,
                    0
            );
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(ScgunsGunShelfRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasItem) {
            return;
        }

        poseStack.pushPose();
        Direction facing = state.facing;
        switch (facing) {
            case NORTH -> poseStack.translate(0.5, 0.4, 0.8F);
            case SOUTH -> poseStack.translate(0.5, 0.4, 0.2);
            case EAST -> poseStack.translate(0.2, 0.4, 0.5);
            case WEST -> poseStack.translate(0.8F, 0.4, 0.5);
            default -> poseStack.translate(0.5, 0.4, 0.5);
        }

        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(facing.toYRot())));
        poseStack.scale(0.55F, 0.55F, 0.55F);
        state.item.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }
}
