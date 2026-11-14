package top.ribs.scguns.attributes;


import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

public class SCEntityAttributes {
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityTypes.PLAYER, SCAttributes.PROJECTILE_SPEED);
        event.add(EntityTypes.PLAYER, SCAttributes.RELOAD_SPEED);
        event.add(EntityTypes.PLAYER, SCAttributes.ADDITIONAL_BULLET_DAMAGE);
        event.add(EntityTypes.PLAYER, SCAttributes.BULLET_DAMAGE_MULTIPLIER);
        event.add(EntityTypes.PLAYER, SCAttributes.SPREAD_MULTIPLIER);
        //Disabled
        //event.add(EntityTypes.PLAYER, SCAttributes.BULLET_RESISTANCE.get());
        //event.add(EntityTypes.PLAYER, SCAttributes.FIRE_RATE_MULTIPLIER.get());
    }
}
