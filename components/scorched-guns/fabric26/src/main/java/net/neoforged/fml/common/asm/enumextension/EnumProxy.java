package net.neoforged.fml.common.asm.enumextension;
/** Holds the parameters of a requested enum extension. 26.3 Rarity is a closed enum, so Constants.safeRarity keeps the vanilla fallback. */
public final class EnumProxy<T extends Enum<T>> {
    private final Class<T> type;
    private final int id;
    private final String name;
    private final Object[] params;
    public EnumProxy(Class<T> type, int id, String name, Object... params) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.params = params;
    }
    public Class<T> type() { return type; }
    public int id() { return id; }
    public String name() { return name; }
}
