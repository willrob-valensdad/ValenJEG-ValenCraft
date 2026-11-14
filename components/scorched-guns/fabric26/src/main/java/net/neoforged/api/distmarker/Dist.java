/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.api.distmarker;

public enum Dist {
    CLIENT,
    DEDICATED_SERVER;
    public boolean isClient() { return this == CLIENT; }
}
