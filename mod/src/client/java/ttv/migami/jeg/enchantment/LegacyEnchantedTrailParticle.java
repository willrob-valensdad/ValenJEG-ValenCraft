package ttv.migami.jeg.enchantment;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** Exact original TrailParticle enchanted branch, GPLv3: native same BaseAshSmoke
 * constructor, scale2, four ticks, purple0.611/0.443/1, full-bright. */
public final class LegacyEnchantedTrailParticle extends BaseAshSmokeParticle {
    private LegacyEnchantedTrailParticle(ClientLevel level,double x,double y,double z,SpriteSet sprites){
        super(level,x,y,z,0F,0F,0F,0,0,0,2F,sprites,.2F,0,0,false);
        lifetime=4;rCol=.611F;gCol=.443F;bCol=1F;alpha=1F;
    }
    @Override protected int getLightCoords(float partial){return 0xF000F0;}
    public static void register(){ParticleProviderRegistry.getInstance().register(LegacyGunEnchantments.TRAIL,Provider::new);}
    private record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType>{
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz,RandomSource random){
            return new LegacyEnchantedTrailParticle(level,x,y,z,sprites);
        }
    }
}
