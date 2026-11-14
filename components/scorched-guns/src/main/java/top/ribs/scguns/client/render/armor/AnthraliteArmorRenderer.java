package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AdrienArmorItem;
import top.ribs.scguns.item.animated.AnthraliteArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class AnthraliteArmorRenderer extends FreshAnimationsGeoArmorRenderer<AnthraliteArmorItem> {
    public AnthraliteArmorRenderer() {
        super(new AnthraliteArmorModel());
    }
}