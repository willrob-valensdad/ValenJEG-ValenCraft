package net.neoforged.neoforge.event.server;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
public class ServerStoppingEvent extends Event {
    private final MinecraftServer server;
    public ServerStoppingEvent() { this(null); }
    public ServerStoppingEvent(MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
}
