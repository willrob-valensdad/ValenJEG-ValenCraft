package ttv.migami.jeg.client.mesh;

import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.resources.Identifier;

public final class HbmMeshClient {
    private HbmMeshClient() {}

    public static void register() {
        CustomUnbakedBlockStateModel.register(Identifier.fromNamespaceAndPath("jeg", "hbm_static_mesh"), HbmStaticMeshModel.Unbaked.CODEC);
    }
}
