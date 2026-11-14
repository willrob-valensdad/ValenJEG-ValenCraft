package earth.terrarium.adastra.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import net.msrandom.multiplatform.annotations.Actual;

import java.util.function.Supplier;

public class PlatformUtilsActual {

    @Actual
    public static Entity teleportToDimension(Entity entity, ServerLevel level, TeleportTransition transition) {
        return entity.teleport(transition);
    }

    @Actual
    public static Supplier<Item> createSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties) {
        return EggUtil.createEgg(type, primaryColor, secondaryColor, properties);
    }

    @Actual
    public static int getBurnTime(ItemStack burnable) {
        CookingFuel fuel = burnable.get(DataComponents.COOKING_FUEL);
        if (fuel == null) return 0;
        ResolvableInt time = fuel.burnTime();
        if (time instanceof ConstantValue constant) return constant.value();
        return time.get(null, 0);
    }
}
