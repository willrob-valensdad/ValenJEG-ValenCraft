package net.neoforged.neoforge.client.event;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.Event;
public class RegisterParticleProvidersEvent extends Event {
    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
        ParticleProviderRegistry.getInstance().register(type, provider);
    }
    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory) {
        ParticleProviderRegistry.getInstance().register(type, (FabricSpriteSet sprites) -> factory.apply(sprites));
    }
    public static class Pre extends RegisterParticleProvidersEvent {}
    public static class Post extends RegisterParticleProvidersEvent {}
}
