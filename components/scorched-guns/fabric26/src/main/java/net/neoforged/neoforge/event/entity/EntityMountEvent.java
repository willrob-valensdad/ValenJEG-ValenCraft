package net.neoforged.neoforge.event.entity;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
public class EntityMountEvent extends Event {
    private final Entity entity;
    private final Entity vehicle;
    private final boolean mounting;
    public EntityMountEvent(Entity entity, Entity vehicle, boolean mounting) {
        this.entity = entity;
        this.vehicle = vehicle;
        this.mounting = mounting;
    }
    public Entity getEntity() { return entity; }
    public Entity getEntityBeingMounted() { return vehicle; }
    public boolean isMounting() { return mounting; }
}
