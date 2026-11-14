// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.3 removed BlockState.blocksMotion(). Vanilla caches the collision shape at
 * the origin against an empty level, and a block blocks motion when that shape
 * is not empty.
 */
public final class BlockMotion {
    private BlockMotion() {}

    public static boolean blocksMotion(BlockState state) {
        return !state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }
}
