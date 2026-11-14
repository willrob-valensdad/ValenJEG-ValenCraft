package net.neoforged.neoforge.event.entity.living;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
public class MobEffectEvent extends Event {
    private final LivingEntity entity;
    protected MobEffectEvent(LivingEntity entity) { this.entity = entity; }
    public LivingEntity getEntity() { return entity; }
    public static class Applicable extends MobEffectEvent {
        private final MobEffectInstance effect;
        private Result result = Result.DEFAULT;
        public Applicable(LivingEntity entity, MobEffectInstance effect) {
            super(entity);
            this.effect = effect;
        }
        public MobEffectInstance getEffectInstance() { return effect; }
        public void setResult(Result result) { this.result = result; }
        public Result getResult() { return result; }
        public enum Result { DEFAULT, DO_NOT_APPLY, APPLY }
    }
    public static class Remove extends MobEffectEvent {
        private final Holder<MobEffect> effect;
        public Remove(LivingEntity entity, Holder<MobEffect> effect) {
            super(entity);
            this.effect = effect;
        }
        public Holder<MobEffect> getEffect() { return effect; }
    }
}
