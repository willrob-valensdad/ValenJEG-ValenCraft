package earth.terrarium.adastra.client.renderers.entities.mobs;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.mobs.MoglerModel;
import earth.terrarium.adastra.common.entities.mob.ZombifiedMogler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class ZombifiedMoglerRenderer extends MobRenderer<ZombifiedMogler, MoglerModel.State, MoglerModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/zombified_mogler.png");

    public ZombifiedMoglerRenderer(EntityRendererProvider.Context context) {
        super(context, new MoglerModel(context.bakeLayer(MoglerModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public MoglerModel.State createRenderState() {
        return new MoglerModel.State();
    }

    @Override
    public void extractRenderState(ZombifiedMogler entity, MoglerModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.hoglinLike = true;
    }

    @Override
    public @NotNull Identifier getTextureLocation(MoglerModel.State state) {
        return TEXTURE;
    }
}
