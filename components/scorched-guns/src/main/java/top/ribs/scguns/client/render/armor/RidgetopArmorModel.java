package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.RidgetopArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class RidgetopArmorModel extends GeoModel<RidgetopArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/ridgetop.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/ridgetop.png");
    }

    @Override
    public Identifier getAnimationResource(RidgetopArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/ridgetop.animation.json");
    }
}