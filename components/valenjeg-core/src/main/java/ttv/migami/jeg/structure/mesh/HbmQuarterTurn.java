package ttv.migami.jeg.structure.mesh;

import net.minecraft.world.level.block.Rotation;

/** Block-center quarter turns. Ordinal 0 is none, then one, two, and three clockwise steps. */
public final class HbmQuarterTurn {
    private HbmQuarterTurn() {}

    public static int compose(int current, Rotation added) {
        return Rotation.values()[current].getRotated(added).ordinal();
    }

    /** (x, z) -> (1 - z, x) repeated. This is clockwise about (0.5, 0.5), matching Rotation.CLOCKWISE_90 on a horizontal direction. */
    public static float[] aroundBlockCenter(float x, float y, float z, int clockwiseQuarters) {
        for (int step = 0; step < clockwiseQuarters; step++) {
            float nextX = 1F - z;
            z = x;
            x = nextX;
        }
        return new float[] {x, y, z};
    }

    /** Structure rotation changes the quarter-turn index and leaves the authored meta value alone. */
    public static int metaAfterRotate(int meta, int rotation, Rotation added) {
        int next = compose(rotation, added);
        if (next < 0 || next > 3) throw new IllegalStateException("quarter turn " + next);
        return meta;
    }
}
