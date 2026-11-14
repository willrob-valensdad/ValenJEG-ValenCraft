package top.ribs.scguns.client.render.gun.animated;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.render.gun.GunRenderContext;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * GeckoLib 5.5 attachment submission helper.
 *
 * The 1.20 renderer re-rendered the attachment GeoModel through an immediate
 * buffer source. 26.2 records the same static attachment geometry through
 * SubmitNodeCollector. If an attachment has no Geo model, its normal item model
 * is submitted as a safe native fallback.
 */
public final class AttachmentRenderer {
    private final GeoRenderer<AnimatedGunItem, GeoItemRenderer.RenderData, GeoRenderState> renderer;

    public AttachmentRenderer(GeoRenderer<AnimatedGunItem, GeoItemRenderer.RenderData, GeoRenderState> renderer) {
        this.renderer = renderer;
    }

    public void submit(RenderPassInfo<GeoRenderState> renderInfo,
                       ItemStack attachmentStack,
                       SubmitNodeCollector collector,
                       int packedLight,
                       int packedOverlay) {
        if (attachmentStack == null || attachmentStack.isEmpty()) {
            return;
        }

        Identifier attachmentId = BuiltInRegistries.ITEM.getKey(attachmentStack.getItem());
        if (attachmentId == null) {
            return;
        }

        Identifier modelResource = Identifier.fromNamespaceAndPath(
                attachmentId.getNamespace(),
                "item/attachment/" + attachmentId.getPath()
        );
        Identifier textureResource = Identifier.fromNamespaceAndPath(
                attachmentId.getNamespace(),
                "textures/animated/attachment/" + attachmentId.getPath() + ".png"
        );

        BakedGeoModel bakedModel = this.renderer.getGeoModel().getBakedModel(modelResource);
        if (bakedModel.isMissingno()) {
            new GunRenderContext(collector, ItemDisplayContext.NONE)
                    .submitItemStack(attachmentStack, renderInfo.poseStack(), packedLight, packedOverlay,
                            Minecraft.getInstance().player);
            return;
        }

        RenderType renderType = RenderTypes.entityTranslucent(textureResource);
        collector.submitCustomGeometry(renderInfo.poseStack(), renderType, (pose, buffer) -> {
            PoseStack poseStack = renderInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            renderInfo.renderPosed(() -> bakedModel.render(
                    renderInfo,
                    buffer,
                    packedLight,
                    packedOverlay,
                    renderInfo.renderColor()
            ));
            poseStack.popPose();
        });
    }
}
