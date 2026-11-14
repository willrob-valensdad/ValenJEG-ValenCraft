package net.neoforged.neoforge.client.event;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.Event;
/** KeyMapping's constructor inserts the binding. Category.register inserts the controls-screen category. */
public class RegisterKeyMappingsEvent extends Event {
    public void register(KeyMapping mapping) {}
    public void registerCategory(KeyMapping.Category category) {
        if (category != null) KeyMapping.Category.register(category.id());
    }
    public static class Pre extends RegisterKeyMappingsEvent {}
    public static class Post extends RegisterKeyMappingsEvent {}
}
