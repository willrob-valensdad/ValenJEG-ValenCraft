package top.ribs.scguns.client.handler;

/** Temporary no-op shim for the optional Controllable integration on 26.2. */
public final class ControllerHandler {
    private ControllerHandler() {}
    public static void init() {}
    public static boolean isAiming() { return false; }
    public static boolean isShooting() { return false; }
}
