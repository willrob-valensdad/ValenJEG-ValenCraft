package top.ribs.scguns.client;

/** Temporary shim while optional controller integration is ported to 26.2. */
public enum GunConflictContext {
    IN_GAME_HOLDING_WEAPON;

    public boolean isActive() { return false; }
    public Object bindingContext() { return null; }
}
