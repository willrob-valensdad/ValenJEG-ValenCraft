package top.ribs.scguns.fabric.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class ScorchedHandsMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void scguns$hands(float partialTick, PoseStack poseStack, SubmitNodeCollector collector, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState hands, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean holdingGun = false;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof GunItem) holdingGun = true;
            RenderHandEvent event = new RenderHandEvent(hand, stack, partialTick, poseStack, collector, 0x00F000F0);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) holdingGun = true;
        }
        if (holdingGun) ci.cancel();
    }
}
