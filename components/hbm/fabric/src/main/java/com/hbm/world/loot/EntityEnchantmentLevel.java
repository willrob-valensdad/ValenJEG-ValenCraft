// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public record EntityEnchantmentLevel(LootContext.EntityTarget entity, Holder<Enchantment> enchantment)
        implements ContextIntProvider {

    public static final MapCodec<EntityEnchantmentLevel> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            LootContext.EntityTarget.CODEC
                                                    .fieldOf("entity")
                                                    .forGetter(EntityEnchantmentLevel::entity),
                                            Enchantment.CODEC
                                                    .fieldOf("enchantment")
                                                    .forGetter(EntityEnchantmentLevel::enchantment))
                                    .apply(i, EntityEnchantmentLevel::new));

        public MapCodec<EntityEnchantmentLevel> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {}

    @Override
    public int getIntUnsafe(LootContext context) {
        return context.getOptional(entity.contextParam()) instanceof LivingEntity living
                ? EnchantmentHelper.getEnchantmentLevel(enchantment, living)
                : 0;
    }
}
