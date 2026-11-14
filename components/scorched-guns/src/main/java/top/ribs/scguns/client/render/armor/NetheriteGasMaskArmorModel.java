package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.NetheriteGasMaskArmorItem;

import com.geckolib.renderer.base.GeoRenderState;
public class NetheriteGasMaskArmorModel extends GeoModel<NetheriteGasMaskArmorItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/models/netherite_respirator.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/armor/netherite_respirator.png");
    }

    @Override
    public Identifier getAnimationResource(NetheriteGasMaskArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geckolib/animations/netherite_gas_mask.animation.json");
    }
}