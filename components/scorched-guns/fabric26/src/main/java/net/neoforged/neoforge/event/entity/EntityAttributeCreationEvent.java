/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class EntityAttributeCreationEvent {
    public <T extends net.minecraft.world.entity.LivingEntity> void put(EntityType<T> entityType, AttributeSupplier attributes) {
    }
}
