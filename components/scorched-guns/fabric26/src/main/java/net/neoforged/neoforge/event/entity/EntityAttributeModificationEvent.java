package net.neoforged.neoforge.event.entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.DeferredHolder;
/** Attributes Scorched adds to an entity type, including the player gun attributes. */
public class EntityAttributeModificationEvent extends Event {
    private final Map<EntityType<?>, List<Holder<Attribute>>> additions = new HashMap<>();
    public void add(EntityType<?> type, DeferredHolder<Attribute, ? extends Attribute> attribute) {
        additions.computeIfAbsent(type, ignored -> new ArrayList<>()).add(attribute.asHolder());
    }
    public Map<EntityType<?>, List<Holder<Attribute>>> additions() { return additions; }
}
