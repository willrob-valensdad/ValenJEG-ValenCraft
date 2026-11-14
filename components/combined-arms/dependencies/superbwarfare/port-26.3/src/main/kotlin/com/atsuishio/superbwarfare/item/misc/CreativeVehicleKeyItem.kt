package com.atsuishio.superbwarfare.item.misc

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class CreativeVehicleKeyItem : VehicleKeyItem(Properties().stacksTo(1).rarity(Rarity.EPIC)) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        tooltip.add(Component.translatable("des.superbwarfare.creative_vehicle_key").withStyle(ChatFormatting.GRAY))
    }

    override fun use(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResult {
        return InteractionResult.FAIL
    }

    override fun onInteractVehicle(
        vehicle: VehicleEntity,
        stack: ItemStack,
        player: Player,
        hand: InteractionHand
    ): InteractionResult {
        if (!vehicle.passengers.isEmpty()) {
            player.sendOverlayMessage(
                Component.translatable("tips.superbwarfare.vehicle.lock_not_empty")
                    .withStyle(ChatFormatting.RED))
            return InteractionResult.FAIL
        }

        if (!vehicle.locked) {
            vehicle.lastDriverUUID = player.stringUUID
            vehicle.locked = true

            player.sendOverlayMessage(
                Component.translatable(
                    "tips.superbwarfare.vehicle.lock_vehicle",
                    vehicle.displayName
                ).withStyle(ChatFormatting.GREEN))
            return InteractionResult.SUCCESS
        } else {
            vehicle.lastDriverUUID = player.stringUUID
            vehicle.locked = false

            player.sendOverlayMessage(
                Component.translatable(
                    "tips.superbwarfare.vehicle.unlock_vehicle",
                    vehicle.displayName
                ).withStyle(ChatFormatting.GREEN))
            return InteractionResult.SUCCESS
        }
    }
}