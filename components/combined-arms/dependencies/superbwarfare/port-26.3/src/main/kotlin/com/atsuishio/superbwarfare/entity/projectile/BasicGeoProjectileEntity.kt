package com.atsuishio.superbwarfare.entity.projectile

import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance
import com.atsuishio.superbwarfare.resource.model.ProjectileModelReloadListener
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

interface BasicGeoProjectileEntity {
    @Deprecated("Model location is auto loaded now")
    fun getModel(): Identifier = loc("projectile/projectile")

    @Deprecated("Animation location is auto loaded now")
    fun getAnimation(): Identifier? = null

    fun getAnimationInstance(): BasicProjectileAnimationInstance<*>? = null

    fun getEmissiveTexture(): Identifier? = null

    fun getHiddenTicks(): Int = 0

    fun getFlareHiddenTicks(): Int = 3

    fun getModelInstance(): BakedModelInstance? {
        val entity = this as Entity
        val (_, namespace, id) = entity.type.descriptionId.split(".")
        return ProjectileModelReloadListener.getModel(
            Identifier.fromNamespaceAndPath(namespace, "models/bedrock/projectile/$id.geo.json")
        )?.createInstance()
    }
}
