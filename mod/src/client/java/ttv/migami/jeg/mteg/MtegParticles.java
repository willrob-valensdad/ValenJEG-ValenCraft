package ttv.migami.jeg.mteg;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** Original MTEG FireballParticle and FlareSmokeParticle (5d7fb0f, GPLv3),
 * ported from TextureSheetParticle to native26.3 SingleQuadParticle. */
public final class MtegParticles {
    private MtegParticles(){}
    public static void register(){
        var registry=ParticleProviderRegistry.getInstance();
        registry.register(MtegContent.FIREBALL_PARTICLE,sprites->new Provider(sprites,true));
        registry.register(MtegContent.FLARE_SMOKE_PARTICLE,sprites->new Provider(sprites,false));
    }
    private static final class SourceParticle extends SingleQuadParticle {
        private final boolean fire;
        SourceParticle(ClientLevel level,double x,double y,double z,SpriteSet sprites,double dx,double dy,double dz,boolean fire){
            super(level,x,y,z,sprites.first());this.fire=fire;
            friction=.8F;xd=dx;yd=dy;zd=dz;
            quadSize*=random.nextFloat()*(fire?3F:1F)+(fire?1F:.2F);
            lifetime=fire?5:1000;setSpriteFromAge(sprites);
            setColor(1F,fire?1F:.2F,fire?1F:.2F);
            if(!fire)setSprite(sprites.get(random));
        }
        @Override public Layer getLayer(){return Layer.TRANSLUCENT;}
        @Override public void tick(){
            alpha=fire?age/2F:age/2F-.2F-.2F;
            super.tick();
            if(fire&&!removed&&random.nextFloat()>.9F)level.addParticle(ParticleTypes.SMOKE,x,y,z,xd,yd,zd);
            alpha=-(1F/lifetime)*age+1;
        }
        @Override protected int getLightCoords(float partial){
            int light=super.getLightCoords(partial),sky=light>>16&255;
            if(fire)return 255|sky<<16;
            float progress=Mth.clamp((age+partial)/lifetime,0,1);
            int block=Math.max(240,(light&255)-(int)(progress*15*16));
            return block|sky<<16;
        }
        @Override public float getQuadSize(float partial){
            float progress=(age+partial)/lifetime;
            return quadSize*(fire?1-progress*progress:(1+progress*progress)*50);
        }
    }
    private record Provider(SpriteSet sprites,boolean fire) implements ParticleProvider<SimpleParticleType>{
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz,RandomSource random){
            var particle=new SourceParticle(level,x,y,z,sprites,dx,dy,dz,fire);
            return particle;
        }
    }
}
