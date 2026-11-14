package net.neoforged.neoforge.event;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;
public class AddServerReloadListenersEvent extends Event {
    private final List<Object> listeners = new ArrayList<>();
    public <T> void addListener(Identifier id, T listener) {
        listeners.add(listener);
        if (listener instanceof PreparableReloadListener reload) {
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new Identified(id, reload));
        }
    }
    public List<Object> listeners() { return listeners; }
    private static final class Identified implements IdentifiableResourceReloadListener {
        private final Identifier id;
        private final PreparableReloadListener delegate;
        private Identified(Identifier id, PreparableReloadListener delegate) { this.id = id; this.delegate = delegate; }
        @Override public Identifier getFabricId() { return id; }
        @Override public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.SharedState state, java.util.concurrent.Executor background, PreparableReloadListener.PreparationBarrier barrier, java.util.concurrent.Executor game) {
            return delegate.reload(state, background, barrier, game);
        }
    }
}
