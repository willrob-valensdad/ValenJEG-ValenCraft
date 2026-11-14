// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 26.3 serializes a block by its registry id. {@code codec()} still returns a
 * {@link MapCodec} so existing block contracts keep a real encode/decode path
 * onto {@link BuiltInRegistries#BLOCK}.
 */
public final class BlockCodecs {
    private BlockCodecs() {}

    public static <B extends Block> MapCodec<B> simpleCodec(
            Function<BlockBehaviour.Properties, B> factory) {
        if (factory == null) {
            throw new IllegalArgumentException("block factory");
        }
        Codec<B> named =
                BuiltInRegistries.BLOCK
                        .byNameCodec()
                        .xmap(
                                block -> {
                                    @SuppressWarnings("unchecked")
                                    B typed = (B) block;
                                    return typed;
                                },
                                Function.identity());
        return named.fieldOf("Name");
    }

    public static BlockBehaviour.Properties flowerProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .noOcclusion()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.POPPED);
    }

    public static MapCodec<BlockBehaviour.Properties> flowerPropertiesCodec() {
        return MapCodec.unit(BlockCodecs::flowerProperties);
    }

    public static BlockBehaviour.Properties tallPlantProperties() {
        return flowerProperties().randomTicks();
    }

    public static MapCodec<BlockBehaviour.Properties> tallPlantPropertiesCodec() {
        return MapCodec.unit(BlockCodecs::tallPlantProperties);
    }
}
