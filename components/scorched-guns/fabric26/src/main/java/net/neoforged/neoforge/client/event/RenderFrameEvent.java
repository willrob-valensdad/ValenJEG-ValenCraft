package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class RenderFrameEvent extends Event {
    public static class Pre extends RenderFrameEvent {}
    public static class Post extends RenderFrameEvent {}
}
