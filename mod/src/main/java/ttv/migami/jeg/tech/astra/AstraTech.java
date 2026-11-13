package ttv.migami.jeg.tech.astra;

import net.fabricmc.loader.api.FabricLoader;

/** Keeps optional TeamReborn classes unloaded when its native API is absent. */
public final class AstraTech {
    private AstraTech() {}
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) AstraTechContent.init();
    }
}
