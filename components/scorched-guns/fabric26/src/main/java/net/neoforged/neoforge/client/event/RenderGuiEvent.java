package net.neoforged.neoforge.client.event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.bus.api.Event;
/** HUD render carrying the 26.3 extractor and tick tracker. */
public class RenderGuiEvent extends Event {
    private final GuiGraphicsExtractor graphics;
    private final DeltaTracker partialTick;
    public RenderGuiEvent(GuiGraphicsExtractor graphics, DeltaTracker partialTick) {
        this.graphics = graphics;
        this.partialTick = partialTick;
    }
    public GuiGraphicsExtractor getGuiGraphics() { return graphics; }
    public DeltaTracker getPartialTick() { return partialTick; }
    public static class Pre extends RenderGuiEvent {
        public Pre(GuiGraphicsExtractor graphics, DeltaTracker partialTick) { super(graphics, partialTick); }
    }
    public static class Post extends RenderGuiEvent {
        public Post(GuiGraphicsExtractor graphics, DeltaTracker partialTick) { super(graphics, partialTick); }
    }
}
