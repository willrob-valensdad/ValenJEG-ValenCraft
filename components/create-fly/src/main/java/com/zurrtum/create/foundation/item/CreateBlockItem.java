package com.zurrtum.create.foundation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Retains Create's placement callback at the native pre-component-loading stage. */
public class CreateBlockItem extends BlockItem {
    public CreateBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player,
                                              ItemStack stack, BlockState state) {
        return BlockItem.updateCustomBlockEntityTag(level, player, pos, stack);
    }
}
