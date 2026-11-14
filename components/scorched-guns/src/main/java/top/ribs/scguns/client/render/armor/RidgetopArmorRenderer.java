package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AnthraliteGasMaskArmorItem;
import top.ribs.scguns.item.animated.RidgetopArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class RidgetopArmorRenderer extends FreshAnimationsGeoArmorRenderer<RidgetopArmorItem> {
    public RidgetopArmorRenderer() {
        super(new RidgetopArmorModel());
    }
}