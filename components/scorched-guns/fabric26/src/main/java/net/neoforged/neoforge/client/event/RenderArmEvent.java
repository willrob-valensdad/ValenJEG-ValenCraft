package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class RenderArmEvent<T> extends Event {
    private final T player;
    public RenderArmEvent(T player) { this.player = player; }
    public T getPlayer() { return player; }
}
