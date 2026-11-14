package com.mrcrayfish.framework.api.network;
import net.minecraft.world.level.Level;
public final class LevelLocation {
    public final Level level;
    public final double x, y, z, radius;
    private LevelLocation(Level level, double x, double y, double z, double radius) {
        this.level = level; this.x = x; this.y = y; this.z = z; this.radius = radius;
    }
    public static LevelLocation create(Level level, double x, double y, double z, double radius) {
        return new LevelLocation(level, x, y, z, radius);
    }
}
