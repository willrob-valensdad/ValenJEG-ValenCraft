package top.ribs.scguns.fabric.mixin;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public class ScorchedLivingEntityMixin {
    @Unique private static final ThreadLocal<Boolean> scguns$handlingDamage = ThreadLocal.withInitial(() -> false);
    @Unique private final List<ItemEntity> scguns$drops = new ArrayList<>();
    @Unique private boolean scguns$collectingDrops;
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void scguns$incomingDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        if (scguns$handlingDamage.get()) return;
        LivingEntity self = (LivingEntity) (Object) this;
        LivingIncomingDamageEvent event = new LivingIncomingDamageEvent(self, source, amount);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            callback.setReturnValue(false);
            return;
        }
        if (event.getAmount() != amount) {
            scguns$handlingDamage.set(true);
            try { callback.setReturnValue(self.hurtServer(level, source, event.getAmount())); }
            finally { scguns$handlingDamage.set(false); }
        }
    }
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void scguns$beginDrops(ServerLevel level, DamageSource source, CallbackInfo callback) {
        scguns$drops.clear();
        scguns$collectingDrops = true;
    }
    @Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void scguns$collectDrop(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> callback) {
        if (scguns$collectingDrops && callback.getReturnValue() != null) scguns$drops.add(callback.getReturnValue());
    }
    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void scguns$finishDrops(ServerLevel level, DamageSource source, CallbackInfo callback) {
        scguns$collectingDrops = false;
        LivingDropsEvent event = new LivingDropsEvent((LivingEntity) (Object) this);
        event.getDrops().addAll(scguns$drops);
        NeoForge.EVENT_BUS.post(event);
        scguns$drops.clear();
    }
}
