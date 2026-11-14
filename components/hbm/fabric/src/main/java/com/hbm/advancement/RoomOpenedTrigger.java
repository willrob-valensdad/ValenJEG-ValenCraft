// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class RoomOpenedTrigger extends SimpleCriterionTrigger<RoomOpenedTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> true);
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player)
            implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                LootItemCondition.CODEC
                                                        .optionalFieldOf("player")
                                                        .forGetter(TriggerInstance::player))
                                        .apply(instance, TriggerInstance::new));
    }
}
