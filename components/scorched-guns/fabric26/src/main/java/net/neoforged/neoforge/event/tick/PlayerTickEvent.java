package net.neoforged.neoforge.event.tick;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
public class PlayerTickEvent extends Event {
    private final Player player;
    public PlayerTickEvent() { this(null); }
    public PlayerTickEvent(Player player) { this.player = player; }
    public Player getEntity() { return player; }
    public static class Pre extends PlayerTickEvent {
        public Pre() {}
        public Pre(Player player) { super(player); }
    }
    public static class Post extends PlayerTickEvent {
        public Post() {}
        public Post(Player player) { super(player); }
    }
}
