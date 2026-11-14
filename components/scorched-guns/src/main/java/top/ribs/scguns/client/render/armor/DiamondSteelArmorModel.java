package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.DiamondSteelArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class DiamondSteelArmorModel extends GeoModel<DiamondSteelArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/diamond_steel_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/diamond_steel_armor.png");
    }

    @Override
    public Identifier getAnimationResource(DiamondSteelArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/diamond_steel_armor.animation.json");
    }
}