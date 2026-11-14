package top.ribs.scguns.client;

/**
 * Temporary 26.2 compatibility shim. Controller support is optional and the
 * Controllable API used by the 1.21.1 bridge is not present in the supplied
 * 26.2 dependencies. Keyboard/mouse controls are unaffected.
 */
public final class GunButtonBindings {
    public static final DisabledBinding SHOOT = new DisabledBinding();
    public static final DisabledBinding AIM = new DisabledBinding();
    public static final DisabledBinding RELOAD = new DisabledBinding();
    public static final DisabledBinding OPEN_ATTACHMENTS = new DisabledBinding();
    public static final DisabledBinding STEADY_AIM = new DisabledBinding();

    private GunButtonBindings() {}

    public static void register() {}

    public static final class DisabledBinding {
        public boolean isButtonDown() { return false; }
        public boolean isButtonPressed() { return false; }
        public boolean isButtonReleased() { return false; }
        public boolean isDown() { return false; }
        public boolean isPressed() { return false; }
    }
}
