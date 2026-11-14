package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.entity.block.PrimedPowderKeg;
import top.ribs.scguns.init.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class PowderKegRenderer extends EntityRenderer<PrimedPowderKeg, TntRenderState> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public PowderKegRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    @Override
    public void extractRenderState(PrimedPowderKeg entity, TntRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fuseRemainingInTicks = entity.getFuse() - partialTicks + 1.0F;
        this.blockModelResolver.update(
                state.blockState,
                ModBlocks.POWDER_KEG.get().defaultBlockState(),
                BLOCK_DISPLAY_CONTEXT
        );
    }

    @Override
    public void submit(TntRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);

        float fuse = state.fuseRemainingInTicks;
        if (fuse < 10.0F) {
            float scale = 1.0F - fuse / 10.0F;
            scale = Mth.clamp(scale, 0.0F, 1.0F);
            scale *= scale;
            scale *= scale;
            float scaleFactor = 1.0F + scale * 0.3F;
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        }

        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(-90.0F)));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(90.0F)));

        TntMinecartRenderer.submitWhiteSolidBlock(
                state.blockState,
                poseStack,
                collector,
                state.lightCoords,
                (int)fuse / 5 % 2 == 0,
                state.outlineColor
        );

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
