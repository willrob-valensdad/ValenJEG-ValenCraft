package top.ribs.scguns.entity.client;

import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;

public class SignalBeaconModel extends EntityModel<ScgunsBasicMobRenderState> {
    private final KeyframeAnimation idleAnimation;

    private final ModelPart main;
    private final ModelPart Body;

        public SignalBeaconModel(ModelPart root) {
        super(root.getChild("body"));
        this.Body = this.root();
        this.main = this.Body;
        this.idleAnimation = ModAnimationDefinitions.SIGNAL_BEACON_IDLE.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 7).addBox(-2.5F, 5.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -0.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-2.0F, 0.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 13).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.5F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 22).addBox(0.0F, -1.0F, -1.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, -1.0F, -1.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(16, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
    @Override
    public void setupAnim(ScgunsBasicMobRenderState state) {
        super.setupAnim(state);
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks, 1.0F);
    }


}
