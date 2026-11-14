/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.registries;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T cached;
    private ResourceKey<? extends Registry<R>> registry;
    private Identifier identifier;

    public DeferredHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public void bindKey(ResourceKey<? extends Registry<R>> registry, Identifier identifier) {
        this.registry = registry;
        this.identifier = identifier;
    }

    @SuppressWarnings("unchecked")
    public Holder<R> asHolder() {
        if (registry != null && identifier != null) {
            var root = BuiltInRegistries.REGISTRY.get(registry.identifier());
            if (root.isPresent()) {
                Registry<R> resolved = (Registry<R>) root.get().value();
                Optional<Holder.Reference<R>> found = resolved.get(identifier);
                if (found.isPresent()) return found.get();
            }
        }
        return (Holder<R>) (Holder<?>) Holder.direct(get());
    }

    public ResourceKey<R> getKey() {
        return ResourceKey.create(registry, identifier);
    }

    @Override
    public T get() {
        if (cached == null) {
            cached = supplier.get();
        }
        return cached;
    }
}
