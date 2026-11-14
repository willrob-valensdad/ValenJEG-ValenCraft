// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.placement;

import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.world.gen.WorldgenHeight;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public final class LightBlockingHeightPlacement implements PlacementModifier {
    public static final LightBlockingHeightPlacement INSTANCE = new LightBlockingHeightPlacement();
    public static final MapCodec<LightBlockingHeightPlacement> CODEC = MapCodec.unit(INSTANCE);
    private LightBlockingHeightPlacement() {}

    public static void register() {
        FabricRegistrar.registerPlacementModifierType("light_blocking_height", CODEC);
    }

    @Override
    public void modify(
            PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int x = origin.getX(), z = origin.getZ();
        int height = WorldgenHeight.lightBlocking(context.getLevel(), x, z);
        if (height > context.getMinY()) output.accept(new BlockPos(x, height, z));
    }

        public MapCodec<LightBlockingHeightPlacement> codec() {
        return CODEC;
    }
}
