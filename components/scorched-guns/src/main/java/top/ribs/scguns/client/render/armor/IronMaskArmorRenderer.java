package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.IronMaskArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class IronMaskArmorRenderer extends FreshAnimationsGeoArmorRenderer<IronMaskArmorItem> {
    public IronMaskArmorRenderer() {
        super(new IronMaskArmorModel());
    }
}
