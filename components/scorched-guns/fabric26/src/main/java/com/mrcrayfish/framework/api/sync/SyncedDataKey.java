package com.mrcrayfish.framework.api.sync;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
public final class SyncedDataKey<E, T> {
    private final Identifier id;
    private final Supplier<T> fallback;
    private final boolean resetOnDeath;
    private final Serializers.Serializer<T> serializer;
    private final Map<Entity, T> values = new WeakHashMap<>();
    private SyncedDataKey(Identifier id, Supplier<T> fallback, boolean resetOnDeath, Serializers.Serializer<T> serializer) {
        this.id = id; this.fallback = fallback; this.resetOnDeath = resetOnDeath; this.serializer = serializer;
    }
    public static <E, T> Builder<E, T> builder(SyncedClassKey<E> key, Serializers.Serializer<T> serializer) {
        return new Builder<>(serializer);
    }
    public Identifier id() { return id; }
    public boolean resetOnDeath() { return resetOnDeath; }
    public Serializers.Serializer<T> serializer() { return serializer; }
    public T getValue(E entity) {
        if (!(entity instanceof Entity living)) return fallback.get();
        synchronized (values) { return values.getOrDefault(living, fallback.get()); }
    }
    public void setValue(E entity, T value) {
        store(entity, value);
        if (entity instanceof ServerPlayer player) com.mrcrayfish.framework.entity.sync.SyncedEntityData.broadcast(this, player);
        else if (entity instanceof Player player && player.level().isClientSide()) com.mrcrayfish.framework.entity.sync.SyncedEntityData.requestServer(this, value);
    }
    public void store(E entity, T value) {
        if (entity instanceof Entity living) {
            synchronized (values) { values.put(living, value); }
        }
    }
    public T defaultValue() { return fallback.get(); }
    @SuppressWarnings("unchecked")
    public Object read(Object entity) { return getValue((E) entity); }
    @SuppressWarnings("unchecked")
    public void writeAuthoritative(Object entity, Object value) { store((E) entity, (T) value); }
    public static final class Builder<E, T> {
        private final Serializers.Serializer<T> serializer;
        private Identifier id;
        private Supplier<T> fallback = () -> null;
        private boolean resetOnDeath;
        private Builder(Serializers.Serializer<T> serializer) { this.serializer = serializer; }
        public Builder<E, T> id(Identifier id) { this.id = id; return this; }
        public Builder<E, T> defaultValueSupplier(Supplier<T> supplier) { this.fallback = supplier; return this; }
        public Builder<E, T> resetOnDeath() { this.resetOnDeath = true; return this; }
        public SyncedDataKey<E, T> build() { return new SyncedDataKey<>(id, fallback, resetOnDeath, serializer); }
    }
}
