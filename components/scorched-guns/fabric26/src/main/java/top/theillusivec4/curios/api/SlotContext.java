package top.theillusivec4.curios.api;
import net.minecraft.world.entity.LivingEntity;
public final class SlotContext {
    private final String identifier;
    private final LivingEntity entity;
    private final int index;
    public SlotContext(String identifier, LivingEntity entity, int index) {
        this.identifier = identifier; this.entity = entity; this.index = index;
    }
    public String identifier() { return identifier; }
    public LivingEntity entity() { return entity; }
    public int index() { return index; }
}
