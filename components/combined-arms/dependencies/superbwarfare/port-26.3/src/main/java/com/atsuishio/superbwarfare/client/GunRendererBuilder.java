package com.atsuishio.superbwarfare.client;

import com.atsuishio.superbwarfare.client.renderer.CustomGunRenderer;
import com.atsuishio.superbwarfare.client.renderer.SimpleGunRenderer;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;

import java.util.function.Supplier;

public class GunRendererBuilder {

    public static <T extends GunGeoItem & GeoAnimatable> Supplier<? extends CustomGunRenderer<T>> simple(Supplier<GeoModel<T>> model) {
        return () -> new SimpleGunRenderer<>(model.get());
    }

}
