package ttv.migami.jeg.legacy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import ttv.migami.jeg.Reference;
/** Original SplashModel boxes/UVs/textures and15degrees per tick rotation. */
public final class SplashRenderer extends EntityRenderer<LandWhirlpool,SplashRenderer.State> {
    private final ModelPart model;
    private static final UvMapping UV=new UvMapping() { public float getU(float u) {return u;} public float getV(float v) {return v;} };
    public SplashRenderer(EntityRendererProvider.Context context) {
        super(context);MeshDefinition mesh=new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bb_main",CubeListBuilder.create().texOffs(0,0).addBox(-24,0,-24,48,80,48).texOffs(0,128).addBox(-32,0,-32,64,48,64),PartPose.ZERO);
        model=LayerDefinition.create(mesh,256,240).bakeRoot();
    }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(LandWhirlpool entity,State state,float delta) { super.extractRenderState(entity,state,delta);state.frame=entity.frame();state.rotation=(entity.tickCount+delta)*15F; }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        pose.pushPose();pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(state.rotation)));
        collector.submitModelPart(model,pose,RenderTypes.entityTranslucent(Reference.id("textures/entity/splash_"+state.frame+".png")),state.lightCoords,OverlayTexture.NO_OVERLAY,UV);
        pose.popPose();
    }
    public static final class State extends EntityRenderState { int frame;float rotation; }
}
