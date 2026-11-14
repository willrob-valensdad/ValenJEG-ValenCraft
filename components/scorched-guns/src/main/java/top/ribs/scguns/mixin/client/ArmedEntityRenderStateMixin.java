package top.ribs.scguns.mixin.client;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.RenderStateEntityAccessor;
import top.ribs.scguns.client.compat.RenderStateEntityBridge;

/**
 * Captures the live entity while Minecraft extracts an ArmedEntityRenderState.
 * This restores the entity context that the old 1.20.1 ItemInHandLayer render
 * callback used to provide directly.
 *
 * <p>The entity is stored on the transient render-state instance itself. This
 * avoids the unbounded static render-state map used by the early 26.2 port.</p>
 */
@Mixin(ArmedEntityRenderState.class)
public abstract class ArmedEntityRenderStateMixin implements RenderStateEntityAccessor {
    @Unique
    private LivingEntity scguns$entity;

    @Override
    public void scguns$setEntity(LivingEntity entity) {
        this.scguns$entity = entity;
    }

    @Override
    public LivingEntity scguns$getEntity() {
        return this.scguns$entity;
    }

    @Inject(method = "extractArmedEntityRenderState", at = @At("TAIL"))
    private static void scguns$captureEntity(LivingEntity entity,
                                             ArmedEntityRenderState renderState,
                                             ItemModelResolver itemModelResolver,
                                             float partialTick,
                                             CallbackInfo ci) {
        RenderStateEntityBridge.capture(renderState, entity);
    }
}
