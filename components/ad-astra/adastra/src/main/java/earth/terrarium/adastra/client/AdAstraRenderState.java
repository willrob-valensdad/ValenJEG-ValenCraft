package earth.terrarium.adastra.client;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.entity.LivingEntity;

public final class AdAstraRenderState {
    public static final RenderStateDataKey<LivingEntity> LIVING_ENTITY = RenderStateDataKey.create(() -> "ad_astra:living_entity");

    private AdAstraRenderState() {}
}
