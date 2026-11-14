package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.RedcoatArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class RedcoatArmorRenderer extends FreshAnimationsGeoArmorRenderer<RedcoatArmorItem> {
    public RedcoatArmorRenderer() {
        super(new RedcoatArmorModel());
    }
}
