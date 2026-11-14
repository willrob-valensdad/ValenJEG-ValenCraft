package top.ribs.scguns.client.render.armor;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** GeckoLib 5 armor renderer provider for Minecraft 26.2. */
public final class ScGunsGeoArmorRenderProvider {
    private ScGunsGeoArmorRenderProvider() {}

    public static <T extends Item & GeoItem> void accept(Consumer<GeoRenderProvider> consumer,
                                                         Supplier<? extends GeoArmorRenderer<T, ?>> rendererFactory) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<T, ?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.renderer == null) this.renderer = rendererFactory.get();
                return this.renderer;
            }
        });
    }
}
