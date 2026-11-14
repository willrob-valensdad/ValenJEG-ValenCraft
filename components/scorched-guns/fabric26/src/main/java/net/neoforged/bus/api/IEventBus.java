package net.neoforged.bus.api;
import java.util.function.Consumer;
public interface IEventBus {
    void register(Object listener);
    void register(Class<?> listenerClass);
    <T> void addListener(Consumer<T> consumer);
    <T> T post(T event);
}
