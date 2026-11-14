package top.ribs.scguns.client.render.gun.model;

import top.ribs.scguns.client.render.gun.GunModelRenderUtil;

import top.ribs.scguns.client.render.gun.GunRenderContext;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class SimpleModel implements IOverrideModel
{
    protected final Supplier<BlockStateModelPart> modelSupplier;

    public SimpleModel(Supplier<BlockStateModelPart> modelSupplier)
    {
        this.modelSupplier = modelSupplier;
    }

    @Override
    public void render(float partialTicks, ItemDisplayContext display, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, GunRenderContext buffer, int light, int overlay)
    {
        GunModelRenderUtil.renderModel(this.modelSupplier.get(), display, null, stack, parent, poseStack, buffer, light, overlay);
    }
}
