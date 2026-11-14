package com.atsuishio.superbwarfare.entity.vehicle.base

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.util.GeckoLibUtil

@Deprecated("Geckolib will be removed since 0.8.10, use Simple Bedrock Model instead")
abstract class GeoVehicleEntity(pEntityType: EntityType<*>, pLevel: Level) : VehicleEntity(pEntityType, pLevel),
    GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache() = this.cache

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {}

}
