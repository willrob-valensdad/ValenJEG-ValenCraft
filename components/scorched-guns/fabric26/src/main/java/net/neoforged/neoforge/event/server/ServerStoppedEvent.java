package net.neoforged.neoforge.event.server;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
public class ServerStoppedEvent extends Event {
    private final MinecraftServer server;
    public ServerStoppedEvent() { this(null); }
    public ServerStoppedEvent(MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
}
