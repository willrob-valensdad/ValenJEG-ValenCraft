package top.ribs.scguns.common;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Synchronized delayed server tasks for Minecraft 26.2.
 *
 * The old LogicalSidedProvider work queue no longer exists. The active
 * MinecraftServer is captured from lifecycle events instead.
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public class DelayedTask {
    public static final List<Impl> tasks = new ArrayList<>();
    private static MinecraftServer activeServer;

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        activeServer = event.getServer();
        tasks.clear();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        tasks.clear();
        if (activeServer == event.getServer()) {
            activeServer = null;
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Iterator<Impl> it = tasks.iterator();
        while (it.hasNext()) {
            Impl impl = it.next();
            if (impl.executionTick <= server.getTickCount()) {
                impl.runnable.run();
                it.remove();
            }
        }
    }

    public static void runAfter(int ticks, Runnable run) {
        MinecraftServer server = activeServer;
        if (server == null) {
            throw new IllegalStateException("Tried to add a delayed task without an active server");
        }
        if (!server.isSameThread()) {
            throw new IllegalStateException("Tried to add a delayed task off the main server thread");
        }
        tasks.add(new Impl(server.getTickCount() + Math.max(0, ticks), run));
    }

    private static final class Impl {
        private final int executionTick;
        private final Runnable runnable;

        private Impl(int executionTick, Runnable runnable) {
            this.executionTick = executionTick;
            this.runnable = runnable;
        }
    }
}
