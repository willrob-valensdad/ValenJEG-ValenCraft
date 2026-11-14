package net.neoforged.neoforge.event.entity.living;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
public class LivingChangeTargetEvent extends Event {
    private final LivingEntity entity;
    private final LivingEntity originalTarget;
    private LivingEntity newTarget;
    public LivingChangeTargetEvent(LivingEntity entity, LivingEntity originalTarget, LivingEntity newTarget) {
        this.entity = entity;
        this.originalTarget = originalTarget;
        this.newTarget = newTarget;
    }
    public LivingEntity getEntity() { return entity; }
    public LivingEntity getOriginalAboutToBeSetTarget() { return originalTarget; }
    public LivingEntity getNewAboutToBeSetTarget() { return newTarget; }
    public void setNewAboutToBeSetTarget(LivingEntity target) { this.newTarget = target; }
}
