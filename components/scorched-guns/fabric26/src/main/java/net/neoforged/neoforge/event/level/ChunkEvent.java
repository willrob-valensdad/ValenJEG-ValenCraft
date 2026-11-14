package net.neoforged.neoforge.event.level;
import net.neoforged.bus.api.Event;
public class ChunkEvent extends Event {
    public static class Pre extends ChunkEvent {}
    public static class Post extends ChunkEvent {}
}
