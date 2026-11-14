package net.neoforged.neoforge.event;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
public class RegisterCauldronInteractionEvent extends Event {
    public static final Map<Identifier, CauldronInteraction.Dispatcher> DISPATCHERS = new HashMap<>();
    public static class Dispatcher extends RegisterCauldronInteractionEvent {
        public void register(Identifier id, CauldronInteraction.Dispatcher dispatcher) {
            DISPATCHERS.put(id, dispatcher);
        }
    }
    public static class Interaction extends RegisterCauldronInteractionEvent {
        public void register(Identifier id, Item item, CauldronInteraction interaction) {
            CauldronInteraction.Dispatcher dispatcher = DISPATCHERS.get(id);
            if (dispatcher == null && Identifier.withDefaultNamespace("empty").equals(id)) {
                dispatcher = net.minecraft.core.cauldron.CauldronInteractions.EMPTY;
            }
            if (dispatcher != null) dispatcher.put(item, interaction);
        }
    }
}
