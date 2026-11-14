package top.ribs.scguns.fabric.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.animated.AnimatedGunItem;

@Mixin(ItemEntity.class)
public abstract class ScorchedItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void scguns$droppedGun(CallbackInfo callback) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getItem();
        if (stack.getItem() instanceof AnimatedGunItem gun) {
            gun.onEntityItemUpdate(stack, self);
        }
    }
}
