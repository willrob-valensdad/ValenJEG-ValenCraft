package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class RecipesReceivedEvent extends Event {
    public static class Pre extends RecipesReceivedEvent {}
    public static class Post extends RecipesReceivedEvent {}
}
