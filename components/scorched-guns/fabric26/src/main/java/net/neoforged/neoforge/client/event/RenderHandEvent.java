package net.neoforged.neoforge.client.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
public class RenderHandEvent extends Event {
    private final InteractionHand hand;
    private final ItemStack stack;
    private final float partialTick;
    private final PoseStack poseStack;
    private final SubmitNodeCollector collector;
    private final int packedLight;
    public RenderHandEvent(InteractionHand hand, ItemStack stack, float partialTick) {
        this(hand, stack, partialTick, null, null, 0);
    }
    public RenderHandEvent(InteractionHand hand, ItemStack stack, float partialTick, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        this.hand = hand; this.stack = stack; this.partialTick = partialTick;
        this.poseStack = poseStack; this.collector = collector; this.packedLight = packedLight;
    }
    public InteractionHand getHand() { return hand; }
    public ItemStack getItemStack() { return stack; }
    public float getPartialTick() { return partialTick; }
    public PoseStack getPoseStack() { return poseStack; }
    public SubmitNodeCollector getSubmitNodeCollector() { return collector; }
    public int getPackedLight() { return packedLight; }
    public static class Pre extends RenderHandEvent {
        public Pre(InteractionHand hand, ItemStack stack, float partialTick) { super(hand, stack, partialTick); }
    }
    public static class Post extends RenderHandEvent {
        public Post(InteractionHand hand, ItemStack stack, float partialTick) { super(hand, stack, partialTick); }
    }
}
