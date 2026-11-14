package ttv.migami.jeg.legacy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import ttv.migami.jeg.client.render.BulletTrailRenderer;
/** Native render-state adapter for original blue water beam. */
public final class WhirlpoolRenderer extends EntityRenderer<WhirlpoolProjectile,WhirlpoolRenderer.State> {
    public WhirlpoolRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(WhirlpoolProjectile entity,State state,float delta) { super.extractRenderState(entity,state,delta);state.id=entity.getId();state.position=entity.position(); }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) { BulletTrailRenderer.updateDynamicTrail(state.id,state.position,56575,.05F); }
    public static final class State extends EntityRenderState { int id;net.minecraft.world.phys.Vec3 position; }
}
