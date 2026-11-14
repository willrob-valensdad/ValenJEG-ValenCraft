package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AnthraliteArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class AnthraliteArmorModel extends GeoModel<AnthraliteArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/anthralite_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/anthralite_armor.png");
    }

    @Override
    public Identifier getAnimationResource(AnthraliteArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/anthralite_armor.animation.json");
    }
}