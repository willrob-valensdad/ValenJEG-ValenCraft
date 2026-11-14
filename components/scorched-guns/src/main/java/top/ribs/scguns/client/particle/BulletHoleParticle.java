package top.ribs.scguns.client.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import top.ribs.scguns.Config;

public class BulletHoleParticle extends LegacyTextureSheetParticle {
    private final Direction direction;
    private final BlockPos pos;
    private int uOffset;
    private int vOffset;
    private float textureDensity;

    public BulletHoleParticle(ClientLevel world, double x, double y, double z, Direction direction, BlockPos pos) {
        super(world, x, y, z);
        this.setSprite(this.getSprite(pos));
        this.direction = direction;
        this.pos = pos;
        this.lifetime = (int)(Config.CLIENT.particle.bulletHoleLifeMin.get()
                + world.getRandom().nextFloat()
                * (Config.CLIENT.particle.bulletHoleLifeMax.get() - Config.CLIENT.particle.bulletHoleLifeMin.get()));
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.quadSize = 0.05F;

        BlockState state = world.getBlockState(pos);
        if (state.isAir()) this.remove();

        int color = this.getBlockColor(state, world, pos);
        this.rCol = ((float)(color >> 16 & 255) / 255.0F) / 3.0F;
        this.gCol = ((float)(color >> 8 & 255) / 255.0F) / 3.0F;
        this.bCol = ((float)(color & 255) / 255.0F) / 3.0F;
        this.alpha = 0.9F;
    }

    private int getBlockColor(BlockState state, ClientLevel world, BlockPos pos) {
        if (state.getBlock() == Blocks.GRASS_BLOCK) return Integer.MAX_VALUE;
        BlockTintSource tint = Minecraft.getInstance().getBlockColors().getTintSource(state, 0);
        return tint != null ? tint.colorAsTerrainParticle(state, world, pos) : 0xFFFFFF;
    }

    @Override
    protected void setSprite(TextureAtlasSprite sprite) {
        super.setSprite(sprite);
        this.uOffset = this.random.nextInt(16);
        this.vOffset = this.random.nextInt(16);
        this.textureDensity = (sprite.getU1() - sprite.getU0()) / 16.0F;
    }

    private TextureAtlasSprite getSprite(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
    }

    @Override protected float getU0() { return this.sprite.getU0() + this.uOffset * this.textureDensity; }
    @Override protected float getV0() { return this.sprite.getV0() + this.vOffset * this.textureDensity; }
    @Override protected float getU1() { return this.getU0() + this.textureDensity; }
    @Override protected float getV1() { return this.getV0() + this.textureDensity; }

    @Override
    public void tick() {
        super.tick();
        if (this.level.getBlockState(this.pos).isAir()) this.remove();
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTick) {
        Vec3 view = camera.position();
        float x = (float)(Mth.lerp(partialTick, this.xo, this.x) - view.x());
        float y = (float)(Mth.lerp(partialTick, this.yo, this.y) - view.y());
        float z = (float)(Mth.lerp(partialTick, this.zo, this.z) - view.z());

        Quaternionf rotation = new Quaternionf(this.direction.getRotation())
                .mul(Axis.XP.rotation((float)(Math.PI / 2.0)));

        float threshold = Config.CLIENT.particle.bulletHoleFadeThreshold.get().floatValue();
        float fade = threshold >= 1.0F ? 1.0F
                : 1.0F - Math.max((float)this.age - (float)this.lifetime * threshold, 0.0F)
                / ((float)this.lifetime - (float)this.lifetime * threshold);

        renderState.add(this.getLayer(), x, y, z,
                rotation.x, rotation.y, rotation.z, rotation.w,
                this.getQuadSize(partialTick),
                this.getU1(), this.getU0(), this.getV0(), this.getV1(),
                ARGB.colorFromFloat(this.alpha * fade, this.rCol, this.gCol, this.bCol),
                this.getLightCoords(partialTick));
    }
}
