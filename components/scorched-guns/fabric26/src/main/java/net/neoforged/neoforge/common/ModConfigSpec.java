package net.neoforged.neoforge.common;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;
/** In-memory NeoForge config. Authored defaults are live until a file loader is attached. */
public final class ModConfigSpec {
    public void save() {}
    public static final class Builder {
        public Builder comment(String... comment) { return this; }
        public Builder push(String path) { return this; }
        public Builder pop() { return this; }
        public ModConfigSpec build() { return new ModConfigSpec(); }
        public BooleanValue define(String path, boolean value) { return new BooleanValue(value); }
        public ConfigValue<String> define(String path, String value) { return new ConfigValue<>(value); }
        public <T> ConfigValue<T> define(String path, T value) { return new ConfigValue<>(value); }
        public IntValue defineInRange(String path, int value, int min, int max) { return new IntValue(value); }
        public DoubleValue defineInRange(String path, double value, double min, double max) { return new DoubleValue(value); }
        public <T extends Enum<T>> EnumValue<T> defineEnum(String path, T value) { return new EnumValue<>(value); }
        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> value, Predicate<Object> elementValidator) {
            return new ConfigValue<>(value);
        }
        public <T> Pair<T, ModConfigSpec> configure(Function<Builder, T> factory) {
            return Pair.of(factory.apply(this), new ModConfigSpec());
        }
    }
    public static class ConfigValue<T> {
        private T value;
        public ConfigValue(T value) { this.value = value; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
    }
    public static final class BooleanValue extends ConfigValue<Boolean> { public BooleanValue(boolean value) { super(value); } }
    public static final class IntValue extends ConfigValue<Integer> { public IntValue(int value) { super(value); } }
    public static final class DoubleValue extends ConfigValue<Double> { public DoubleValue(double value) { super(value); } }
    public static final class EnumValue<T extends Enum<T>> extends ConfigValue<T> { public EnumValue(T value) { super(value); } }
}
