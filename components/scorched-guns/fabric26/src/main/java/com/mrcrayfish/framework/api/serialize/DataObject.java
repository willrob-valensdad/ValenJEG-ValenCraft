package com.mrcrayfish.framework.api.serialize;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
public final class DataObject implements DataValue {
    public static final DataObject EMPTY = new DataObject();
    private final Map<String, DataValue> values = new LinkedHashMap<>();
    private final int id = System.identityHashCode(this);
    public static DataObject parse(Reader reader) {
        return fromJson(JsonParser.parseReader(reader));
    }
    public static DataObject fromJson(JsonElement element) {
        if (element == null || !element.isJsonObject()) return EMPTY;
        DataObject object = new DataObject();
        element.getAsJsonObject().entrySet().forEach(entry -> object.values.put(entry.getKey(), value(entry.getValue())));
        return object;
    }
    public static DataObject fromCompound(CompoundTag tag) {
        DataObject object = new DataObject();
        for (String key : tag.keySet()) object.values.put(key, value(tag.get(key)));
        return object;
    }
    private static DataValue value(JsonElement element) {
        if (element == null || element.isJsonNull()) return new DataString("");
        if (element.isJsonObject()) return fromJson(element);
        if (element.isJsonArray()) {
            DataArray array = new DataArray();
            element.getAsJsonArray().forEach(child -> array.add(value(child)));
            return array;
        }
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) return new DataNumber(element.getAsDouble());
            if (element.getAsJsonPrimitive().isBoolean()) return new DataString(Boolean.toString(element.getAsBoolean()));
            return new DataString(element.getAsString());
        }
        return new DataString("");
    }
    private static DataValue value(Tag tag) {
        if (tag instanceof CompoundTag compound) return fromCompound(compound);
        if (tag instanceof ListTag list) {
            DataArray array = new DataArray();
            for (Tag child : list) array.add(value(child));
            return array;
        }
        if (tag instanceof NumericTag number) return new DataNumber(number.doubleValue());
        if (tag instanceof StringTag string) return new DataString(string.value());
        return new DataString(String.valueOf(tag));
    }
    public boolean isEmpty() { return values.isEmpty(); }
    public boolean has(String key, DataType type) {
        DataValue value = values.get(key);
        return value != null && value.getType() == type;
    }
    public DataValue get(String key) { return values.get(key); }
    public DataObject getDataObject(String key) {
        DataValue value = values.get(key);
        return value instanceof DataObject object ? object : EMPTY;
    }
    public DataArray getDataArray(String key) {
        DataValue value = values.get(key);
        return value instanceof DataArray array ? array : new DataArray();
    }
    public DataNumber getDataNumber(String key) {
        DataValue value = values.get(key);
        return value instanceof DataNumber number ? number : new DataNumber(0);
    }
    public DataString getDataString(String key) {
        DataValue value = values.get(key);
        return value instanceof DataString string ? string : new DataString("");
    }
    @Override public DataType getType() { return DataType.OBJECT; }
    @Override public int getId() { return id; }
}
