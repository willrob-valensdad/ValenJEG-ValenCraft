package net.neoforged.neoforge.common;
import net.neoforged.bus.DispatchingEventBus;
import net.neoforged.bus.api.IEventBus;
public final class NeoForge {
    public static final IEventBus EVENT_BUS = new DispatchingEventBus();
    private NeoForge() {}
}
