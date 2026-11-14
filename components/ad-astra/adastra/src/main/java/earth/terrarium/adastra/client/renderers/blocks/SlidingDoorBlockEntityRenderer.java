package earth.terrarium.adastra.client.renderers.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.render.BlockModelSubmit;
import earth.terrarium.adastra.client.utils.CloseablePoseStack;
import earth.terrarium.adastra.common.blockentities.SlidingDoorBlockEntity;
import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;
import earth.terrarium.adastra.common.registry.ModBlocks;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlidingDoorBlockEntityRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity, SlidingDoorBlockEntityRenderer.State> {

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SlidingDoorBlockEntity entity, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        state.blockState = entity.getBlockState();
        state.slide = Mth.lerp(partialTick, entity.lastSlideTicks(), entity.slideTicks()) / 81.0f;
        state.direction = state.blockState.getValue(SlidingDoorBlock.FACING);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        var blockState = state.blockState;
        var direction = state.direction;
        float slide = state.slide;
        BlockStateModel model = BlockModelSubmit.block(blockState);
        boolean flipSecondDoor = ModBlocks.SIMPLE_SLIDING_DOORS.stream().noneMatch(block -> block.get().equals(blockState.getBlock()));
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;
        try (var pose = new CloseablePoseStack(poseStack)) {
            pose.translate(0.5f, 1, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(direction.toYRot()));
            pose.translate(-0.5f, 0, -0.5f);
            pose.translate(slide, 0, 0.0625f);
            if (direction.getAxis() == Direction.Axis.Z) {
                pose.translate(0, 0, 0.6875f);
                if (blockState.is(ModBlocks.REINFORCED_DOOR.get())) {
                    pose.translate(0, 0, -0.3125f);
                }
            }
            BlockModelSubmit.cutout(collector, poseStack, model, light, overlay);
            pose.translate(-slide - slide, 0, 0);
            if (!flipSecondDoor) {
                pose.translate(0.5f, 0, 0.5f);
                pose.mulPose(Axis.YP.rotationDegrees(180));
                pose.translate(-0.5f, 0, -0.5f);
                pose.translate(0, 0, 0.8125f);
                BlockModelSubmit.cutout(collector, poseStack, model, light, overlay);
            } else {
                pose.translate(-1.25f, 0, 0);
                String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
                BlockStateModel flipped = BlockModelSubmit.extra(Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "block/%s_flipped".formatted(blockId)));
                BlockModelSubmit.cutout(collector, poseStack, flipped, light, overlay);
            }
        }
    }

    public static final class State extends BlockEntityRenderState {
        public BlockState blockState;
        public Direction direction;
        public float slide;
    }
}
