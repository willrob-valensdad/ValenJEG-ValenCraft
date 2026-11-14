package net.neoforged.neoforge.client.event;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.bus.api.Event;
public class RenderLivingEvent extends Event {
    private final LivingEntityRenderer<?, ?, ?> renderer;
    private final EntityRenderState renderState;
    public RenderLivingEvent(LivingEntityRenderer<?, ?, ?> renderer, EntityRenderState renderState) {
        this.renderer = renderer;
        this.renderState = renderState;
    }
    public LivingEntityRenderer<?, ?, ?> getRenderer() { return renderer; }
    public EntityRenderState getRenderState() { return renderState; }
    public static class Pre extends RenderLivingEvent {
        public Pre(LivingEntityRenderer<?, ?, ?> renderer, EntityRenderState renderState) { super(renderer, renderState); }
    }
    public static class Post extends RenderLivingEvent {
        public Post(LivingEntityRenderer<?, ?, ?> renderer, EntityRenderState renderState) { super(renderer, renderState); }
    }
}
