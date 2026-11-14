package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AnthraliteGasMaskArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class AnthraliteGasMaskArmorRenderer extends FreshAnimationsGeoArmorRenderer<AnthraliteGasMaskArmorItem> {
    public AnthraliteGasMaskArmorRenderer() {
        super(new AnthraliteGasMaskArmorModel());
    }
}