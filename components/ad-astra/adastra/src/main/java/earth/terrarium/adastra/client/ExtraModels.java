package earth.terrarium.adastra.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ExtraModels {
    private static final Map<Identifier, ExtraModelKey<BlockStateModel>> KEYS = new HashMap<>();

    private ExtraModels() {}

    public static ExtraModelKey<BlockStateModel> key(Identifier id) {
        return KEYS.computeIfAbsent(id, ignored -> ExtraModelKey.create(id::toString));
    }
}
