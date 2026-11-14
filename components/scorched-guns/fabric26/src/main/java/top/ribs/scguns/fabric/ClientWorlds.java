package top.ribs.scguns.fabric;
import java.lang.reflect.Method;
import net.minecraft.world.level.Level;
/** Client level for tooltip day-scaling. Dedicated servers have no client world. */
public final class ClientWorlds {
    private ClientWorlds() {}
    public static Level current() {
        try {
            Class<?> minecraft = Class.forName("net.minecraft.client.Minecraft");
            Object instance = minecraft.getMethod("getInstance").invoke(null);
            Method level = minecraft.getMethod("level");
            Object value = level.invoke(instance);
            return value instanceof Level world ? world : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
