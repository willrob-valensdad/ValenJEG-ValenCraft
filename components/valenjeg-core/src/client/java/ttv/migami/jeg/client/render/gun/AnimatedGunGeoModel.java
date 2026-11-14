package ttv.migami.jeg.client.render.gun;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.item.AnimatedGunItem;

public final class AnimatedGunGeoModel extends GeoModel<AnimatedGunItem> {
    private static Identifier modelPath(String gunId) {
        // GeckoLib v5 resolves this against assets/<modid>/geckolib/models/<path>.geo.json
        return Reference.id("item/gun/" + gunId);
    }

    private static Identifier animPath(String gunId) {
        // GeckoLib v5 resolves this against assets/<modid>/geckolib/animations/<path>.animation.json
        // IMPORTANT: do not include ".animation" here; GeckoLib appends ".animation.json" internally.
        return Reference.id("item/" + gunId);
    }

    private static String gunIdFromState(GeoRenderState renderState) {
        Object item = renderState.hasGeckolibData(AnimatedGunRenderer.ANIMATED_ITEM) ? renderState.getGeckolibData(AnimatedGunRenderer.ANIMATED_ITEM) : null;
        if (item instanceof AnimatedGunItem gun) {
            return gun.getStats().id().getPath();
        }
        return "abstract_gun";
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelPath(gunIdFromState(renderState));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        ItemStack stack = renderState.getOrDefaultGeckolibData(AnimatedGunRenderer.ITEM_STACK, ItemStack.EMPTY);
        return GunCosmeticTextures.gunTexture(stack, gunIdFromState(renderState));
    }

    @Override
    public Identifier getAnimationResource(AnimatedGunItem animatable) {
        return animPath(animatable.getStats().id().getPath());
    }
}
