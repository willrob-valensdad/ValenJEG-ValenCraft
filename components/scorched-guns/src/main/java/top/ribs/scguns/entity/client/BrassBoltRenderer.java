package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;

public class BrassBoltRenderer extends ArrowRenderer<BrassBoltEntity, ArrowRenderState> {
    public BrassBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return Identifier.fromNamespaceAndPath("scguns", "textures/entity/projectiles/brass_bolt.png");
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
