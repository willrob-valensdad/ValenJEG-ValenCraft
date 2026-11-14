package com.atsuishio.superbwarfare.client.renderer.entity

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.entity.misc.CatapultShuttleEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier

class CatapultShuttleRenderer(renderManager: EntityRendererProvider.Context) :
    EntityRenderer<CatapultShuttleEntity>(renderManager) {
    init {
        this.shadowRadius = 0f
    }

    override fun render(
        entityIn: CatapultShuttleEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        bufferIn: MultiBufferSource,
        packedLightIn: Int
    ) {
        val instance = entityIn.modelInstance ?: return

        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180f))

        val renderType = RenderTypes.entityTranslucent(getTextureLocation(entityIn))
        val vertexConsumer = bufferIn.getBuffer(renderType)

        instance.renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLightIn,
            OverlayTexture.NO_OVERLAY
        )

        poseStack.popPose()
    }

    public override fun shouldShowName(animatable: CatapultShuttleEntity): Boolean {
        return false
    }

    override fun getTextureLocation(pEntity: CatapultShuttleEntity): Identifier {
        return TEXTURE
    }

    companion object {
        val TEXTURE = loc("textures/bedrock/entity/catapult_shuttle.png")
    }
}
