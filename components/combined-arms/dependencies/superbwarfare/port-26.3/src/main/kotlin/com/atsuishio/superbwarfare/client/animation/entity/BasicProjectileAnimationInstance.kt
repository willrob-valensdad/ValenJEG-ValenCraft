package com.atsuishio.superbwarfare.client.animation.entity

import com.atsuishio.superbwarfare.entity.projectile.BasicGeoProjectileEntity
import com.maydaymemory.mae.basic.Pose
import com.maydaymemory.mae.control.statemachine.AnimationStateMachine
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

open class BasicProjectileAnimationInstance<T>(
    entity: T,
    loop: Boolean = false
) where T : Entity, T : BasicGeoProjectileEntity {
    val context = BasicProjectileContext(entity, getAnimationLocation(entity), loop)
    private val stateMachine: AnimationStateMachine<BasicProjectileContext<*>> =
        AnimationStateMachine(BasicProjectileStates.INIT, context) { System.nanoTime() }

    fun tick() {
        context.tick()
        stateMachine.tick()
    }

    fun getPose(): Pose {
        return stateMachine.getPose()
    }

    open fun getAnimationLocation(entity: T): Identifier {
        val (_,  namespace, id) = entity.type.descriptionId.split(".")
        return Identifier.fromNamespaceAndPath(namespace, "animations/bedrock/projectile/$id.animation.json")
    }
}
