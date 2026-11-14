package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class ModelEvent extends Event {
    public static class Pre extends ModelEvent {}
    public static class Post extends ModelEvent {}
}
