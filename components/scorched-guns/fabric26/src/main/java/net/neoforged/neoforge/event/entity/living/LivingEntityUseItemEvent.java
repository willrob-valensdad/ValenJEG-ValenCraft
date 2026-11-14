package net.neoforged.neoforge.event.entity.living;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
public class LivingEntityUseItemEvent extends Event {
    private final LivingEntity entity;
    private final ItemStack item;
    private ItemStack result;
    public LivingEntityUseItemEvent(LivingEntity entity, ItemStack item) {
        this.entity = entity;
        this.item = item;
        this.result = item;
    }
    public LivingEntity getEntity() { return entity; }
    public ItemStack getItem() { return item; }
    public ItemStack getResultStack() { return result; }
    public void setResultStack(ItemStack result) { this.result = result; }
    public static class Start extends LivingEntityUseItemEvent {
        public Start(LivingEntity entity, ItemStack item) { super(entity, item); }
    }
    public static class Tick extends LivingEntityUseItemEvent {
        public Tick(LivingEntity entity, ItemStack item) { super(entity, item); }
    }
    public static class Stop extends LivingEntityUseItemEvent {
        public Stop(LivingEntity entity, ItemStack item) { super(entity, item); }
    }
    public static class Finish extends LivingEntityUseItemEvent {
        public Finish(LivingEntity entity, ItemStack item, ItemStack result) {
            super(entity, item);
            setResultStack(result);
        }
    }
}
