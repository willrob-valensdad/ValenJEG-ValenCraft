package top.ribs.scguns.client.render.gun;

import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.render.ModelQuadCache;

/**
 * 26.2 submission context for Scorched Guns' modular weapon model pieces.
 *
 * The old port threaded an immediate buffer source through every weapon model.
 * Minecraft 26.2 extracts render state and submits render nodes instead.
 */
public final class GunRenderContext {
    private final SubmitNodeCollector collector;
    private final ItemDisplayContext displayContext;
    private final int outlineColor;

    public GunRenderContext(SubmitNodeCollector collector, ItemDisplayContext displayContext) {
        this(collector, displayContext, 0);
    }

    public GunRenderContext(SubmitNodeCollector collector, ItemDisplayContext displayContext, int outlineColor) {
        this.collector = collector;
        this.displayContext = displayContext;
        this.outlineColor = outlineColor;
    }

    public SubmitNodeCollector collector() {
        return this.collector;
    }

    public ItemDisplayContext displayContext() {
        return this.displayContext;
    }

    public int outlineColor() {
        return this.outlineColor;
    }

    public GunRenderContext withDisplayContext(ItemDisplayContext displayContext) {
        if (displayContext == this.displayContext) {
            return this;
        }
        return new GunRenderContext(this.collector, displayContext, this.outlineColor);
    }

    public void submitStandaloneModel(BlockStateModelPart model, ItemStack stack, ItemStack parent,
                                      PoseStack poseStack, int light, int overlay) {
        if (model == null) {
            return;
        }

        List<BakedQuad> quads = ModelQuadCache.getQuads(model);

        ItemStackRenderState.FoilType foil =
                stack.hasFoil() || (!parent.isEmpty() && parent.hasFoil())
                        ? ItemStackRenderState.FoilType.STANDARD
                        : ItemStackRenderState.FoilType.NONE;

        this.collector.submitItem(
                poseStack,
                this.displayContext,
                light,
                overlay,
                this.outlineColor,
                ModelQuadCache.noTints(),
                ItemQuads.split(quads),
                foil
        );
    }

    public void submitItemStack(ItemStack stack, PoseStack poseStack, int light, int overlay) {
        submitItemStack(stack, poseStack, light, overlay, null);
    }

    public void submitItemStack(ItemStack stack, PoseStack poseStack, int light, int overlay, LivingEntity entity) {
        if (stack.isEmpty()) {
            return;
        }

        // ItemStackRenderState is intentionally kept per submission: the 26.2
        // collector may retain it for the current frame, so pooling it here
        // would risk cross-item state corruption.
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        if (entity != null) {
            minecraft.getItemModelResolver().updateForLiving(state, stack, this.displayContext, entity);
        } else {
            minecraft.getItemModelResolver().updateForTopItem(
                    state,
                    stack,
                    this.displayContext,
                    minecraft.level,
                    null,
                    0
            );
        }
        state.submit(poseStack, this.collector, light, overlay, this.outlineColor);
    }
}
