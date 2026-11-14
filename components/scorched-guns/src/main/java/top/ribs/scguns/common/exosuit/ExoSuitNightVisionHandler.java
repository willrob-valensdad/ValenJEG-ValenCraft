package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.exosuit.NightVisionModuleItem;

import net.minecraft.world.entity.EquipmentSlot;
@EventBusSubscriber(modid = Reference.MOD_ID)
public class ExoSuitNightVisionHandler {

    private static final int REFRESH_INTERVAL = 150;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        if (player.tickCount % 10 != 0) {
            return;
        }

        handleNightVisionEnergy(player);
    }

    private static void handleNightVisionEnergy(Player player) {
        if (!ExoSuitPowerManager.isPowerEnabled(player, "hud")) {
            return;
        }

        if (!hasNightVisionModule(player)) {
            return;
        }

        if (!ExoSuitPowerManager.canConsumeEnergy(player, "hud", REFRESH_INTERVAL)) {
            return;
        }

        if (!ExoSuitPowerManager.canUpgradeFunction(player, "hud")) {
            return;
        }

        ItemStack helmetUpgrade = findNightVisionModule(player);
        if (!helmetUpgrade.isEmpty() &&
                helmetUpgrade.getItem() instanceof NightVisionModuleItem nightVisionModule) {
            if (!nightVisionModule.canFunctionWithoutPower()) {
                ExoSuitPowerManager.consumeEnergyForUpgrade(player, "hud", helmetUpgrade);
            }
        }
    }

    private static boolean hasNightVisionModule(Player player) {
        return !findNightVisionModule(player).isEmpty();
    }

    private static ItemStack findNightVisionModule(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.HELMET) {
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("hud") &&
                                upgradeItem.getItem() instanceof NightVisionModuleItem) {
                            return upgradeItem;
                        }
                    }
                }
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void onPlayerLogout(Player player) {
    }
}
