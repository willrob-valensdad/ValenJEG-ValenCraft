package top.ribs.scguns.entity.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;

public class SulfurheadModel extends EntityModel<ScgunsMobRenderState> {
   private final ModelPart Sulfurhead;
   private final ModelPart head;
   private final ModelPart left_eye;
   private final ModelPart right_eye;
   private final ModelPart torso;
   private final ModelPart back_leg;
   private final ModelPart front_leg_right;
   private final ModelPart front_leg_left;
   private float attackStartTime = -1.0F;
   private int lastAttackTimeout = 0;
   private static final float ATTACK_DURATION = 12.0F;

   public SulfurheadModel(ModelPart root) {
      super(root.getChild("Sulfurhead"));
      this.Sulfurhead = this.root();
      this.head = this.Sulfurhead.getChild("head");
      this.left_eye = this.head.getChild("left_eye");
      this.right_eye = this.head.getChild("right_eye");
      this.torso = this.Sulfurhead.getChild("torso");
      this.back_leg = this.Sulfurhead.getChild("back_leg");
      this.front_leg_right = this.Sulfurhead.getChild("front_leg_right");
      this.front_leg_left = this.Sulfurhead.getChild("front_leg_left");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Sulfurhead = partdefinition.addOrReplaceChild("Sulfurhead", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition head = Sulfurhead.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -0.8285F, 0.65F));
      PartDefinition cube_r1 = head.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(16, 63).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -1.95F, 0.85F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition cube_r2 = head.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(60, 47).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, -4.95F, 0.85F, 0.0F, -1.5708F, 0.0F)
      );
      PartDefinition cube_r3 = head.addOrReplaceChild(
         "cube_r3",
         CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -4.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -4.7F, -0.15F, 0.0F, 0.0F, 0.0F)
      );
      PartDefinition left_eye = head.addOrReplaceChild("left_eye", CubeListBuilder.create(), PartPose.offset(2.5F, -3.95F, -0.9F));
      PartDefinition cube_r4 = left_eye.addOrReplaceChild(
         "cube_r4",
         CubeListBuilder.create().texOffs(1, 2).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, 1.5708F, 0.0F)
      );
      PartDefinition cube_r5 = left_eye.addOrReplaceChild(
         "cube_r5",
         CubeListBuilder.create().texOffs(30, 68).addBox(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, 1.5708F, 0.0F)
      );
      PartDefinition cube_r6 = left_eye.addOrReplaceChild(
         "cube_r6",
         CubeListBuilder.create().texOffs(0, 4).addBox(0.1F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.0F, 1.5708F, 0.0F)
      );
      PartDefinition right_eye = head.addOrReplaceChild("right_eye", CubeListBuilder.create(), PartPose.offset(-2.5F, -3.95F, -0.9F));
      PartDefinition cube_r7 = right_eye.addOrReplaceChild(
         "cube_r7",
         CubeListBuilder.create().texOffs(1, 0).addBox(-2.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F)
      );
      PartDefinition cube_r8 = right_eye.addOrReplaceChild(
         "cube_r8",
         CubeListBuilder.create().texOffs(68, 26).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, -1.5708F, 0.0F)
      );
      PartDefinition cube_r9 = right_eye.addOrReplaceChild(
         "cube_r9",
         CubeListBuilder.create().texOffs(2, 4).addBox(-0.1F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.0F, -1.5708F, 0.0F)
      );
      PartDefinition torso = Sulfurhead.addOrReplaceChild(
         "torso",
         CubeListBuilder.create()
            .texOffs(0, 38)
            .addBox(-4.0F, -8.9399F, -5.1074F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(36, 20)
            .addBox(-4.0F, -5.9399F, -3.1074F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(60, 58)
            .addBox(-4.0F, 5.0601F, -3.1074F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 7.9399F, 2.1074F)
      );
      PartDefinition cube_r10 = torso.addOrReplaceChild(
         "cube_r10",
         CubeListBuilder.create().texOffs(1, 1).addBox(-5.0F, -5.5F, -3.0F, 10.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -1.4399F, -2.1074F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition cube_r11 = torso.addOrReplaceChild(
         "cube_r11",
         CubeListBuilder.create().texOffs(36, 36).addBox(-4.0F, 0.5F, -2.0F, 9.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.5F, 3.5601F, -1.1074F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition back_leg = Sulfurhead.addOrReplaceChild(
         "back_leg",
         CubeListBuilder.create()
            .texOffs(28, 61)
            .addBox(-2.0F, 4.0F, 4.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(38, 14)
            .addBox(-1.5F, 7.0F, 5.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(60, 67)
            .addBox(1.0F, 10.0F, 5.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(24, 68)
            .addBox(-1.0F, 10.0F, 5.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 12.0F, 4.0F)
      );
      PartDefinition cube_r12 = back_leg.addOrReplaceChild(
         "cube_r12",
         CubeListBuilder.create().texOffs(38, 0).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.25F)),
         PartPose.offsetAndRotation(0.0F, 2.0F, 4.0F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition front_leg_right = Sulfurhead.addOrReplaceChild(
         "front_leg_right",
         CubeListBuilder.create()
            .texOffs(44, 61)
            .addBox(-2.0F, 4.0F, -8.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(50, 14)
            .addBox(-1.5F, 7.0F, -8.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(66, 0)
            .addBox(-1.0F, 10.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(66, 67)
            .addBox(1.0F, 10.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 12.0F, -1.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition cube_r13 = front_leg_right.addOrReplaceChild(
         "cube_r13",
         CubeListBuilder.create().texOffs(32, 47).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.25F)),
         PartPose.offsetAndRotation(0.0F, 2.0F, -4.0F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition front_leg_left = Sulfurhead.addOrReplaceChild(
         "front_leg_left",
         CubeListBuilder.create()
            .texOffs(0, 63)
            .addBox(-2.0F, 4.0F, -8.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(62, 14)
            .addBox(-1.5F, 7.0F, -8.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(66, 6)
            .addBox(-1.0F, 10.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(68, 20)
            .addBox(1.0F, 10.0F, -8.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(2.0F, 12.0F, -1.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition cube_r14 = front_leg_left.addOrReplaceChild(
         "cube_r14",
         CubeListBuilder.create().texOffs(0, 49).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.25F)),
         PartPose.offsetAndRotation(0.0F, 2.0F, -4.0F, 0.3927F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   @Override
   public void setupAnim(ScgunsMobRenderState state) {
      super.setupAnim(state);
      float limbSwing = state.walkAnimationPos;
      float limbSwingAmount = state.walkAnimationSpeed;
      float ageInTicks = state.ageInTicks;
      this.head.yRot = state.yRot * ((float)Math.PI / 180.0F);
      this.head.xRot = state.xRot * ((float)Math.PI / 180.0F);
      float leftEyeWobble = Mth.sin(ageInTicks * 0.15F) * 0.08F;
      float rightEyeWobble = Mth.sin(ageInTicks * 0.12F + 1.5F) * 0.08F;
      this.left_eye.xRot = leftEyeWobble;
      this.left_eye.zRot = Mth.cos(ageInTicks * 0.1F) * 0.05F;
      this.right_eye.xRot = rightEyeWobble;
      this.right_eye.zRot = Mth.cos(ageInTicks * 0.13F + 2.0F) * 0.05F;

      if (state.attacking && state.attackTimeout > 0) {
         this.animateAttackSmooth(state.attackTimeout, ageInTicks);
      } else if (this.lastAttackTimeout > 0) {
         this.animateAttackSmooth(0, ageInTicks);
      }

      if (limbSwingAmount > 0.01F) {
         float speed = 0.6662F;
         float backPhase = Mth.cos(limbSwing * speed) * limbSwingAmount;
         float frontRightPhase = Mth.cos(limbSwing * speed + 2.0944F) * limbSwingAmount;
         float frontLeftPhase = Mth.cos(limbSwing * speed + 4.1888F) * limbSwingAmount;
         this.back_leg.xRot = backPhase * 0.8F;
         this.front_leg_right.xRot = frontRightPhase * 0.8F;
         this.front_leg_left.xRot = frontLeftPhase * (state.attacking ? 0.3F : 0.8F);

         float backLift = Math.max(0.0F, Mth.sin(limbSwing * speed) * limbSwingAmount);
         float rightLift = Math.max(0.0F, Mth.sin(limbSwing * speed + 2.0944F) * limbSwingAmount);
         float leftLift = Math.max(0.0F, Mth.sin(limbSwing * speed + 4.1888F) * limbSwingAmount);
         backLift *= backLift;
         rightLift *= rightLift;
         leftLift *= leftLift;
         this.back_leg.y -= backLift * 1.2F;
         this.front_leg_right.y -= rightLift * 1.2F;
         this.front_leg_left.y -= leftLift * 1.2F;
         float bodyPitch = (backPhase - (frontRightPhase + frontLeftPhase) * 0.5F) * 0.06F;
         this.torso.xRot = bodyPitch;
         float bodySway = (frontRightPhase - frontLeftPhase) * 0.05F;
         this.torso.yRot = bodySway;
         this.torso.zRot = bodySway * 0.5F;
         float bodyBob = Mth.sin(limbSwing * speed);
         bodyBob = Math.abs(bodyBob) * bodyBob;
         this.torso.y += bodyBob * limbSwingAmount * 0.5F;
         this.head.yRot += -bodySway * 0.3F;
         this.head.xRot += -bodyPitch * 0.5F;
      }

      float breathe = Mth.sin(ageInTicks * 0.08F) * 0.03F;
      this.torso.y += breathe;
      if (limbSwingAmount < 0.01F) {
         this.head.y += Mth.sin(ageInTicks * 0.05F) * 0.5F;
      }
   }

   private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
      if (attackTimeout <= 0) {
         this.front_leg_left.xRot = 0.0F;
         this.front_leg_left.yRot = 0.0F;
         this.front_leg_left.zRot = 0.0F;
         this.attackStartTime = -1.0F;
         this.lastAttackTimeout = 0;
         return;
      }
      if (attackTimeout > this.lastAttackTimeout || this.attackStartTime < 0.0F) {
         this.attackStartTime = ageInTicks;
      }
      this.lastAttackTimeout = attackTimeout;
      float elapsedTime = ageInTicks - this.attackStartTime;
      float attackProgress = Mth.clamp(elapsedTime / ATTACK_DURATION, 0.0F, 1.0F);
      float swingCurve = Mth.sin(attackProgress * (float)Math.PI);
      this.front_leg_left.xRot = -swingCurve * 1.8F;
      this.front_leg_left.zRot = swingCurve * 0.15F;
   }
}
