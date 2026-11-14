package com.mrcrayfish.framework.api.sync;
public final class Serializers {
    public static final Serializer<Boolean> BOOLEAN = new Serializer<>();
    public static final Serializer<Integer> INTEGER = new Serializer<>();
    private Serializers() {}
    public static final class Serializer<T> {}
}
