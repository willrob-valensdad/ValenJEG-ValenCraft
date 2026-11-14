package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Minecraft 26.2 compatibility base for the old legacy texture-sheet
 * Scorched Guns particles.
 *
 * This is a thin adapter over the real 26.2 SingleQuadParticle API.
 */
public abstract class LegacyTextureSheetParticle extends SingleQuadParticle {
    protected LegacyTextureSheetParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, (TextureAtlasSprite) null);
    }

    protected LegacyTextureSheetParticle(ClientLevel level, double x, double y, double z,
                                         double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, (TextureAtlasSprite) null);
    }

    protected void pickSprite(SpriteSet sprites) {
        this.setSprite(sprites.get(this.random));
    }

    /**
     * Retained so old subclasses that override getRenderType() still compile.
     * Minecraft 26.2 groups these through getGroup().
     */
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    protected Layer getLayer() {
        return this.sprite == null ? Layer.TRANSLUCENT : Layer.bySprite(this.sprite);
    }

    @Override
    protected float getU0() {
        return this.sprite == null ? 0.0F : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.sprite == null ? 1.0F : super.getU1();
    }

    @Override
    protected float getV0() {
        return this.sprite == null ? 0.0F : super.getV0();
    }

    @Override
    protected float getV1() {
        return this.sprite == null ? 1.0F : super.getV1();
    }
}
