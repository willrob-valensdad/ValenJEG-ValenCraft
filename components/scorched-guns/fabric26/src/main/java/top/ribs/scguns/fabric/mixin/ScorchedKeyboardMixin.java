package top.ribs.scguns.fabric.mixin;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardHandler.class)
public class ScorchedKeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void scguns$key(long window, int action, KeyEvent event, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new InputEvent.Key(event.key(), action));
    }
}
