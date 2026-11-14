package net.neoforged.neoforge.client.model.standalone;
import java.util.function.Supplier;
/** Identity of a JSON model baked outside the block-state set. */
public final class StandaloneModelKey<T> {
    private final Supplier<String> id;
    public StandaloneModelKey(Supplier<String> id) { this.id = id; }
    public String id() { return id.get(); }
}
