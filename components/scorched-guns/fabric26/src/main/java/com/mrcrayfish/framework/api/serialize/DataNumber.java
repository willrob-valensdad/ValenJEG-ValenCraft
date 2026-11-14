package com.mrcrayfish.framework.api.serialize;
public final class DataNumber implements DataValue {
    private final double value;
    private final int id = System.identityHashCode(this);
    public DataNumber(double value) { this.value = value; }
    public int asInt() { return (int) value; }
    public double asDouble() { return value; }
    public float asFloat() { return (float) value; }
    @Override public DataType getType() { return DataType.NUMBER; }
    @Override public int getId() { return id; }
}
