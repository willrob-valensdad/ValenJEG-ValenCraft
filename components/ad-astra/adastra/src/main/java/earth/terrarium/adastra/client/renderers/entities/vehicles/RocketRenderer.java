package earth.terrarium.adastra.client.renderers.entities.vehicles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
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

public class RocketRenderer extends EntityRenderer<Rocket, RocketRenderer.State> {
    public static final Identifier TIER_1_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/rocket/tier_1_rocket.png");
    public static final Identifier TIER_2_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/rocket/tier_2_rocket.png");
    public static final Identifier TIER_3_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/rocket/tier_3_rocket.png");
    public static final Identifier TIER_4_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/rocket/tier_4_rocket.png");

    protected final EntityModel<EntityRenderState> model;
    private final Identifier texture;

    public RocketRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, Identifier texture) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new RocketModel(context.bakeLayer(layer));
        this.texture = texture;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Rocket entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        float yaw = entity.getYRot();
        if (!Minecraft.getInstance().isPaused() && (entity.isLaunching() || entity.hasLaunched())) {
            yaw += (float) (entity.level().random.nextGaussian() * 0.3);
        }
        state.yRot = yaw;
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.55F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        model.setupAnim(state);
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(texture), state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return texture;
    }

    public static final class State extends EntityRenderState {
        public float yRot;
        public float xRot;
    }

    public static final class ItemRenderer implements SpecialModelRenderer<Void> {
        private final ModelLayerLocation layer;
        private final Identifier texture;
        private EntityModel<EntityRenderState> model;

        public ItemRenderer(ModelLayerLocation layer, Identifier texture) {
            this.layer = layer;
            this.texture = texture;
        }

        @Override
        public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean foil, int outline) {
            if (model == null) {
                model = new RocketModel(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
            }
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0.0, -1.501, 0.0);
            collector.submitModel(model, new EntityRenderState(), poseStack, RenderTypes.entityCutoutZOffset(texture), packedLight, packedOverlay, -1);
            poseStack.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {}

        @Override
        public Void extractArgument(ItemStack stack) {
            return null;
        }

        public record Unbaked(String tier) implements SpecialModelRenderer.Unbaked<Void> {
            public static final MapCodec<Unbaked> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.STRING.fieldOf("tier").forGetter(Unbaked::tier)
            ).apply(instance, Unbaked::new));

            @Override
            public MapCodec<Unbaked> type() {
                return CODEC;
            }

            @Override
            public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
                return switch (tier) {
                    case "2" -> new ItemRenderer(RocketModel.TIER_2_LAYER, TIER_2_TEXTURE);
                    case "3" -> new ItemRenderer(RocketModel.TIER_3_LAYER, TIER_3_TEXTURE);
                    case "4" -> new ItemRenderer(RocketModel.TIER_4_LAYER, TIER_4_TEXTURE);
                    default -> new ItemRenderer(RocketModel.TIER_1_LAYER, TIER_1_TEXTURE);
                };
            }
        }
    }
}
