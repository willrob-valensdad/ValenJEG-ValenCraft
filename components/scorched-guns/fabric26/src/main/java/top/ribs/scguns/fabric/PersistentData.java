package top.ribs.scguns.fabric;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
/** Forge persistent-data replacement. Values live with the entity instance. */
public final class PersistentData {
    private static final Map<Entity, CompoundTag> DATA = new IdentityHashMap<>();
    private PersistentData() {}
    public static CompoundTag of(Entity entity) {
        return DATA.computeIfAbsent(entity, ignored -> new CompoundTag());
    }
}
