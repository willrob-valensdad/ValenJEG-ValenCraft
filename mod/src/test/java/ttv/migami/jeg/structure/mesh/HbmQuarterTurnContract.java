package ttv.migami.jeg.structure.mesh;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

/** Geometry contract against the pinned Rotation enum. No world, client, or template is loaded. */
public final class HbmQuarterTurnContract {
    private HbmQuarterTurnContract() {}

    public static void main(String[] args) {
        float x = 0.2F, y = 3.5F, z = 0.1F;
        float[] none = HbmQuarterTurn.aroundBlockCenter(x, y, z, Rotation.NONE.ordinal());
        require(none[0] == x && none[1] == y && none[2] == z, "none");
        float[] cw = HbmQuarterTurn.aroundBlockCenter(x, y, z, Rotation.CLOCKWISE_90.ordinal());
        require(cw[0] == 1F - z && cw[1] == y && cw[2] == x, "clockwise 90");
        float[] half = HbmQuarterTurn.aroundBlockCenter(x, y, z, Rotation.CLOCKWISE_180.ordinal());
        require(half[0] == 1F - x && half[1] == y && half[2] == 1F - z, "180");
        float[] ccw = HbmQuarterTurn.aroundBlockCenter(x, y, z, Rotation.COUNTERCLOCKWISE_90.ordinal());
        float[] thrice = HbmQuarterTurn.aroundBlockCenter(x, y, z, 3);
        require(close(ccw, thrice), "three clockwise steps are one counterclockwise step");
        for (Rotation first : Rotation.values()) {
            for (Rotation second : Rotation.values()) {
                int composed = HbmQuarterTurn.compose(first.ordinal(), second);
                require(composed == first.getRotated(second).ordinal(), "compose uses Rotation.getRotated");
                float[] step = HbmQuarterTurn.aroundBlockCenter(x, y, z, first.ordinal());
                float[] sequential = HbmQuarterTurn.aroundBlockCenter(step[0], step[1], step[2], second.ordinal());
                float[] direct = HbmQuarterTurn.aroundBlockCenter(x, y, z, composed);
                require(close(sequential, direct), "composition " + first + " then " + second);
            }
        }
        require(Rotation.CLOCKWISE_90.rotate(Direction.NORTH) == Direction.EAST, "north turns east");
        require(Rotation.CLOCKWISE_90.rotate(Direction.UP) == Direction.UP, "vertical shade stays");
        require(HbmQuarterTurn.metaAfterRotate(4, 2, Rotation.CLOCKWISE_90) == 4, "tape meta 4 stays");
        require(HbmQuarterTurn.metaAfterRotate(5, 0, Rotation.CLOCKWISE_180) == 5, "pole meta 5 stays");
        require(HbmQuarterTurn.metaAfterRotate(0, 1, Rotation.COUNTERCLOCKWISE_90) == 0, "barbed meta 0 stays");
        System.out.println("hbm-quarter-turn-contract-ok");
    }

    private static boolean close(float[] left, float[] right) {
        return Math.abs(left[0] - right[0]) < 1.0e-5F && Math.abs(left[1] - right[1]) < 1.0e-5F && Math.abs(left[2] - right[2]) < 1.0e-5F;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
