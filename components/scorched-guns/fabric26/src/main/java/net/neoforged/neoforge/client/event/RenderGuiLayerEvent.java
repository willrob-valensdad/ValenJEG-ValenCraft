package net.neoforged.neoforge.client.event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
/** One vanilla or mod HUD layer. Name matches Fabric's VanillaHudElements identifiers. */
public class RenderGuiLayerEvent extends Event {
    private final Identifier name;
    private final GuiGraphicsExtractor graphics;
    private final DeltaTracker partialTick;
    public RenderGuiLayerEvent(Identifier name, GuiGraphicsExtractor graphics, DeltaTracker partialTick) {
        this.name = name;
        this.graphics = graphics;
        this.partialTick = partialTick;
    }
    public Identifier getName() { return name; }
    public GuiGraphicsExtractor getGuiGraphics() { return graphics; }
    public DeltaTracker getPartialTick() { return partialTick; }
    public static class Pre extends RenderGuiLayerEvent {
        public Pre(Identifier name, GuiGraphicsExtractor graphics, DeltaTracker partialTick) { super(name, graphics, partialTick); }
    }
    public static class Post extends RenderGuiLayerEvent {
        public Post(Identifier name, GuiGraphicsExtractor graphics, DeltaTracker partialTick) { super(name, graphics, partialTick); }
    }
}
