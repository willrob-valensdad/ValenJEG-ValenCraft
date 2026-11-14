package net.neoforged.neoforge.client.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.neoforged.bus.api.Event;
public class SubmitCustomGeometryEvent extends Event {
    private final PoseStack poseStack;
    private final SubmitNodeCollector collector;
    public SubmitCustomGeometryEvent(PoseStack poseStack, SubmitNodeCollector collector) {
        this.poseStack = poseStack;
        this.collector = collector;
    }
    public PoseStack getPoseStack() { return poseStack; }
    public SubmitNodeCollector getSubmitNodeCollector() { return collector; }
}
