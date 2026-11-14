package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class CalculatePlayerTurnEvent extends Event {
    public static class Pre extends CalculatePlayerTurnEvent {}
    public static class Post extends CalculatePlayerTurnEvent {}
}
