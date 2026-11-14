package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.DiamondSteelArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class DiamondSteelArmorRenderer extends FreshAnimationsGeoArmorRenderer<DiamondSteelArmorItem> {
    public DiamondSteelArmorRenderer() {
        super(new DiamondSteelArmorModel());
    }
}