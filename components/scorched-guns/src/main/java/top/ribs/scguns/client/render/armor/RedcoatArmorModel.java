package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.RedcoatArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class RedcoatArmorModel extends GeoModel<RedcoatArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/redcoat_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/redcoat_armor.png");
    }

    @Override
    public Identifier getAnimationResource(RedcoatArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/redcoat_armor.animation.json");
    }
}
