package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 26.2-compatible replacement for the removed vanilla FlyingMob base.
 * Keeps the no-fall-damage and flying travel semantics used by Scorched Guns mobs.
 */
public abstract class ScgunsFlyingMob extends Mob {
    protected ScgunsFlyingMob(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void checkFallDamage(double yMovement, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.02F);
    }
}
