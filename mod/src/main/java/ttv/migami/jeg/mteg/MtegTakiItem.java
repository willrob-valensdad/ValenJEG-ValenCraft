package ttv.migami.jeg.mteg;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/** Original MTEG ModItems.TAKI (5d7fb0f, GPLv3), native food/consumable components. */
public final class MtegTakiItem extends Item {
    public MtegTakiItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(4).saturationModifier(.1F).build(),
                Consumable.builder().consumeSeconds(.8F).onConsume(
                        new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,40,1),1F)).build()));
    }
}
