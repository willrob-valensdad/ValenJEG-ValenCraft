package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.HumanoidArm;
import top.ribs.scguns.item.GunItem;

public class LateRaidHumanoidRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, LateRaidHumanoidModel> {
    private final Identifier texture;
    private final float scale;

    public LateRaidHumanoidRenderer(EntityRendererProvider.Context context, Identifier texture, float shadowRadius, float scale) {
        super(context, new LateRaidHumanoidModel(context.bakeLayer(ModModelLayers.LATE_RAID_HUMANOID_LAYER)), shadowRadius);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(T mob, HumanoidArm arm) {
        if (arm == HumanoidArm.RIGHT && mob.getMainHandItem().getItem() instanceof GunItem) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return this.texture;
    }

    @Override
    protected void scale(HumanoidRenderState state, PoseStack poseStack) {
        if (this.scale != 1.0F) {
            poseStack.scale(this.scale, this.scale, this.scale);
        }
    }
}
