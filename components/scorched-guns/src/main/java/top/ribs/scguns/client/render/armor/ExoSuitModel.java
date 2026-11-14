package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

import com.geckolib.renderer.base.GeoRenderState;
public class ExoSuitModel extends GeoModel<ExoSuitItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/exo_suit.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/exo_suit.png");
    }

    @Override
    public Identifier getAnimationResource(ExoSuitItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/exo_suit.animation.json");
    }
}