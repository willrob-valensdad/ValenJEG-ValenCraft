package top.ribs.scguns.fabric;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.DispatchingEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.ribs.scguns.ScorchedGuns;
public final class ScorchedFabric implements ModInitializer {
    public static final DispatchingEventBus MOD_BUS = new DispatchingEventBus();
    @Override public void onInitialize() {
        SyncedEntityData.initPayloads();
        MenuExtras.init();
        SpawnExtras.init();
        new ScorchedGuns(MOD_BUS, new ModContainer());
        MOD_BUS.post(new RegisterCapabilitiesEvent());
        registerAnnotatedSubscribers();
        MOD_BUS.post(new ModConfigEvent.Loading(new ModConfig(ModConfig.Type.COMMON, "scguns")));
        MOD_BUS.post(new ModConfigEvent.Loading(new ModConfig(ModConfig.Type.CLIENT, "scguns")));
        MOD_BUS.post(new ModConfigEvent.Loading(new ModConfig(ModConfig.Type.SERVER, "scguns")));
        MOD_BUS.post(new FMLCommonSetupEvent());
        FabricEventHooks.register(NeoForge.EVENT_BUS);
    }
    private static void registerAnnotatedSubscribers() {
        var stream = ScorchedFabric.class.getResourceAsStream("/scguns-event-subscribers.txt");
        if (stream == null) throw new IllegalStateException("Missing scguns-event-subscribers.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(" ", 2);
                if ("CLIENT".equals(parts[0]) && FMLEnvironment.getDist() != Dist.CLIENT) continue;
                Class<?> type = Class.forName(parts[1]);
                EventBusSubscriber annotation = type.getAnnotation(EventBusSubscriber.class);
                if (annotation != null) {
                    boolean clientOnly = false;
                    for (Dist dist : annotation.value()) if (dist == Dist.CLIENT) clientOnly = true;
                    if (clientOnly && FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) continue;
                }
                NeoForge.EVENT_BUS.register(type);
            }
        } catch (Exception failure) {
            throw new IllegalStateException("Could not register Scorched event subscribers", failure);
        }
    }
}
