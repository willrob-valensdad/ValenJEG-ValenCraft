package ttv.migami.jeg.worldgen.structure;

import java.util.function.IntBinaryOperator;

/**
 * Narrow adaptation of HBM NBTStructure.Component.getAverageHeight and
 * addComponentParts from ad3185b56ae2b020bf9a4a0ddcd3ebcb6e1002e6,
 * NBTStructure.java lines 927-931 and 977-992. Original LGPL-3.0 notices are
 * preserved in structures/upstream/hbm. No new placement algorithm.
 */
public final class HbmSourceHeight {
    private HbmSourceHeight() {}

    public static int average(int minX, int minZ, int maxX, int maxZ, IntBinaryOperator topSolidOrLiquid) {
        int total = 0;
        int iterations = 0;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                total += topSolidOrLiquid.applyAsInt(x, z);
                iterations++;
            }
        }
        if (iterations == 0) return 64;
        return total / iterations;
    }

    public static int baseY(int averageHeight, int offset, int minHeight, int maxHeight, boolean flatWorld) {
        int height = averageHeight + offset;
        return flatWorld ? height : Math.max(minHeight, Math.min(maxHeight, height));
    }
}
