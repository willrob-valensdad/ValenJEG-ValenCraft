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
import net.minecraft.util.StringRepresentable;

public class BossKilledTrigger extends SimpleCriterionTrigger<BossKilledTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Kind kind) {
        this.trigger(player, instance -> instance.kind() == kind);
    }

    public enum Kind implements StringRepresentable {
        MASKMAN("maskman"),
        MELTDOWN("meltdown"),
        WORM("worm"),
        UFO("ufo");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Kind kind)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                LootItemCondition.CODEC
                                                        .optionalFieldOf("player")
                                                        .forGetter(TriggerInstance::player),
                                                Kind.CODEC
                                                        .fieldOf("kind")
                                                        .forGetter(TriggerInstance::kind))
                                        .apply(instance, TriggerInstance::new));
    }
}
