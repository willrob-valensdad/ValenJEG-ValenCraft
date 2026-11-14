package com.atsuishio.superbwarfare.client.particle

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.*
import net.minecraft.core.particles.SimpleParticleType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import javax.annotation.ParametersAreNonnullByDefault
import kotlin.math.max

@Environment(EnvType.CLIENT)
open class WhiteStarParticle protected constructor(
    world: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    vx: Double,
    vy: Double,
    vz: Double,
    spriteSet: SpriteSet
) : SingleQuadParticle(world, x, y, z, spriteSet.first()) {
    class WhiteStarParticleProvider(private val spriteSet: SpriteSet) : ParticleProvider<SimpleParticleType> {
        @ParametersAreNonnullByDefault
        override fun createParticle(
            typeIn: SimpleParticleType,
            worldIn: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            xSpeed: Double,
            ySpeed: Double,
            zSpeed: Double,
            randomSource: net.minecraft.util.RandomSource
        ): Particle {
            return WhiteStarParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet)
        }
    }

    init {
        this.setSize(0.6f, 0.6f)
        this.quadSize *= 0.9f
        this.lifetime = max(1, 40 + (this.random.nextInt(40) - 20))
        this.gravity = 0f
        this.hasPhysics = true
        this.xd = vx * 0.95
        this.yd = vy * 0.95
        this.zd = vz * 0.95
        this.setSpriteFromAge(spriteSet)
    }

    public override fun getLightCoords(partialTick: Float): Int {
        return 15728880
    }

    override fun getLayer(): SingleQuadParticle.Layer {
        return SingleQuadParticle.Layer.OPAQUE
    }

    override fun tick() {
        super.tick()
        this.quadSize *= 0.95f
    }

    companion object {
        fun provider(spriteSet: SpriteSet): WhiteStarParticleProvider {
            return WhiteStarParticleProvider(spriteSet)
        }
    }
}
