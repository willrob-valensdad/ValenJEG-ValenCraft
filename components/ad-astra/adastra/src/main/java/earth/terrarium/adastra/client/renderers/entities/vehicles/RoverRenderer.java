package earth.terrarium.adastra.client.renderers.entities.vehicles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.vehicles.RoverModel;
import earth.terrarium.adastra.common.entities.vehicles.Rover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class RoverRenderer extends EntityRenderer<Rover, RoverRenderer.State> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/rover/tier_1_rover.png");
    protected final EntityModel<EntityRenderState> model;

    public RoverRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0f;
        this.model = new RoverModel(context.bakeLayer(RoverModel.LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Rover entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot();
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.spin.rover = entity;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.55F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        model.setupAnim(state.spin);
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    public static final class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public final RoverModel.Spin spin = new RoverModel.Spin();
    }

    public static final class ItemRenderer implements SpecialModelRenderer<Void> {
        private EntityModel<EntityRenderState> model;

        @Override
        public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean foil, int outline) {
            if (model == null) {
                model = new RoverModel(Minecraft.getInstance().getEntityModels().bakeLayer(RoverModel.LAYER));
            }
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0.0, -1.501, 0.0);
            EntityRenderState state = new EntityRenderState();
            collector.submitModel(model, state, poseStack, RenderTypes.entityCutoutZOffset(TEXTURE), packedLight, packedOverlay, -1);
            poseStack.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {}

        @Override
        public Void extractArgument(ItemStack stack) {
            return null;
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
            public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return CODEC;
            }

            @Override
            public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
                return new ItemRenderer();
            }
        }
    }
}
