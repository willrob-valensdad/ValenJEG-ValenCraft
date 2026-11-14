package earth.terrarium.adastra.client.renderers.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.render.BlockModelSubmit;
import earth.terrarium.adastra.client.utils.CloseablePoseStack;
import earth.terrarium.adastra.common.blockentities.machines.GravityNormalizerBlockEntity;
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
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class GravityNormalizerBlockEntityRenderer implements BlockEntityRenderer<GravityNormalizerBlockEntity, GravityNormalizerBlockEntityRenderer.State> {
    public static final Identifier TOP = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "block/gravity_normalizer_top");
    public static final Identifier TOE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "block/gravity_normalizer_toe");
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GravityNormalizerBlockEntity entity, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        state.blockState = entity.getBlockState();
        state.face = state.blockState.getValue(SidedMachineBlock.FACE);
        state.direction = state.blockState.getValue(SidedMachineBlock.FACING);
        state.animation = Mth.lerp(partialTick, entity.lastAnimation(), entity.animation());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        try (var pose = new CloseablePoseStack(poseStack)) {
            OxygenDistributorBlockEntityRenderer.orient(state.face, state.direction, pose);
            submitMachine(state.blockState, state.animation, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void submitMachine(BlockState state, float animation, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay) {
        BlockStateModel blockModel = BlockModelSubmit.extra(TOP);
        BlockStateModel toeModel = BlockModelSubmit.extra(TOE);
        try (var pose = new CloseablePoseStack(poseStack)) {
            pose.pushPose();
            pose.translate(0.5, 0.7, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(animation));
            pose.mulPose(Axis.YP.rotationDegrees(animation));
            pose.mulPose(Axis.ZP.rotationDegrees(animation));
            float yRot = animation / 1.2f;
            pose.mulPose(Axis.YP.rotationDegrees(yRot));
            pose.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0, SIN_45));
            pose.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0, SIN_45));
            pose.mulPose(Axis.YP.rotationDegrees(yRot));
            pose.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0, SIN_45));
            pose.mulPose(Axis.YP.rotationDegrees(yRot));
            pose.translate(-0.5, -0.7, -0.5);
            BlockModelSubmit.cutout(collector, poseStack, blockModel, packedLight, packedOverlay);
            pose.popPose();
            for (int i = 0; i < 4; i++) {
                poseStack.pushPose();
                pose.translate(0.5, 0, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(90 * i));
                pose.translate(-0.5, 0, -0.5);
                pose.translate(0.27, 0.27, 0.27);
                pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(animation / 50 + i) * 10));
                pose.translate(-0.27, -0.27, -0.27);
                BlockModelSubmit.cutout(collector, poseStack, toeModel, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    public static final class State extends BlockEntityRenderState {
        public BlockState blockState;
        public AttachFace face;
        public Direction direction;
        public float animation;
    }

    public static final class ItemRenderer implements SpecialModelRenderer<BlockState> {
        @Override
        public void submit(BlockState state, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean foil, int outline) {
            float animation = Util.getMillis() / 5f;
            submitMachine(state, animation, poseStack, collector, packedLight, packedOverlay);
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
