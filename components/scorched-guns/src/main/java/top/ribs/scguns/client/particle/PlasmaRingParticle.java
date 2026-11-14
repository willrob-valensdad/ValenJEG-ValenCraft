package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class PlasmaRingParticle extends RisingParticle {
    PlasmaRingParticle(ClientLevel level, double x, double y, double z,
                       double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float partialTick) {
        float f = ((float)this.age + partialTick) / (float)this.lifetime;
        return this.quadSize * (1.0F + f * f);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        float f = Mth.clamp(((float)this.age + partialTick) / (float)this.lifetime, 0.0F, 1.0F);
        int packed = super.getLightCoords(partialTick);
        int block = packed & 255;
        int sky = packed >> 16 & 255;
        block = Math.min(240, block + (int)(f * 15.0F * 16.0F));
        return block | sky << 16;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public Provider(SpriteSet sprite) { this.sprite = sprite; }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource randomSource) {
            PlasmaRingParticle particle = new PlasmaRingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite.get(randomSource));
            particle.setAlpha(0.75F);
            particle.scale(3.0F);
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public SmallFlameProvider(SpriteSet sprite) { this.sprite = sprite; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource randomSource) {
            PlasmaRingParticle particle = new PlasmaRingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite.get(randomSource));
            particle.scale(0.5F);
            return particle;
        }
    }
}
