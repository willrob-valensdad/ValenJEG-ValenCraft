package top.ribs.scguns.item.animated;

import net.minecraft.core.Holder;
import top.ribs.scguns.compat.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.cache.animation.Animation;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import top.ribs.scguns.client.render.armor.ScrapArmorRenderer;
import top.ribs.scguns.init.ModArmorMaterials;

import java.util.function.Consumer;

public class ScrapArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public ScrapArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, ModArmorMaterials.withDurability(properties, material, type));
    }

    @Override
    public void createGeoRenderer(Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        top.ribs.scguns.client.render.armor.ScGunsGeoArmorRenderProvider.accept(consumer, ScrapArmorRenderer::new);
    }

    private PlayState predicate(AnimationTest<ScrapArmorItem> animationState) {
        animationState.controller().setAnimation(RawAnimation.begin().thenLoop("animation.scrap_armor.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>("controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
