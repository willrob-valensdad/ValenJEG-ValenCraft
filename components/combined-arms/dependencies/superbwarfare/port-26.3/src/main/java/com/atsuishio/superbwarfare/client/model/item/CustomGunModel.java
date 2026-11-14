package com.atsuishio.superbwarfare.client.model.item;

import com.atsuishio.superbwarfare.client.molang.MolangVariable;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.atsuishio.superbwarfare.resource.ModelResource;
import com.atsuishio.superbwarfare.resource.gun.DefaultGunResource;
import com.atsuishio.superbwarfare.resource.gun.GunResource;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MolangQueries;
import com.geckolib.model.GeoModel;

import java.util.function.DoubleSupplier;

public abstract class CustomGunModel<T extends GunGeoItem & GeoAnimatable> extends GeoModel<T> {
    public ItemStack gunItemStack;

    @Override
    public Identifier getAnimationResource(T animatable) {
        return getModel(animatable).animation;
    }

    @Override
    public Identifier getModelResource(T animatable) {
        return getModel(animatable).model;
    }

    @Override
    public Identifier getTextureResource(T animatable) {
        return getModel(animatable).texture;
    }

    public Identifier getLODModelResource(T animatable) {
        return getModel(animatable).getLODModel(Integer.MAX_VALUE);
    }

    public Identifier getLODTextureResource(T animatable) {
        return getModel(animatable).getLODTexture(Integer.MAX_VALUE);
    }

    protected ModelResource getModel(T animatable) {
        if (this.gunItemStack != null && this.gunItemStack.getItem() instanceof GunGeoItem) {
            return GunResource.from(this.gunItemStack).compute().getModel();
        }

        return getResource(animatable).getModel();
    }

    protected DefaultGunResource getResource(T animatable) {
        return GunResource.getDefault(animatable);
    }

    @Override
    public void applyMolangQueries(AnimationTest<T> animationState, double animTime) {
        Minecraft mc = Minecraft.getInstance();

        set(MolangQueries.LIFE_TIME, () -> animTime / 20d);

        if (mc.level != null) {
            set(MolangQueries.ACTOR_COUNT, mc.level::getEntityCount);
            set(MolangQueries.TIME_OF_DAY, () -> mc.level.getDayTime() / 24000f);
            set(MolangQueries.MOON_PHASE, mc.level::getMoonPhase);
        }

        // GunData
        var player = mc.player;
        if (player == null) {
            resetQueryValue();
            return;
        }

        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) {
            resetQueryValue();
            return;
        }

        var item = animationState.getData(DataTickets.ITEMSTACK);
        if (item == null || GeoItem.getId(item) != GeoItem.getId(stack)) {
            resetQueryValue();
            return;
        }

        if (animationState.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            resetQueryValue();
            return;
        }

        var data = GunData.from(stack);
        set(MolangVariable.SBW_IS_EMPTY, () -> data.isEmpty.get() ? 1 : 0);
        set(MolangVariable.SBW_SYSTEM_TIME, System::currentTimeMillis);
    }

    private static void set(String key, DoubleSupplier value) {
        MathParser.setVariable(key, value);
    }

    private void resetQueryValue() {
        set(MolangVariable.SBW_IS_EMPTY, () -> 0);
        set(MolangVariable.SBW_SYSTEM_TIME, () -> 0);
    }

    public boolean shouldCancelRender(ItemStack stack, AnimationTest<T> animationState) {
        if (!(stack.getItem() instanceof GunItem)) return true;
        var item = animationState.getData(DataTickets.ITEMSTACK);
        if (item == null || GeoItem.getId(item) != GeoItem.getId(stack)) return true;
        return animationState.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }
}
