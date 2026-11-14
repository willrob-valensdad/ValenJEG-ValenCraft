package com.zurrtum.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class AllFuelTimes {
    private static final ContextKeySet FUEL_CONTEXT = new ContextKeySet.Builder()
        .required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE)
        .optional(LootContextParams.BLOCK_ENTITY).build();

    public static Item.Properties properties(Item.Properties properties, int ticks) {
        return properties.component(DataComponents.COOKING_FUEL,
            new CookingFuel(new ResolvableInt.Constant(ticks), new ResolvableFloat.Constant(1.0F)));
    }

    public static int burnDuration(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.has(DataComponents.COOKING_FUEL) || !(level instanceof ServerLevel serverLevel)) return 0;
        LootParams params = new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(pos))
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos))
            .create(FUEL_CONTEXT);
        return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime,
            new LootContext.Builder(params).create(Optional.empty()), 0);
    }
}
