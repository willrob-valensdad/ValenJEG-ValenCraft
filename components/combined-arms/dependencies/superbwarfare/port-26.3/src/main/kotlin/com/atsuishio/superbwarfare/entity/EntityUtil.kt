package com.atsuishio.superbwarfare.entity

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Entity
import com.geckolib.animatable.GeoAnimatable
import com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar
import com.geckolib.animation.AnimationController
import com.geckolib.animation.state.AnimationTest
import com.geckolib.animation.object.PlayState
import com.geckolib.animation.RawAnimation
import kotlin.reflect.KProperty

// Geo动画播放Builder
class ControllerBuilder<T : GeoAnimatable>(val animatable: T, val data: ControllerRegistrar) {

    operator fun String.invoke(
        transitionTickTime: Int = 0,
        animationHandler: AnimationTest<T>.() -> PlayState
    ) {
        add(this, transitionTickTime, animationHandler)
    }

    fun add(
        name: String,
        transitionTickTime: Int = 0,
        animationHandler: AnimationTest<T>.() -> PlayState
    ) {
        data.add(AnimationController(animatable, name, transitionTickTime) { it.animationHandler() })
    }

    fun <T : GeoAnimatable> AnimationTest<T>.thenPlay(name: String): PlayState =
        setAndContinue(RawAnimation.begin().thenPlay(name))

    fun <T : GeoAnimatable> AnimationTest<T>.thenLoop(name: String): PlayState =
        setAndContinue(RawAnimation.begin().thenLoop(name))
}

fun <T : GeoAnimatable> AnimationTest<T>.thenPlay(name: String): PlayState =
    setAndContinue(RawAnimation.begin().thenPlay(name))

fun <T : GeoAnimatable> AnimationTest<T>.thenLoop(name: String): PlayState =
    setAndContinue(RawAnimation.begin().thenLoop(name))

inline fun <T : GeoAnimatable> T.buildControllers(
    data: ControllerRegistrar,
    builder: ControllerBuilder<T>.() -> Unit
) {
    ControllerBuilder(this, data).apply(builder)
}

// EntityDataAccessor代理
operator fun <T : Any> EntityDataAccessor<T>.getValue(entity: Any?, prop: KProperty<*>) =
    (entity as Entity).entityData.get(this) as T

operator fun <T : Any> EntityDataAccessor<T>.setValue(entity: Any?, prop: KProperty<*>, value: T) {
    (entity as Entity).entityData.set(this, value)
}
