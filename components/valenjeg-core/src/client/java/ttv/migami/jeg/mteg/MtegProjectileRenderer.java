package ttv.migami.jeg.mteg;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** All35 original definitions have visible=false (JEG5322334 default).
 * The original ProjectileRenderer draws no item geometry for these projectiles;
 * source particle providers and native JEG trail payloads render their effects. */
public final class MtegProjectileRenderer extends EntityRenderer<MtegProjectile,EntityRenderState> {
    public MtegProjectileRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public EntityRenderState createRenderState(){return new EntityRenderState();}
    public static void register(){
        EntityRendererRegistry.register(MtegContent.BULLET_ENTITY,MtegProjectileRenderer::new);
        EntityRendererRegistry.register(MtegContent.TAKI_ENTITY,MtegProjectileRenderer::new);
        EntityRendererRegistry.register(MtegContent.FIREBALL_ENTITY,MtegProjectileRenderer::new);
        EntityRendererRegistry.register(MtegContent.FLARE_ENTITY,MtegProjectileRenderer::new);
    }
}
