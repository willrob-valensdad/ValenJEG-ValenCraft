package net.neoforged.fml;
import net.neoforged.fml.config.ModConfig;
public class ModContainer {
    public void registerConfig(ModConfig.Type type, Object spec) {}
    public void registerConfig(ModConfig.Type type, Object spec, String fileName) { registerConfig(type, spec); }
    public void registerExtensionPoint(Class<?> type, Object factory) {}
}
