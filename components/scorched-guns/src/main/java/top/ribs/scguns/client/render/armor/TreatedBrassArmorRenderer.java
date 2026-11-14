package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.TreatedBrassArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class TreatedBrassArmorRenderer extends FreshAnimationsGeoArmorRenderer<TreatedBrassArmorItem> {
    public TreatedBrassArmorRenderer() {
        super(new TreatedBrassArmorModel());
    }
}
