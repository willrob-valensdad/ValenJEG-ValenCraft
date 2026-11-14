// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.handler;

import com.hbm.items.ModItems;
import com.hbm.util.ShadyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;

public final class BobmazonSignHandler {

    private BobmazonSignHandler() {}

    public static boolean onSignUsed(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return false;
        if (!(level.getBlockEntity(pos) instanceof SignBlockEntity sign)) return false;

        SignText text = sign.getText(SignTextSlot.FRONT);
        String result =
                ShadyUtil.smoosh(
                        text.getMessages(false).get(0).getString(),
                        text.getMessages(false).get(1).getString(),
                        text.getMessages(false).get(2).getString(),
                        text.getMessages(false).get(3).getString());

        if (!ShadyUtil.HASHES.contains(result)) return false;

        level.destroyBlock(pos, false);

        ItemEntity drop =
                new ItemEntity(
                        level,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        new ItemStack(ModItems.BOBMAZON_HIDDEN));
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);

        return true;
    }
}
