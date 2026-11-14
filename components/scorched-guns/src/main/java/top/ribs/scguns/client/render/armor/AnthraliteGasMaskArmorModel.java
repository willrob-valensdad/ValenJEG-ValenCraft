package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AnthraliteGasMaskArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class AnthraliteGasMaskArmorModel extends GeoModel<AnthraliteGasMaskArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/anthralite_respirator.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/anthralite_respirator.png");
    }

    @Override
    public Identifier getAnimationResource(AnthraliteGasMaskArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/anthralite_gas_mask.animation.json");
    }
}