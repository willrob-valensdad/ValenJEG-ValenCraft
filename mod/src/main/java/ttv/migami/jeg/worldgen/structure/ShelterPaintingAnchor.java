package ttv.migami.jeg.worldgen.structure;

import net.minecraft.resources.Identifier;

/**
 * Scope for the authored shelter and miner templates. Placement of any other pool element
 * suspends the scope, including a nested {@code SinglePoolElement.place}.
 */
public final class ShelterPaintingAnchor {
    public static final String TEMPLATE_ID = "jeg:valen_minipurge/underground_shelter";
    public static final Identifier TEMPLATE = Identifier.parse(TEMPLATE_ID);
    public static final String MINER_TEMPLATE_ID = "jeg:valen_mining/miner_hut";
    public static final Identifier MINER_TEMPLATE = Identifier.parse(MINER_TEMPLATE_ID);

    private static final ThreadLocal<String> ACTIVE = new ThreadLocal<>();

    private ShelterPaintingAnchor() {}

    public static boolean isShelter(Identifier template) {
        return TEMPLATE.equals(template);
    }

    public static boolean active() {
        return TEMPLATE_ID.equals(ACTIVE.get()) || MINER_TEMPLATE_ID.equals(ACTIVE.get());
    }

    /**
     * @return the scope to pass to {@link #end(String)}
     */
    public static String begin(Identifier template) {
        String previous = ACTIVE.get();
        if (isShelter(template) || MINER_TEMPLATE.equals(template)) {
            ACTIVE.set(template.toString());
        } else if (previous != null) {
            ACTIVE.remove();
        }
        return previous;
    }

    public static void end(String previous) {
        if (previous == null) {
            ACTIVE.remove();
        } else {
            ACTIVE.set(previous);
        }
    }

}
