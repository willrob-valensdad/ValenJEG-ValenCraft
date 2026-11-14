package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.TreatedBrassArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class TreatedBrassArmorModel extends GeoModel<TreatedBrassArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/treated_brass_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/treated_brass_armor.png");
    }

    @Override
    public Identifier getAnimationResource(TreatedBrassArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/treated_brass_armor.animation.json");
    }
}
