import dev.valen.compat.energy.OxygenBridge;
public final class SpaceHudAbsenceTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("org.alex_melan.spacereloaded.SpaceReloaded", false, SpaceHudAbsenceTest.class.getClassLoader());
            throw new AssertionError("Control unexpectedly contains SpaceReloaded");
        } catch (ClassNotFoundException expected) {}
        if (OxygenBridge.showNativeHud(null) || OxygenBridge.hasSpaceMask(null))
            throw new AssertionError("Optional HUD gate did not short circuit without SpaceReloaded");
        Class.forName("dev.valen.compat.energy.mixin.NeroOxygenHudMixin", false, SpaceHudAbsenceTest.class.getClassLoader());
        System.out.println("PASS: Space absent, actual HUD bridge gates short circuit, client mixin class loads");
    }
}
