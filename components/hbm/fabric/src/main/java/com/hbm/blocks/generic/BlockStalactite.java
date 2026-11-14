// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.blocks.generic;

import com.hbm.blocks.BlockCodecs;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class BlockStalactite extends BlockSpike {

    public static final MapCodec<BlockStalactite> CODEC = BlockCodecs.simpleCodec(BlockStalactite::new);

    public BlockStalactite(Properties props) {
        super(props, true);
    }

        protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
