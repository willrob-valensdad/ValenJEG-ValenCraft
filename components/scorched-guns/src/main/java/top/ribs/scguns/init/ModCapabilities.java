package top.ribs.scguns.init;

import top.ribs.scguns.compat.transfer.LegacyEnergyTransferBridge;
import top.ribs.scguns.compat.transfer.LegacyItemTransferBridge;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.ribs.scguns.item.AirCanisterItem;
import top.ribs.scguns.item.CreativeAirCanisterItem;
import top.ribs.scguns.item.EnergyGunItem;
import top.ribs.scguns.item.animated.AnimatedEnergyGunItem;
import top.ribs.scguns.item.exosuit.ExoSuitCoreItem;

public class ModCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.CRYONITER.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler())
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.THERMOLITH.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler())
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.VENT_COLLECTOR.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.LIGHTNING_BATTERY.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.LIGHTNING_BATTERY.get(),
                (blockEntity, side) -> LegacyEnergyTransferBridge.wrap(blockEntity.getEnergyStorage(side), blockEntity.getEnergyStorage(null))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.POLAR_GENERATOR.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.POLAR_GENERATOR.get(),
                (blockEntity, side) -> LegacyEnergyTransferBridge.wrap(blockEntity.getEnergyStorage(side), blockEntity.getEnergyStorage(null))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.MACERATOR.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.MECHANICAL_PRESS.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.POWERED_MACERATOR.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.POWERED_MACERATOR.get(),
                (blockEntity, side) -> LegacyEnergyTransferBridge.wrap(blockEntity.getEnergyStorage(side), blockEntity.getEnergyStorage(null))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.POWERED_MECHANICAL_PRESS.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.POWERED_MECHANICAL_PRESS.get(),
                (blockEntity, side) -> LegacyEnergyTransferBridge.wrap(blockEntity.getEnergyStorage(side), blockEntity.getEnergyStorage(null))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.SHOCK_COIL.get(),
                (blockEntity, side) -> LegacyEnergyTransferBridge.wrap(blockEntity.getEnergyStorage(side), blockEntity.getEnergyStorage(null))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.AUTO_TURRET.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(side != Direction.UP ? blockEntity.getItemStackHandler() : null)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.BASIC_TURRET.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(side != Direction.UP ? blockEntity.getItemStackHandler() : null)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.SHOTGUN_TURRET.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(side != Direction.UP ? blockEntity.getItemStackHandler() : null)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.SNIPER_TURRET.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(side != Direction.UP ? blockEntity.getItemStackHandler() : null)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.AMMO_MODULE.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemStackHandler())
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.SHELL_CATCHER_MODULE.get(),
                (blockEntity, side) -> LegacyItemTransferBridge.wrap(blockEntity.getItemStackHandler())
        );
        event.registerItem(
                Capabilities.Energy.ITEM,
                (stack, context) -> stack.getItem() instanceof ExoSuitCoreItem core
                        ? LegacyEnergyTransferBridge.customData(context, "Energy", core.getCapacity())
                        : null,
                ModItems.EXO_SUIT_CORE.get(),
                ModItems.ADVANCED_EXO_SUIT_CORE.get()
        );
        event.registerItem(
                Capabilities.Energy.ITEM,
                (stack, context) -> {
                    if (stack.getItem() instanceof EnergyGunItem energyGun) {
                        return LegacyEnergyTransferBridge.customData(context, "Energy", energyGun.getCapacity());
                    }
                    if (stack.getItem() instanceof AnimatedEnergyGunItem animatedEnergyGun) {
                        return LegacyEnergyTransferBridge.customData(context, "Energy", animatedEnergyGun.getCapacity());
                    }
                    return null;
                },
                ModItems.SCRATCHES.get(),
                ModItems.GALE.get(),
                ModItems.UMAX_PISTOL.get(),
                ModItems.VENTURI.get()
        );
        event.registerItem(
                Capabilities.Energy.ITEM,
                (stack, context) -> {
                    if (stack.getItem() instanceof CreativeAirCanisterItem creativeCanister) {
                        return LegacyEnergyTransferBridge.infinite(creativeCanister.getCapacity());
                    }
                    if (stack.getItem() instanceof AirCanisterItem airCanister) {
                        return LegacyEnergyTransferBridge.customData(context, "AirStored", airCanister.getCapacity());
                    }
                    return null;
                },
                ModItems.AIR_CANISTER.get(),
                ModItems.REINFORCED_AIR_CANISTER.get(),
                ModItems.CREATIVE_AIR_CANISTER.get()
        );
    }
}
