// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.blocks.generic;

import com.hbm.blocks.BlockCodecs;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class BlockPoleTop extends Block {

    public static final MapCodec<BlockPoleTop> CODEC = BlockCodecs.simpleCodec(BlockPoleTop::new);

    public BlockPoleTop(Properties props) {
        super(props);
    }

        protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
