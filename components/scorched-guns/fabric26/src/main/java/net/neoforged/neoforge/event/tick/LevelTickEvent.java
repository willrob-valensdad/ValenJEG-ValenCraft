package net.neoforged.neoforge.event.tick;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
public class LevelTickEvent extends Event {
    private final Level level;
    public LevelTickEvent(Level level) { this.level = level; }
    public Level getLevel() { return level; }
    public static class Pre extends LevelTickEvent { public Pre(Level level) { super(level); } }
    public static class Post extends LevelTickEvent { public Post(Level level) { super(level); } }
}
