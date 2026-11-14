package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AdrienArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class AdrienArmorRenderer extends FreshAnimationsGeoArmorRenderer<AdrienArmorItem> {
    public AdrienArmorRenderer() {
        super(new AdrienArmorModel());
    }
}