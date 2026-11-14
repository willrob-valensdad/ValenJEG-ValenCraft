/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityJoinLevelEvent {
    public static final java.util.Map<Entity, Boolean> LOADED_FROM_DISK = new java.util.WeakHashMap<>();
    private final Entity entity;
    private final Level level;
    private final boolean loadedFromDisk;

    public EntityJoinLevelEvent(Entity entity, Level level) {
        this.entity = entity;
        this.level = level;
        this.loadedFromDisk = Boolean.TRUE.equals(LOADED_FROM_DISK.get(entity));
    }

    public boolean loadedFromDisk() { return loadedFromDisk; }

    public Entity getEntity() {
        return entity;
    }

    public Level getLevel() {
        return level;
    }
}
