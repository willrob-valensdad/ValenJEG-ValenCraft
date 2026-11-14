package top.ribs.scguns.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.ammo_boxes.DishesPouch;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Reference.MOD_ID)
public class DishesEventHandler {

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        ItemStack resultStack = event.getResultStack();
        if (isDishItem(resultStack)) {
            if (addDishToPouch(player, resultStack)) {
                event.setResultStack(ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack pickedItem = event.getItemEntity().getItem();

        if (isDishItem(pickedItem)) {
            if (addDishToPouch(player, pickedItem)) {
                event.setCanPickup(TriState.FALSE);
                event.getItemEntity().setItem(ItemStack.EMPTY);
            }
        }
    }

    private static boolean isDishItem(ItemStack stack) {
        if (stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE)) {
            return true;
        }
        Item item = stack.getItem();
        ItemStackTemplate remainder = item instanceof top.ribs.scguns.item.StackCraftingRemainder custom
                ? custom.getCraftingRemainder(stack)
                : item.getCraftingRemainder();
        return remainder != null && (remainder.is(Items.BOWL) || remainder.is(Items.GLASS_BOTTLE));
    }

    private static boolean addDishToPouch(Player player, ItemStack dishStack) {
        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            if (itemStack.getItem() instanceof DishesPouch) {
                int insertedItems = DishesPouch.add(itemStack, dishStack);
                if (insertedItems > 0) {
                    dishStack.shrink(insertedItems);
                    return true;
                }
            }
        }

        AtomicBoolean result = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof DishesPouch) {
                    int insertedItems = DishesPouch.add(stack, dishStack);
                    if (insertedItems > 0) {
                        dishStack.shrink(insertedItems);
                        result.set(true);
                        break;
                    }
                }
            }
        });

        return result.get();
    }
}
