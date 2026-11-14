package earth.terrarium.adastra.client.render;

import earth.terrarium.adastra.client.ExtraModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class BlockModelSubmit {
    private BlockModelSubmit() {}

    public static BlockStateModel extra(Identifier id) {
        return Minecraft.getInstance().getModelManager().getModel(ExtraModels.key(id));
    }

    public static BlockStateModel block(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    public static void cutout(SubmitNodeCollector collector, com.mojang.blaze3d.vertex.PoseStack pose, BlockStateModel model, int light, int overlay) {
        if (model == null) return;
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(0L), parts);
        collector.submitBlockModel(pose, RenderTypes.cutoutMovingBlock(), parts, new int[]{-1}, light, overlay, 0);
    }
}
