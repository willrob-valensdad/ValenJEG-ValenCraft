package top.ribs.scguns.fabric.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PersistentEntitySectionManager.class)
public class ScorchedEntitySectionMixin {
    @Inject(method = "addEntity", at = @At("HEAD"))
    private void scguns$load(EntityAccess entity, boolean loaded, CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Entity loadedEntity) EntityJoinLevelEvent.LOADED_FROM_DISK.put(loadedEntity, loaded);
    }
    @Inject(method = "addEntity", at = @At("RETURN"))
    private void scguns$clear(EntityAccess entity, boolean loaded, CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Entity loadedEntity) EntityJoinLevelEvent.LOADED_FROM_DISK.remove(loadedEntity);
    }
}
