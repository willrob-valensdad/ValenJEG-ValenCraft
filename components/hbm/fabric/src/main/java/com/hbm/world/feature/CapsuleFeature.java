// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.MapCodec;

import com.hbm.blockentity.machine.storage.BlockEntitySoyuzCapsule;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.SoyuzCapsule;
import com.hbm.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CapsuleFeature extends HbmFeature<HbmUnit> {
    private static final int DEPTH = 4;

    public static final MapCodec<CapsuleFeature> CODEC = HbmFeature.unitCodec(CapsuleFeature::new);

    public CapsuleFeature() {
        super(HbmUnit.INSTANCE);
    }

        public MapCodec<CapsuleFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean placeConfigured(HbmPlace<HbmUnit> ctx) {
        var level = ctx.level();
        BlockPos site = ctx.origin().below(DEPTH);
        BlockPos lid = site.above();
        if (!level.getBlockState(lid).isFaceSturdy(level, lid, Direction.UP)) return false;

        level.setBlock(
                site,
                ModBlocks.SOYUZ_CAPSULE
                        .get()
                        .defaultBlockState()
                        .setValue(SoyuzCapsule.RUSTED, true),
                Block.UPDATE_CLIENTS);

        if (level.getBlockEntity(site) instanceof BlockEntitySoyuzCapsule capsule) {
            capsule.setItem(
                    ctx.random().nextInt(BlockEntitySoyuzCapsule.SLOT_COUNT),
                    new ItemStack(ModItems.RECORD_GLASS.get()));
        }
        return true;
    }
}
