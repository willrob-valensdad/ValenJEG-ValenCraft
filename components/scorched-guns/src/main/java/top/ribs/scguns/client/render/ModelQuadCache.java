package top.ribs.scguns.client.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Frame-allocation cache for immutable standalone model geometry.
 *
 * <p>Scorched Guns 1.20.1 could submit a BakedModel directly. The 26.2 submit
 * pipeline needs the directional quads flattened into one list. Building that
 * list for every gun part on every frame creates a large amount of short-lived
 * garbage, so the port caches the flattened list for the lifetime of the
 * current resource/model set.</p>
 */
public final class ModelQuadCache {
    private static final Map<BlockStateModelPart, List<BakedQuad>> QUADS = new IdentityHashMap<>();
    private static final int[] NO_TINTS = new int[0];

    private ModelQuadCache() {
    }

    public static synchronized List<BakedQuad> getQuads(BlockStateModelPart model) {
        return QUADS.computeIfAbsent(model, ModelQuadCache::flatten);
    }

    public static int[] noTints() {
        return NO_TINTS;
    }

    public static synchronized void clear() {
        QUADS.clear();
    }

    private static List<BakedQuad> flatten(BlockStateModelPart model) {
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            quads.addAll(model.getQuads(direction));
        }
        quads.addAll(model.getQuads(null));
        return List.copyOf(quads);
    }
}
