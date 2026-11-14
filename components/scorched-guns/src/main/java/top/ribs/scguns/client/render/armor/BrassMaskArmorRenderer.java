package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.BrassMaskArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class BrassMaskArmorRenderer extends FreshAnimationsGeoArmorRenderer<BrassMaskArmorItem> {
    public BrassMaskArmorRenderer() {
        super(new BrassMaskArmorModel());
    }
}