// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.loot;

import com.hbm.data.WorldData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public record ConfigScaledNumberProvider(Holder<ContextIntProvider> value)
        implements ContextIntProvider {

    public static final MapCodec<ConfigScaledNumberProvider> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            ContextIntProviders.CODEC
                                                    .fieldOf("value")
                                                    .forGetter(ConfigScaledNumberProvider::value))
                                    .apply(i, ConfigScaledNumberProvider::new));

    public static int scaledRolls(double base, double factor) {
        return (int) Math.max(1, Math.floor(base * factor));
    }

    private static double factor() {
        return WorldData.LOOT_AMOUNT_FACTOR.get();
    }

        public MapCodec<ConfigScaledNumberProvider> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "value", value);
    }

    @Override
    public int getIntUnsafe(LootContext context) {
        return scaledRolls(value.value().getInt(context), factor());
    }
}
