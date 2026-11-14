package com.atsuishio.superbwarfare.client.animation.entity

import com.atsuishio.superbwarfare.entity.projectile.BasicGeoProjectileEntity
import com.atsuishio.superbwarfare.resource.model.ProjectileModelReloadListener
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

class BasicProjectileContext<T>(entity: T, location: Identifier, val loop: Boolean) :
    BasicEntityContext<T>(entity, location) where T : Entity, T : BasicGeoProjectileEntity {
    override fun init(location: Identifier) {
        val ani = ProjectileModelReloadListener.getAnimation(location)
        for (entry in ani!!) {
            animations[entry.name] = entry
        }
    }
}
