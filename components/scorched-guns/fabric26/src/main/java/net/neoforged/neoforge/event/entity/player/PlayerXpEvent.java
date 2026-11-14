package net.neoforged.neoforge.event.entity.player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
public class PlayerXpEvent extends Event {
    public static class Pre extends PlayerXpEvent {}
    public static class Post extends PlayerXpEvent {}
    public static class PickupXp extends PlayerXpEvent {
        private final Player player;
        private final ExperienceOrb orb;
        public PickupXp(Player player, ExperienceOrb orb) {
            this.player = player;
            this.orb = orb;
        }
        public Player getEntity() { return player; }
        public ExperienceOrb getOrb() { return orb; }
    }
}
