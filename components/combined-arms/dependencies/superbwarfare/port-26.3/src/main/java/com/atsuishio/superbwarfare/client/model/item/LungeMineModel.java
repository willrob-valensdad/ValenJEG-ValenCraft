package com.atsuishio.superbwarfare.client.model.item;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.item.LungeMine;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;

public class LungeMineModel extends GeoModel<LungeMine> {

    @Override
    public Identifier getAnimationResource(LungeMine animatable) {
        return Mod.loc("animations/lunge_mine.animation.json");
    }

    @Override
    public Identifier getModelResource(LungeMine animatable) {
        return Mod.loc("geo/lunge_mine.geo.json");
    }

    @Override
    public Identifier getTextureResource(LungeMine animatable) {
        return Mod.loc("textures/item/lunge_mine.png");
    }

    @Override
    public void setCustomAnimations(LungeMine animatable, long instanceId, AnimationTest animationState) {
        GeoBone camera = getAnimationProcessor().getBone("camera");
        ClientEventHandler.handleReloadShake(Mth.RAD_TO_DEG * camera.getRotX(), Mth.RAD_TO_DEG * camera.getRotY(), Mth.RAD_TO_DEG * camera.getRotZ());
    }
}
