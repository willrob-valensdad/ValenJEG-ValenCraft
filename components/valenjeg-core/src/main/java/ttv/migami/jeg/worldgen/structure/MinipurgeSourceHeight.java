package ttv.migami.jeg.worldgen.structure;

/** Source parameters24..25below origin-column surface/min-63 from CC-BY4.0
 * Minipurge:Black1.0.1 build_bunkers.mcfunction and frozen placement contract.
 * Pure native placement parameter adapter; no source ARR helper implementation. */
public final class MinipurgeSourceHeight {
    private MinipurgeSourceHeight() {}
    public static int baseY(int surfaceAirY, int depth) {
        if (depth != 24 && depth != 25) throw new IllegalArgumentException("Original bunker depth must be24or25");
        return Math.max(-63, surfaceAirY - depth);
    }
}
