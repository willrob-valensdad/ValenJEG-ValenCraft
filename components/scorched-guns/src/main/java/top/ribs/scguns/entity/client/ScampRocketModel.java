package top.ribs.scguns.entity.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Minecraft 26.2 render-state model for the Scamp Tank rocket. */
public class ScampRocketModel extends EntityModel<ScampRocketRenderState> {
    public ScampRocketModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -6.0714F, -2.0541F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 17).addBox(0.0F, -4.0714F, 2.9459F, 0.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 14).addBox(-2.5F, 3.9286F, -2.0541F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(20, 0).addBox(-2.0F, 2.9286F, -1.5541F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 20).addBox(-2.0F, -9.0714F, -1.5541F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.3214F, -0.6656F, -1.5708F, 0.0F, 1.5708F));

        main.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(20, 5).addBox(0.0F, -4.5F, -1.5F, 0.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5607F, 0.4286F, -3.1148F, 0.0F, -0.7854F, 0.0F));

        main.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(0.0F, -4.5F, -1.5F, 0.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5607F, 0.4286F, -3.1148F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
