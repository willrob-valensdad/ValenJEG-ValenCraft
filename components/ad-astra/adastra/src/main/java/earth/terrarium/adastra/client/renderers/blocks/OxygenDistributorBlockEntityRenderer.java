package earth.terrarium.adastra.client.renderers.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.render.BlockModelSubmit;
import earth.terrarium.adastra.client.utils.CloseablePoseStack;
import earth.terrarium.adastra.common.blockentities.machines.OxygenDistributorBlockEntity;
import earth.terrarium.adastra.common.blocks.base.SidedMachineBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class OxygenDistributorBlockEntityRenderer implements BlockEntityRenderer<OxygenDistributorBlockEntity, OxygenDistributorBlockEntityRenderer.State> {
    public static final Identifier TOP = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "block/oxygen_distributor_top");

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(OxygenDistributorBlockEntity entity, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        state.blockState = entity.getBlockState();
        state.face = state.blockState.getValue(SidedMachineBlock.FACE);
        state.direction = state.blockState.getValue(SidedMachineBlock.FACING);
        state.yRot = Mth.lerp(partialTick, entity.lastYRot(), entity.yRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        try (var pose = new CloseablePoseStack(poseStack)) {
            orient(state.face, state.direction, pose);
            submitTop(state.blockState, state.yRot, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void submitTop(BlockState state, float yRot, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay) {
        BlockStateModel blockModel = BlockModelSubmit.extra(TOP);
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
            poseStack.translate(-0.5, 0, -0.5);
            BlockModelSubmit.cutout(collector, poseStack, blockModel, packedLight, packedOverlay);
        }
    }

    static void orient(AttachFace face, Direction direction, PoseStack pose) {
        if (face == AttachFace.CEILING) {
            pose.translate(0, 1, 1);
            pose.mulPose(Axis.XP.rotationDegrees(180));
        } else if (face == AttachFace.FLOOR) {
            pose.mulPose(Axis.XP.rotationDegrees(0));
        } else {
            if (direction == Direction.NORTH) {
                pose.translate(0, 0, 1);
            } else if (direction == Direction.SOUTH) {
                pose.translate(1, 0, 0);
            } else if (direction == Direction.WEST) {
                pose.translate(1, 0, 1);
            }
            rotateBlock(direction, pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
        }
    }

    public static void rotateBlock(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
        }
    }

    public static final class State extends BlockEntityRenderState {
        public BlockState blockState;
        public AttachFace face;
        public Direction direction;
        public float yRot;
    }

    public static final class ItemRenderer implements SpecialModelRenderer<BlockState> {
        @Override
        public void submit(BlockState state, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean foil, int outline) {
            float yRot = Util.getMillis() / 5f % 360f;
            submitTop(state, yRot, poseStack, collector, packedLight, packedOverlay);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {}

        @Override
        public BlockState extractArgument(ItemStack stack) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Block block = BuiltInRegistries.BLOCK.get(id).orElse(net.minecraft.world.level.block.Blocks.AIR);
            return block.defaultBlockState();
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked<BlockState> {
            public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return CODEC;
            }

            @Override
            public SpecialModelRenderer<BlockState> bake(SpecialModelRenderer.BakingContext context) {
                return new ItemRenderer();
            }
        }
    }
}
