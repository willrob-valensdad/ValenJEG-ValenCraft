package net.neoforged.fml.event.config;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.config.ModConfig;
public class ModConfigEvent extends Event {
    private final ModConfig config;
    public ModConfigEvent(ModConfig config) { this.config = config; }
    public ModConfig getConfig() { return config; }
    public static final class Loading extends ModConfigEvent { public Loading(ModConfig config) { super(config); } }
    public static final class Reloading extends ModConfigEvent { public Reloading(ModConfig config) { super(config); } }
}
