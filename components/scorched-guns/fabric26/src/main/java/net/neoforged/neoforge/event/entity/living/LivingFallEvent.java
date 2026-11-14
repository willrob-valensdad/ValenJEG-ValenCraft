package net.neoforged.neoforge.event.entity.living;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
public class LivingFallEvent extends Event {
    private final LivingEntity entity;
    private float distance;
    private float damageMultiplier;
    public LivingFallEvent(LivingEntity entity, float distance, float damageMultiplier) {
        this.entity = entity;
        this.distance = distance;
        this.damageMultiplier = damageMultiplier;
    }
    public LivingEntity getEntity() { return entity; }
    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(float damageMultiplier) { this.damageMultiplier = damageMultiplier; }
}
