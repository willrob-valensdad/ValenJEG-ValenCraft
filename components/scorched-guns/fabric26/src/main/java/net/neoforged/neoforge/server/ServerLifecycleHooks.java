package net.neoforged.neoforge.server;
import net.minecraft.server.MinecraftServer;
public final class ServerLifecycleHooks {
    private static MinecraftServer server;
    private ServerLifecycleHooks() {}
    public static void captureFromFabric() {}
    public static void setCurrentServer(MinecraftServer current) { server = current; }
    public static MinecraftServer getCurrentServer() { return server; }
}
