package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AdrienArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class AdrienArmorModel extends GeoModel<AdrienArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/adrien_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/adrien_armor.png");
    }

    @Override
    public Identifier getAnimationResource(AdrienArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/adrien_armor.animation.json");
    }
}