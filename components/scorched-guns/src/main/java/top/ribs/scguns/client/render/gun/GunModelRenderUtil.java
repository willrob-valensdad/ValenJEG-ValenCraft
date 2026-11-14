package top.ribs.scguns.client.render.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 26.2 render helpers for the non-GeckoLib modular weapon models.
 */
public final class GunModelRenderUtil {
    private GunModelRenderUtil() {}

    public static void renderModel(BlockStateModelPart model, ItemStack stack,
                                   PoseStack poseStack, GunRenderContext context,
                                   int light, int overlay) {
        context.submitStandaloneModel(model, stack, ItemStack.EMPTY, poseStack, light, overlay);
    }

    public static void renderModel(BlockStateModelPart model, ItemDisplayContext display,
                                   ItemStack stack, PoseStack poseStack,
                                   GunRenderContext context, int light, int overlay) {
        context.withDisplayContext(display)
                .submitStandaloneModel(model, stack, ItemStack.EMPTY, poseStack, light, overlay);
    }

    public static void renderModel(BlockStateModelPart model, ItemDisplayContext display,
                                   @Nullable Runnable transform,
                                   ItemStack stack, ItemStack parent, PoseStack poseStack,
                                   GunRenderContext context, int light, int overlay) {
        if (transform != null) {
            transform.run();
        }
        context.withDisplayContext(display)
                .submitStandaloneModel(model, stack, parent, poseStack, light, overlay);
    }

    public static void renderModel(ItemStack child, ItemStack parent, PoseStack poseStack,
                                   GunRenderContext context, int light, int overlay) {
        context.submitItemStack(child, poseStack, light, overlay);
    }

    public static void renderModel(ItemStack stack, ItemDisplayContext display,
                                   PoseStack poseStack, GunRenderContext context,
                                   int light, int overlay, @Nullable LivingEntity entity) {
        context.withDisplayContext(display).submitItemStack(stack, poseStack, light, overlay, entity);
    }
}
