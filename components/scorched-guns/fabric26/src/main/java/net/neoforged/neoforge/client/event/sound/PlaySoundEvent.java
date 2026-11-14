package net.neoforged.neoforge.client.event.sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.bus.api.Event;
public class PlaySoundEvent extends Event {
    private final SoundEngine engine;
    private SoundInstance sound;
    public PlaySoundEvent(SoundEngine engine, SoundInstance sound) {
        this.engine = engine;
        this.sound = sound;
    }
    public SoundEngine getEngine() { return engine; }
    public SoundInstance getSound() { return sound; }
    public void setSound(SoundInstance sound) { this.sound = sound; }
}
