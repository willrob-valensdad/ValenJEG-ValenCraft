package earth.terrarium.adastra.client.renderers.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.render.BlockModelSubmit;
import earth.terrarium.adastra.client.utils.CloseablePoseStack;
import earth.terrarium.adastra.common.blockentities.GlobeBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class GlobeBlockEntityRenderer implements BlockEntityRenderer<GlobeBlockEntity, GlobeBlockEntityRenderer.State> {

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GlobeBlockEntity entity, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        state.yRot = Mth.lerp(partialTick, entity.lastYRot(), entity.yRot());
        state.blockState = entity.getBlockState();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        submitGlobe(state.blockState, state.yRot, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    public static void submitGlobe(BlockState state, float yRot, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        BlockStateModel blockModel = BlockModelSubmit.extra(Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "block/%s_cube".formatted(blockId)));
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
            poseStack.translate(-0.5, 0, -0.5);
            BlockModelSubmit.cutout(collector, poseStack, blockModel, packedLight, packedOverlay);
        }
    }

    public static final class State extends BlockEntityRenderState {
        public float yRot;
        public BlockState blockState;
    }

    public static final class ItemRenderer implements SpecialModelRenderer<BlockState> {
        @Override
        public void submit(BlockState state, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean foil, int outline) {
            float yRot = Util.getMillis() / 20f % 360f;
            submitGlobe(state, yRot, poseStack, collector, packedLight, packedOverlay);
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
