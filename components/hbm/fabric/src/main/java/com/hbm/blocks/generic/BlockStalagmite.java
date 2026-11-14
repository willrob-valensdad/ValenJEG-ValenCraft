// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.blocks.generic;

import com.hbm.blocks.BlockCodecs;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class BlockStalagmite extends BlockSpike {

    public static final MapCodec<BlockStalagmite> CODEC = BlockCodecs.simpleCodec(BlockStalagmite::new);

    public BlockStalagmite(Properties props) {
        super(props, false);
    }

        protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
