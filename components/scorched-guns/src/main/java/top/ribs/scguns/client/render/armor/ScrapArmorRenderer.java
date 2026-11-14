package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.ScrapArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class ScrapArmorRenderer extends FreshAnimationsGeoArmorRenderer<ScrapArmorItem> {
    public ScrapArmorRenderer() {
        super(new ScrapArmorModel());
    }
}
