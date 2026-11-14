package net.neoforged.neoforge.event;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/** Explosion and tame hooks. No Scorched listener cancels these, so the authored explosion continues. */
public final class EventHooks {
    private EventHooks() {}
    public static boolean onExplosionStart(Level level, Object explosion) { return false; }
    public static void onExplosionDetonate(Level level, Object explosion, List<? extends Entity> entities, List<?> blocks) {}
    public static Vec3 getExplosionKnockback(Level level, Object explosion, Entity entity, Vec3 knockback, List<?> blocks) { return knockback; }
    public static boolean onAnimalTame(LivingEntity animal, Player player) { return false; }
}
