package com.atsuishio.superbwarfare.client.particle

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.inventory.InventoryMenu
import org.joml.Vector3f
import kotlin.math.max

/**
 * @author Forked from MrCrayfish, continued by Timeless devs
 * Code based on TaC-Z
 */
class BulletDecalParticle @JvmOverloads constructor(
    level: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    private val direction: Direction,
    private val pos: BlockPos,
    rCol: Float = 0f,
    gCol: Float = 0f,
    bCol: Float = 0f
) : SingleQuadParticle(
    level,
    x,
    y,
    z,
    Minecraft.getInstance().modelManager.blockStateModelSet
        .getParticleMaterial(level.getBlockState(pos)).sprite()
) {
    private var uOffset = 0
    private var vOffset = 0
    private var textureDensity = 0f

    init {
        this.setSprite(this.getSprite(pos))
        this.lifetime = 200
        this.hasPhysics = false
        this.gravity = 0f
        this.quadSize = 0.05f

        if (shouldRemove()) {
            this.remove()
        }

        this.rCol = rCol
        this.gCol = gCol
        this.bCol = bCol
        this.alpha = 0.9f
    }

    public override fun setSprite(sprite: TextureAtlasSprite) {
        super.setSprite(sprite)
        this.uOffset = this.random.nextInt(16)
        this.vOffset = this.random.nextInt(16)
        this.textureDensity = (sprite.u1 - sprite.u0) / 16f
    }

    private fun getSprite(pos: BlockPos): TextureAtlasSprite {
        val minecraft = Minecraft.getInstance()
        val clientLevel = minecraft.level

        if (clientLevel != null) {
            val state = clientLevel.getBlockState(pos)
            return minecraft.modelManager.blockStateModelSet.getParticleMaterial(state).sprite()
        }

        return minecraft.modelManager.blockStateModelSet
            .getParticleMaterial(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()).sprite()
    }

    override fun getU0(): Float {
        return this.sprite.u0 + this.uOffset * this.textureDensity
    }

    override fun getV0(): Float {
        return this.sprite.v0 + this.vOffset * this.textureDensity
    }

    override fun getU1(): Float {
        return this.u0 + this.textureDensity
    }

    override fun getV1(): Float {
        return this.v0 + this.textureDensity
    }

    override fun tick() {
        super.tick()

        if (shouldRemove()) {
            this.remove()
        }
    }

    override fun getLightCoords(partialTick: Float): Int = 15728880

    override fun extract(
        state: net.minecraft.client.renderer.state.level.QuadParticleRenderState,
        camera: Camera,
        partialTick: Float
    ) {
        val light = max(15 - this.age / 2, 0)
        val colorPercent = light / 15.0f
        val threshold = 0.98 * this.lifetime
        val fade = 1.0f - (max(this.age - threshold, 0.0) / (this.lifetime - threshold)).toFloat()
        val savedR = this.rCol
        val savedG = this.gCol
        val savedB = this.bCol
        val savedA = this.alpha
        this.rCol *= colorPercent
        this.gCol *= colorPercent
        this.bCol *= colorPercent
        this.alpha *= fade
        val quaternion = org.joml.Quaternionf(this.direction.rotation)
        val bias = Vector3f(0f, 0.01f, 0f).rotate(quaternion)
        val view = camera.position()
        val particleX = (Mth.lerp(partialTick.toDouble(), this.xo, this.x) - view.x()).toFloat() + bias.x
        val particleY = (Mth.lerp(partialTick.toDouble(), this.yo, this.y) - view.y()).toFloat() + bias.y
        val particleZ = (Mth.lerp(partialTick.toDouble(), this.zo, this.z) - view.z()).toFloat() + bias.z
        extractRotatedQuad(state, quaternion, particleX, particleY, particleZ, partialTick)
        this.rCol = savedR
        this.gCol = savedG
        this.bCol = savedB
        this.alpha = savedA
    }

    private fun shouldRemove(): Boolean {
        val blockState = this.level.getBlockState(this.pos)

        if (blockState.isAir) {
            return true
        }

        val shape = blockState.getCollisionShape(this.level, this.pos)

        if (shape.isEmpty) {
            return true
        }

        val baseBlockBoundingBox = shape.bounds()
        val blockBoundingBox = baseBlockBoundingBox.move(this.pos)

        return !blockBoundingBox.intersects(
            this.x - 0.1,
            this.y - 0.1,
            this.z - 0.1,
            this.x + 0.1,
            this.y + 0.1,
            this.z + 0.1
        )
    }

    override fun getLayer(): SingleQuadParticle.Layer {
        return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN
    }

    class Provider : ParticleProvider<BulletDecalOption> {
        override fun createParticle(
            option: BulletDecalOption,
            level: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            xSpeed: Double,
            ySpeed: Double,
            zSpeed: Double,
            randomSource: net.minecraft.util.RandomSource
        ): Particle {
            return BulletDecalParticle(
                level,
                x,
                y,
                z,
                option.direction,
                option.pos,
                option.red,
                option.green,
                option.blue
            )
        }
    }
}
