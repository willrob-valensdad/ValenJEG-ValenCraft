/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;

public final class EntityTickEvent {
    private EntityTickEvent() {
    }

    public static class Post {
        private final Entity entity;

        public Post(Entity entity) {
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }
}
