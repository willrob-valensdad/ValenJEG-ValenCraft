package net.neoforged.bus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
/** Delivers listeners that were registered through the NeoForge-shaped bus. */
public final class DispatchingEventBus implements IEventBus {
    private final List<Consumer<Object>> listeners = new ArrayList<>();
    private final List<Object> subscribers = new ArrayList<>();
    @Override public void register(Object listener) { if (listener != null) subscribers.add(listener); }
    @Override public void register(Class<?> listenerClass) {
        try { subscribers.add(listenerClass.getDeclaredConstructor().newInstance()); }
        catch (ReflectiveOperationException ignored) { subscribers.add(listenerClass); }
    }
    @Override public <T> void addListener(Consumer<T> consumer) {
        listeners.add(event -> {
            try { consumer.accept((T) event); }
            catch (ClassCastException ignored) {}
        });
    }
    @Override public <T> T post(T event) {
        for (Consumer<Object> listener : List.copyOf(listeners)) listener.accept(event);
        for (Object subscriber : List.copyOf(subscribers)) deliver(subscriber, event);
        return event;
    }
    private static void deliver(Object subscriber, Object event) {
        Class<?> type = subscriber instanceof Class<?> clazz ? clazz : subscriber.getClass();
        Object target = subscriber instanceof Class<?> ? null : subscriber;
        for (Method method : type.getDeclaredMethods()) {
            if (method.getAnnotation(SubscribeEvent.class) == null || method.getParameterCount() != 1) continue;
            if (!method.getParameterTypes()[0].isInstance(event)) continue;
            if (target == null && !Modifier.isStatic(method.getModifiers())) continue;
            method.setAccessible(true);
            try { method.invoke(target, event); }
            catch (ReflectiveOperationException failure) { throw new IllegalStateException(method.toString(), failure); }
        }
    }
}
