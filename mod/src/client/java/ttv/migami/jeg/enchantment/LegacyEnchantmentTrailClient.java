package ttv.migami.jeg.enchantment;

import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/** Original BulletTrail tick: move, gravity, one always-visible particle with motion,
 * only for the local shooter. Master hardcodes MYCELIUM;5322334 honors enchantment data.
 * Collateral uses the original purple four-tick ordinary enchanted particle. */
public final class LegacyEnchantmentTrailClient {
    private LegacyEnchantmentTrailClient(){}
    private static final List<Trail> ACTIVE=new ArrayList<>();
    private static final class Trail{
        Vec3 position,motion;final int life,shooter,flags;final double gravity;final boolean master;int age;
        Trail(LegacyEnchantmentTrailPayload p){position=p.position();motion=p.motion();life=p.life();gravity=p.gravity();shooter=p.shooterId();flags=p.flags();master=p.master();}
    }
    public static void register(){
        LegacyEnchantedTrailParticle.register();
        ClientPlayNetworking.registerGlobalReceiver(LegacyEnchantmentTrailPayload.TYPE,(payload,context)->context.client().execute(()->ACTIVE.add(new Trail(payload))));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.level==null||client.player==null){ACTIVE.clear();return;}
            var iterator=ACTIVE.iterator();while(iterator.hasNext()){
                var trail=iterator.next();trail.age++;trail.position=trail.position.add(trail.motion);
                if(trail.gravity!=0)trail.motion=trail.motion.add(0,trail.gravity,0);
                if(trail.shooter==client.player.getId()){
                    ParticleOptions particle=trail.master?ParticleTypes.MYCELIUM:(trail.flags&2)!=0?ParticleTypes.LAVA:(trail.flags&1)!=0?ParticleTypes.ENCHANTED_HIT:LegacyGunEnchantments.TRAIL;
                    if(particle!=null)client.level.addAlwaysVisibleParticle(particle,true,trail.position.x,trail.position.y,trail.position.z,trail.motion.x,trail.motion.y,trail.motion.z);
                }
                if(trail.age>=trail.life||client.player.distanceToSqr(trail.position)>256D*256D)iterator.remove();
            }
        });
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->ACTIVE.clear());
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client->ACTIVE.clear());
    }
}
