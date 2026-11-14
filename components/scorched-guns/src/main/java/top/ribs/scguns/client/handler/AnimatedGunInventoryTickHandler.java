package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AnimatedGunItem;

@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class AnimatedGunInventoryTickHandler {
    private AnimatedGunInventoryTickHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                animatedGun.clientInventoryTick26(stack, player, slot, slot == selectedSlot);
            }
        }
    }
}
