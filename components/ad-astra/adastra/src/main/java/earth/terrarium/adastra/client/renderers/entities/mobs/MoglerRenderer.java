package earth.terrarium.adastra.client.renderers.entities.mobs;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.mobs.MoglerModel;
import earth.terrarium.adastra.common.entities.mob.Mogler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class MoglerRenderer extends MobRenderer<Mogler, MoglerModel.State, MoglerModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/mogler.png");

    public MoglerRenderer(EntityRendererProvider.Context context) {
        super(context, new MoglerModel(context.bakeLayer(MoglerModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public MoglerModel.State createRenderState() {
        return new MoglerModel.State();
    }

    @Override
    public void extractRenderState(Mogler entity, MoglerModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.hoglinLike = true;
        state.attackTicks = entity.getAttackAnimationRemainingTicks();
        state.converting = entity.isConverting();
    }

    @Override
    public @NotNull Identifier getTextureLocation(MoglerModel.State state) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(MoglerModel.State state) {
        return super.isShaking(state) || state.converting;
    }
}
