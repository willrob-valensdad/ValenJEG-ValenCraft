package com.atsuishio.superbwarfare.item.ammo

import com.atsuishio.superbwarfare.data.gun.Ammo
import com.atsuishio.superbwarfare.init.ModAttachments
import com.atsuishio.superbwarfare.init.ModItems
import com.atsuishio.superbwarfare.init.ModSounds
import com.atsuishio.superbwarfare.tools.InventoryTool
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

open class AmmoSupplierItem(val type: Ammo, val ammoToAdd: Int, properties: Properties) : Item(properties) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(Component.translatable("des.superbwarfare.ammo_supplier").withStyle(ChatFormatting.AQUA))
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        var count = stack.count

        if (player.isShiftKeyDown) {
            count = InventoryTool.countItem(player, stack.item)
        }

        val offhandItem = player.offhandItem

        val addedCount = if (offhandItem.`is`(ModItems.AMMO_BOX)) {
            val canAddAmount = type.ammoBoxLimit - type.get(offhandItem)
            val toAddCount = (canAddAmount / ammoToAdd).coerceAtMost(count)
            if (toAddCount <= 0) {
                player.sendOverlayMessage(
                    Component.translatable("item.superbwarfare.ammo_supplier.fail").withStyle(ChatFormatting.RED))
                return InteractionResult.FAIL
            }

            this.type.add(offhandItem, ammoToAdd * toAddCount)

            toAddCount
        } else {
            val capability = ModAttachments.PLAYER_VARIABLE.get(player).watch()

            val canAddAmount = type.limit - type.get(capability)
            val toAddCount = (canAddAmount / ammoToAdd).coerceAtMost(count)
            if (toAddCount <= 0) {
                player.sendOverlayMessage(
                    Component.translatable("item.superbwarfare.ammo_supplier.fail").withStyle(ChatFormatting.RED))
                return InteractionResult.FAIL
            }

            this.type.add(capability, ammoToAdd * toAddCount)
            capability.sync(player)

            toAddCount
        }

        player.cooldowns.addCooldown(this, 10)

        if (!player.isCreative) {
            InventoryTool.consumeItem(player, stack.item, addedCount)
        }

        if (!level.isClientSide()) {
            player.sendOverlayMessage(
                Component.translatable(
                    "item.superbwarfare.ammo_supplier.supply",
                    Component.translatable(this.type.translationKey),
                    ammoToAdd * count
                ))
            level.playSound(null, player.blockPosition(), ModSounds.BULLET_SUPPLY, SoundSource.PLAYERS, 1f, 1f)
        }

        return InteractionResult.SUCCESS
    }
}
