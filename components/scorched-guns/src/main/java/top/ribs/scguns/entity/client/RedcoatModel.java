package top.ribs.scguns.entity.client;

import top.ribs.scguns.client.render.entity.state.ScgunsBasicMobRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;

public class RedcoatModel extends EntityModel<ScgunsBasicMobRenderState> {
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation attackAnimation;

	private final ModelPart Redcoat;
	private final ModelPart head;
	final ModelPart Flash;

	    public RedcoatModel(ModelPart root) {
        super(root.getChild("Redcoat"));
        this.Redcoat = this.root();
        this.head = this.Redcoat.getChild("head");
        this.Flash = this.Redcoat.getChild("right_arm").getChild("Musket").getChild("Flash");
        this.walkAnimation = ModAnimationDefinitions.REDCOAT_WALK.bake(this.root());
        this.idleAnimation = ModAnimationDefinitions.REDCOAT_IDLE.bake(this.root());
        this.attackAnimation = ModAnimationDefinitions.REDCOAT_IDLE.bake(this.root());
    }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Redcoat = partdefinition.addOrReplaceChild("Redcoat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = Redcoat.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 18).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.5F, -8.75F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition body = Redcoat.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = Redcoat.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -24.0F, 0.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 1.0F, -0.6981F, 0.0F, 0.0F));

		PartDefinition Musket = right_arm.addOrReplaceChild("Musket", CubeListBuilder.create().texOffs(15, 35).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 39).addBox(-1.5F, 1.0651F, 10.6397F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(40, 36).addBox(-1.5F, 4.0651F, 15.6397F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(18, 18).addBox(-1.0F, 1.0651F, 19.6397F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, 1.5F, 1.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(40, 24).addBox(-1.0F, 0.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(40, 29).addBox(0.0F, 1.0F, -22.0F, 0.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2462F, 5.5F, -9.4566F, 0.0F, 0.1745F, 0.0F));

		PartDefinition cube_r1 = Musket.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -0.5F, 0.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.8F, 1.05F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Musket.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -0.5F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 3.9F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Musket.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.5F, 2.75F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Musket.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(22, 34).addBox(-1.51F, -1.0F, -2.5F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, 7.25F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Musket.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(42, 16).addBox(-1.3473F, -1.3597F, -14.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0123F, 2.0F, 0.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition cube_r6 = Musket.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 18).addBox(-0.6527F, 0.3597F, -15.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, -1.0F, 0.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition Flash = Musket.addOrReplaceChild("Flash", CubeListBuilder.create().texOffs(52, 44).addBox(-4.0F, -4.4F, -15.25F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = Redcoat.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 51).addBox(0.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -23.0F, -0.75F, -0.5031F, 0.274F, 0.7459F));

		PartDefinition left_leg = Redcoat.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 48).addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition right_leg = Redcoat.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 48).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
    @Override
    public void setupAnim(ScgunsBasicMobRenderState state) {
        super.setupAnim(state);
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.0F, 2.5F);
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks, 1.0F);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
        this.head.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * ((float)Math.PI / 180.0F);
        this.head.xRot = Mth.clamp(state.xRot, -20.0F, 20.0F) * ((float)Math.PI / 180.0F);
        this.Flash.visible = state.muzzleFlashVisible;
    }


}


