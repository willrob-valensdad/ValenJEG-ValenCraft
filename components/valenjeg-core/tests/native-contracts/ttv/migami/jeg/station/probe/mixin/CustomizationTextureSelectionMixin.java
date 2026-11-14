package ttv.migami.jeg.station.probe.mixin;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ttv.migami.jeg.client.render.gun.AnimatedGunGeoModel.class)
public abstract class CustomizationTextureSelectionMixin {
 @Inject(method="getTextureResource",at=@At("RETURN"))
 private void valen$recordActualTexture(GeoRenderState state,CallbackInfoReturnable<Identifier> result){ttv.migami.jeg.station.probe.CustomizationClientProbe.texture(state,result.getReturnValue());}
}
