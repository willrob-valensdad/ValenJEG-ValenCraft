package top.ribs.scguns.client.render.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.NetheriteGasMaskArmorItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class NetheriteGasMaskArmorRenderer extends FreshAnimationsGeoArmorRenderer<NetheriteGasMaskArmorItem> {
    public NetheriteGasMaskArmorRenderer() {
        super(new NetheriteGasMaskArmorModel());
    }
}