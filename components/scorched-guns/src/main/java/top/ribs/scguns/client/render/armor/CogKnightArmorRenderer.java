package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.CogKnightArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class CogKnightArmorRenderer extends FreshAnimationsGeoArmorRenderer<CogKnightArmorItem> {
    public CogKnightArmorRenderer() {
        super(new CogKnightArmorModel());
    }
}