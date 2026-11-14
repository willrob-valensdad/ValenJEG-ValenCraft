// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.placement;

import com.hbm.data.DataGroups;
import com.hbm.data.WorldData;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public final class WorldConfigPlacement implements PlacementFilter {
    public static final MapCodec<WorldConfigPlacement> CODEC =
            WorldData.WORLDGEN_SWITCH
                    .listOf()
                    .fieldOf("switches")
                    .xmap(WorldConfigPlacement::new, placement -> placement.switches);
    private final List<DataGroups.Param<Boolean>> switches;

    public WorldConfigPlacement(List<DataGroups.Param<Boolean>> switches) {
        this.switches = List.copyOf(switches);
    }

    public static void register() {
        FabricRegistrar.registerPlacementModifierType("world_config", CODEC);
    }

    public boolean open() {
        for (DataGroups.Param<Boolean> gate : switches) {
            if (!gate.get()) return false;
        }
        return true;
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return open();
    }

        public MapCodec<WorldConfigPlacement> codec() {
        return CODEC;
    }
}
