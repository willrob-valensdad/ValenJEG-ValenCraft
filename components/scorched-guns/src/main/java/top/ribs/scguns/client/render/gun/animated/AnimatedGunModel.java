package top.ribs.scguns.client.render.gun.animated;

import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * GeckoLib 5.5 item model bridge for Scorched Guns animated weapons.
 * Model/texture resources are render-state based in GeckoLib 5; animations
 * remain keyed by the animatable item instance.
 */
public class AnimatedGunModel extends DefaultedItemGeoModel<AnimatedGunItem> {
    private final String modelPath;

    public AnimatedGunModel(Identifier path) {
        super(path);
        this.modelPath = path.getPath();
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("scguns", "item/gun/" + this.modelPath);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("scguns", "textures/animated/gun/" + this.modelPath + ".png");
    }

    @Override
    public Identifier getAnimationResource(AnimatedGunItem gunItem) {
        return Identifier.fromNamespaceAndPath("scguns", "item/" + this.modelPath);
    }
}
