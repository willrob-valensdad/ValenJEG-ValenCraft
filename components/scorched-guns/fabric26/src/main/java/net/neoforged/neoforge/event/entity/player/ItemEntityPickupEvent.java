package net.neoforged.neoforge.event.entity.player;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
public class ItemEntityPickupEvent extends Event {
    private final Player player;
    private final ItemEntity itemEntity;
    private final ItemStack originalStack;
    private TriState canPickup = TriState.DEFAULT;
    public ItemEntityPickupEvent(Player player, ItemEntity itemEntity) {
        this.player = player;
        this.itemEntity = itemEntity;
        this.originalStack = itemEntity == null ? ItemStack.EMPTY : itemEntity.getItem().copy();
    }
    public Player getPlayer() { return player; }
    public ItemEntity getItemEntity() { return itemEntity; }
    public ItemStack getOriginalStack() { return originalStack; }
    public void setCanPickup(TriState canPickup) { this.canPickup = canPickup; }
    public TriState getCanPickup() { return canPickup; }
    public static class Pre extends ItemEntityPickupEvent {
        public Pre(Player player, ItemEntity itemEntity) { super(player, itemEntity); }
    }
    public static class Post extends ItemEntityPickupEvent {
        public Post(Player player, ItemEntity itemEntity) { super(player, itemEntity); }
    }
}
