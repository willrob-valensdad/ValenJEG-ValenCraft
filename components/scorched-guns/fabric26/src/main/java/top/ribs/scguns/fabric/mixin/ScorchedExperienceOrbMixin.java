package top.ribs.scguns.fabric.mixin;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.fabric.ScorchedFabric;
@Mixin(ExperienceOrb.class)
public class ScorchedExperienceOrbMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void scguns$pickup(Player player, CallbackInfo callback) {
        ScorchedFabric.MOD_BUS.post(new PlayerXpEvent.PickupXp(player, (ExperienceOrb) (Object) this));
    }
}
