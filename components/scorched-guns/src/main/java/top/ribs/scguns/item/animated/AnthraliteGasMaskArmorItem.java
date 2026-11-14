package top.ribs.scguns.item.animated;

import net.minecraft.core.Holder;
import top.ribs.scguns.compat.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.object.PlayState;
import top.ribs.scguns.client.render.armor.AnthraliteGasMaskArmorRenderer;
import top.ribs.scguns.init.ModArmorMaterials;

import java.util.function.Consumer;

public class AnthraliteGasMaskArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public AnthraliteGasMaskArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, ModArmorMaterials.withDurability(pProperties, pMaterial, pType));
    }
    @Override
    public void createGeoRenderer(Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        top.ribs.scguns.client.render.armor.ScGunsGeoArmorRenderProvider.accept(consumer, AnthraliteGasMaskArmorRenderer::new);
    }

    private PlayState predicate(AnimationTest animationState) {
        animationState.controller().setAnimation(RawAnimation.begin().thenLoop("animation.anthralite_gas_mask.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>( "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
