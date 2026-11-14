package com.mrcrayfish.framework.api.serialize;
public final class DataString implements DataValue {
    private final String value;
    private final int id = System.identityHashCode(this);
    public DataString(String value) { this.value = value; }
    public String asString() { return value; }
    @Override public DataType getType() { return DataType.STRING; }
    @Override public int getId() { return id; }
}
