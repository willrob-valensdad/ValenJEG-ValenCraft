package net.neoforged.fml.config;
public class ModConfig {
    public enum Type { CLIENT, COMMON, SERVER }
    private final Type type;
    private final String modId;
    public ModConfig(Type type, String modId) { this.type = type; this.modId = modId; }
    public Type getType() { return type; }
    public String getModId() { return modId; }
}
