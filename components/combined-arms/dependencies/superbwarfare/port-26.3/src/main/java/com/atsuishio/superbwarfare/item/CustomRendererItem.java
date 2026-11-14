package com.atsuishio.superbwarfare.item;

import net.minecraft.world.item.Item;
import com.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

public interface CustomRendererItem {
    Supplier<? extends GeoItemRenderer<? extends Item>> getRenderer();
}
