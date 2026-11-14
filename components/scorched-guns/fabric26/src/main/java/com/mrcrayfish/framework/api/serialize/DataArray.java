package com.mrcrayfish.framework.api.serialize;
import java.util.ArrayList;
import java.util.List;
public final class DataArray implements DataValue {
    private final List<DataValue> values = new ArrayList<>();
    private final int id = System.identityHashCode(this);
    public void add(DataValue value) { values.add(value); }
    public int length() { return values.size(); }
    public DataValue get(int index) { return values.get(index); }
    public List<DataValue> values() { return values; }
    @Override public DataType getType() { return DataType.ARRAY; }
    @Override public int getId() { return id; }
}
