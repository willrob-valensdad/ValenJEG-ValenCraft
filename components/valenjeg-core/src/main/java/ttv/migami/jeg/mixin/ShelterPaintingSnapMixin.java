package ttv.migami.jeg.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import org.spongepowered.asm.mixin.Mixin;
import ttv.migami.jeg.worldgen.structure.ShelterPaintingAnchor;

/** Preserve the authored attachment only inside this shelter's native entity placement. */
@Mixin(Painting.class)
public abstract class ShelterPaintingSnapMixin {
    @WrapMethod(method = "snapTo(DDDFF)V")
    private void jeg$preserveAuthoredAnchor(double x, double y, double z, float yRot, float xRot, Operation<Void> original) {
        BlockPos authored = ShelterPaintingAnchor.active()
            ? ((BlockAttachedEntity) (Object) this).getPos() : null;
        original.call(x, y, z, yRot, xRot);
        if (authored != null) {
            BlockAttachedEntityAccessor access = (BlockAttachedEntityAccessor) (Object) this;
            access.jeg$setAttachment(authored);
            access.jeg$recalculateBoundingBox();
        }
    }
}
