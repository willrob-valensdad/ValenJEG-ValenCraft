package earth.terrarium.adastra.client.renderers.entities.mobs;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.mobs.StarCrawlerModel;
import earth.terrarium.adastra.common.entities.mob.StarCrawler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class StarCrawlerRenderer extends MobRenderer<StarCrawler, net.minecraft.client.renderer.entity.state.LivingEntityRenderState, StarCrawlerModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/star_crawler.png");

    public StarCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new StarCrawlerModel(context.bakeLayer(StarCrawlerModel.LAYER_LOCATION)), 0.0f);
    }

    @Override
    public net.minecraft.client.renderer.entity.state.LivingEntityRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.LivingEntityRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        return TEXTURE;
    }
}
