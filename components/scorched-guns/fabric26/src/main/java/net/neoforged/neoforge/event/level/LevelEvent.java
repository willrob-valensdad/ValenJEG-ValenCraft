package net.neoforged.neoforge.event.level;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
public class LevelEvent extends Event {
    private final Level level;
    public LevelEvent(Level level) { this.level = level; }
    public Level getLevel() { return level; }
    public static class Load extends LevelEvent { public Load(Level level) { super(level); } }
    public static class Pre extends LevelEvent { public Pre(Level level) { super(level); } }
    public static class Post extends LevelEvent { public Post(Level level) { super(level); } }
}
