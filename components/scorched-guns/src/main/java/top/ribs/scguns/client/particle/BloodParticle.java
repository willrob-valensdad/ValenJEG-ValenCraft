package top.ribs.scguns.client.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import top.ribs.scguns.init.ModTags;

@OnlyIn(Dist.CLIENT)
public class BloodParticle extends LegacyTextureSheetParticle {
    public BloodParticle(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z, 0.1, 0.1, 0.1);
        this.gravity = 1.5F;
        this.quadSize = 0.1F;
        this.lifetime = (int)(12.0F / (this.random.nextFloat() * 0.9F + 0.1F));
    }

    private BloodParticle(ClientLevel world, double x, double y, double z,
                          net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
        this(world, x, y, z);
        this.setSprite(sprite);
    }

    public void setCustomColor(float r, float g, float b, float a) {
        this.setColor(r, g, b);
        this.alpha = a;
    }

    public void setColorBasedOnEntity(EntityType<?> entityType) {
        if (entityType.builtInRegistryHolder().is(ModTags.Entities.RED_BLOOD)) this.setColor(0.541F, 0.027F, 0.027F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.WHITE_BLOOD)) this.setColor(1.0F, 1.0F, 1.0F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.GREEN_BLOOD)) this.setColor(0.0F, 1.0F, 0.0F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.BLUE_BLOOD)) this.setColor(0.0F, 0.0F, 1.0F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.YELLOW_BLOOD)) this.setColor(1.0F, 1.0F, 0.0F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.PURPLE_BLOOD)) this.setColor(0.5F, 0.0F, 0.5F);
        else if (entityType.builtInRegistryHolder().is(ModTags.Entities.BLACK_BLOOD)) this.setColor(0.0F, 0.0F, 0.0F);
        else this.setColor(0.541F, 0.027F, 0.027F);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.xd = 0.0;
            this.zd = 0.0;
            this.quadSize *= 0.95F;
        }
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTick) {
        Vec3 view = camera.position();
        float x = (float)(Mth.lerp(partialTick, this.xo, this.x) - view.x());
        float y = (float)(Mth.lerp(partialTick, this.yo, this.y) - view.y());
        float z = (float)(Mth.lerp(partialTick, this.zo, this.z) - view.z());
        if (this.onGround) y += 0.01F;

        Quaternionf rotation = Direction.NORTH.getRotation();
        if (this.roll == 0.0F) {
            if (!this.onGround) rotation = new Quaternionf(camera.rotation());
        } else {
            rotation = new Quaternionf(camera.rotation());
            rotation.mul(Axis.ZP.rotation(Mth.lerp(partialTick, this.oRoll, this.roll)));
        }

        renderState.add(this.getLayer(), x, y, z,
                rotation.x, rotation.y, rotation.z, rotation.w,
                this.getQuadSize(partialTick),
                this.getU1(), this.getU0(), this.getV0(), this.getV1(),
                ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol),
                this.getLightCoords(partialTick));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource randomSource) {
            BloodParticle particle = new BloodParticle(world, x, y, z, this.spriteSet.get(randomSource));
            particle.setColor((float)xSpeed, (float)ySpeed, (float)zSpeed);
            return particle;
        }
    }
}
