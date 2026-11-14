package top.ribs.scguns.fabric.mixin;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public class ScorchedCameraMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void scguns$fov(CallbackInfoReturnable<Float> cir) {
        ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(cir.getReturnValueF(), 1.0F);
        NeoForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getFOV());
    }
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void scguns$angles(CameraRenderState state, DeltaTracker tracker, CallbackInfo ci) {
        float partial = tracker.getGameTimeDeltaPartialTick(true);
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(partial, state.yRot, state.xRot, 0.0F);
        NeoForge.EVENT_BUS.post(event);
        state.yRot = event.getYaw();
        state.xRot = event.getPitch();
        if (event.getRoll() != 0.0F && state.orientation != null) {
            state.orientation.rotateZ((float) Math.toRadians(event.getRoll()));
        }
    }
}
