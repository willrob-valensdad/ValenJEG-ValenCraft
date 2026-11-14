package top.ribs.scguns.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** Minecraft 26.2 compatibility helpers for entity calls that became server-level explicit. */
public final class EntityPortingCompat {
    private EntityPortingCompat() {}

    public static boolean doHurtTarget(Mob attacker, Entity target) {
        if (attacker.level() instanceof ServerLevel serverLevel) {
            return attacker.doHurtTarget(serverLevel, target);
        }
        return false;
    }
}
