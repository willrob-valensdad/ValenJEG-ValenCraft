package top.ribs.scguns.fabric;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
/** Fabric callbacks that the NeoForge game bus used to deliver on its own. */
public final class FabricEventHooks {
    private FabricEventHooks() {}
    public static void register(IEventBus bus) {
        ServerLifecycleHooks.captureFromFabric();
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            ServerLifecycleHooks.setCurrentServer(server);
            bus.post(new ServerTickEvent.Pre(server));
            for (ServerPlayer player : server.getPlayerList().getPlayers()) bus.post(new PlayerTickEvent.Pre(player));
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLifecycleHooks.setCurrentServer(server);
            bus.post(new ServerTickEvent.Post(server));
            for (ServerPlayer player : server.getPlayerList().getPlayers()) bus.post(new PlayerTickEvent.Post(player));
        });
        ServerTickEvents.START_LEVEL_TICK.register(level -> bus.post(new LevelTickEvent.Pre(level)));
        ServerTickEvents.END_LEVEL_TICK.register(level -> bus.post(new LevelTickEvent.Post(level)));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> bus.post(new ServerAboutToStartEvent(server)));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> bus.post(new ServerStartedEvent(server)));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents.LOAD.register((server, level) -> bus.post(new net.neoforged.neoforge.event.level.LevelEvent.Load(level)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> bus.post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> bus.post(new ServerStoppedEvent(server)));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            bus.post(new AddServerReloadListenersEvent());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) SyncedEntityData.sendAllTo(player);
        });
        ServerPlayerEvents.JOIN.register(player -> {
            bus.post(new PlayerEvent.PlayerLoggedInEvent(player));
            SyncedEntityData.sendAllTo(player);
        });
        ServerPlayerEvents.LEAVE.register(player -> bus.post(new PlayerEvent.PlayerLoggedOutEvent(player)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> SyncedEntityData.sendAllTo(newPlayer));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            bus.post(new LivingDeathEvent(entity, source));
            if (entity instanceof ServerPlayer player) SyncedEntityData.resetOnDeath(player);
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            bus.post(new EntityJoinLevelEvent(entity, level));
            SpawnExtras.send(entity);
        });
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, next) ->
                bus.post(new LivingEquipmentChangeEvent(entity, slot, previous, next)));
        bus.post(new AddServerReloadListenersEvent());
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) ClientHooks.register(bus);
    }
    private static final class ClientHooks {
        private static void register(IEventBus bus) {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.register(client -> {
                bus.post(new net.neoforged.neoforge.client.event.ClientTickEvent.Pre());
                if (client.player != null) bus.post(new PlayerTickEvent.Pre(client.player));
            });
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
                bus.post(new net.neoforged.neoforge.client.event.ClientTickEvent.Post());
                if (client.player != null) bus.post(new PlayerTickEvent.Post(client.player));
                SpawnExtras.clientTick();
            });
            net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("scguns", "hud"),
                    (graphics, tickCounter) -> bus.post(new net.neoforged.neoforge.client.event.RenderGuiEvent.Post(graphics, tickCounter)));
        }
    }
}
