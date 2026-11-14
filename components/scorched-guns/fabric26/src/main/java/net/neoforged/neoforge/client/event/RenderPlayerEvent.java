package net.neoforged.neoforge.client.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.neoforged.bus.api.Event;
public class RenderPlayerEvent extends Event {
    private final LivingEntityRenderer<?, ?, ?> renderer;
    private final AvatarRenderState renderState;
    private final PoseStack poseStack;
    private final SubmitNodeCollector collector;
    private final float partialTick;
    public RenderPlayerEvent(LivingEntityRenderer<?, ?, ?> renderer, AvatarRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        this.renderer = renderer;
        this.renderState = renderState;
        this.poseStack = poseStack;
        this.collector = collector;
        this.partialTick = partialTick;
    }
    public LivingEntityRenderer<?, ?, ?> getRenderer() { return renderer; }
    public AvatarRenderState getRenderState() { return renderState; }
    public PoseStack getPoseStack() { return poseStack; }
    public SubmitNodeCollector getSubmitNodeCollector() { return collector; }
    public float getPartialTick() { return partialTick; }
    public static class Pre extends RenderPlayerEvent {
        public Pre(LivingEntityRenderer<?, ?, ?> renderer, AvatarRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
            super(renderer, renderState, poseStack, collector, partialTick);
        }
    }
}
