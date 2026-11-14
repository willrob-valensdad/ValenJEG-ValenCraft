package top.theillusivec4.curios.api.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
public interface ICurioRenderer {
    <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int light, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context,
            float yRot, float xRot);
    static void translateIfSneaking(PoseStack poseStack, LivingEntity entity) {
        if (entity.isCrouching()) poseStack.translate(0.0D, 0.2D, 0.0D);
    }
}
