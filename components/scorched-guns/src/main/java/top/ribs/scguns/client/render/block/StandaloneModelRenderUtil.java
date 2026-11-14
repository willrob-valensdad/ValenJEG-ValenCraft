package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import top.ribs.scguns.client.render.ModelQuadCache;

/**
 * Submission helper for Scorched Guns standalone JSON model parts.
 *
 * Hotfix 8 migrated these models from BakedModel to BlockStateModelPart.
 * Hotfix 19 reuses that representation from BlockEntityRenderer submit passes.
 */
public final class StandaloneModelRenderUtil {
    private StandaloneModelRenderUtil() {}

    public static void submit(BlockStateModelPart model, PoseStack poseStack,
                              SubmitNodeCollector collector, int light, int overlay) {
        if (model == null) {
            return;
        }

        List<BakedQuad> quads = ModelQuadCache.getQuads(model);

        collector.submitItem(
                poseStack,
                ItemDisplayContext.NONE,
                light,
                overlay,
                0,
                ModelQuadCache.noTints(),
                ItemQuads.split(quads),
                ItemStackRenderState.FoilType.NONE
        );
    }
}
