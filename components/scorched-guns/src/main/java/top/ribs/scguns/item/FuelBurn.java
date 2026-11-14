package top.ribs.scguns.item;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
/** Burn time stored as the 26.3 cooking-fuel component, including vanilla referenced fuels. */
public final class FuelBurn {
    private FuelBurn() {}
    public static int ticks(ItemStack stack, Level level) {
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null || fuel.burnTime() == null) return 0;
        if (fuel.burnTime() instanceof ResolvableInt.Constant constant) return constant.value();
        if (!(level instanceof ServerLevel server)) return 0;
        LootParams params = new LootParams.Builder(server).create(LootContextParamSets.EMPTY);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        return fuel.burnTime().get(context, 0);
    }
}
