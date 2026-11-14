package top.ribs.scguns.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class FuelItem extends Item {
    private int burnTime = 0;

    public FuelItem(Properties pProperties, int burnTime) {
        super(pProperties.component(net.minecraft.core.component.DataComponents.COOKING_FUEL, new net.minecraft.world.item.component.CookingFuel(new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(burnTime), new net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant(1.0F))));
        this.burnTime = burnTime;
    }

}
