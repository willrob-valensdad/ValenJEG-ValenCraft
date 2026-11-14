package net.neoforged.neoforge.event.server;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
public class ServerAboutToStartEvent extends Event {
    private final MinecraftServer server;
    public ServerAboutToStartEvent(MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
}
