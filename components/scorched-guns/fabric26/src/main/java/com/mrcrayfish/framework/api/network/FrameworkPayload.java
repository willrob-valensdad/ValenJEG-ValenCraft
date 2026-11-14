package com.mrcrayfish.framework.api.network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class FrameworkPayload implements CustomPacketPayload {
    private final Type<FrameworkPayload> type;
    final Object value;
    public FrameworkPayload(Type<FrameworkPayload> type, Object value) { this.type = type; this.value = value; }
    @Override public Type<? extends CustomPacketPayload> type() { return type; }
}
