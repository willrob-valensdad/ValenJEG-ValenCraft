package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.exosuit.RebreatherModuleItem;

import net.minecraft.world.entity.EquipmentSlot;
@EventBusSubscriber(modid = Reference.MOD_ID)
public class ExoSuitRebreatherHandler {

    private static final int REFRESH_INTERVAL = 160;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        if (player.tickCount % 20 != 0) {
            return;
        }

        handleRebreatherEnergy(player);
    }

    private static void handleRebreatherEnergy(Player player) {
        boolean inWater = player.isInWater() || player.isUnderWater();
        if (!inWater) {
            return;
        }

        if (!hasRebreatherModule(player)) {
            return;
        }

        if (!ExoSuitPowerManager.canConsumeEnergy(player, "breathing", REFRESH_INTERVAL)) {
            return;
        }

        if (!ExoSuitPowerManager.canUpgradeFunction(player, "breathing")) {
            return;
        }

        ItemStack helmetUpgrade = findRebreatherModule(player);
        if (!helmetUpgrade.isEmpty() &&
                helmetUpgrade.getItem() instanceof RebreatherModuleItem rebreatherModule) {
            if (!rebreatherModule.canFunctionWithoutPower()) {
                ExoSuitPowerManager.consumeEnergyForUpgrade(player, "breathing", helmetUpgrade);
            }
        }
    }

    private static boolean hasRebreatherModule(Player player) {
        return !findRebreatherModule(player).isEmpty();
    }

    private static ItemStack findRebreatherModule(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.HELMET) {

                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("breathing") &&
                                upgradeItem.getItem() instanceof RebreatherModuleItem) {
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
