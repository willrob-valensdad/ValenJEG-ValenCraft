/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.network.event;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

public class RegisterPayloadHandlersEvent {
    public Registrar registrar(String namespace) {
        return new Registrar();
    }

    public static class Registrar {
        public <T extends CustomPacketPayload> Registrar playToServer(
                CustomPacketPayload.Type<T> type,
                StreamCodec<RegistryFriendlyByteBuf, T> codec,
                BiConsumer<T, IPayloadContext> handler
        ) {
            return this;
        }

        public <T extends CustomPacketPayload> Registrar playToClient(
                CustomPacketPayload.Type<T> type,
                StreamCodec<RegistryFriendlyByteBuf, T> codec,
                BiConsumer<T, IPayloadContext> handler
        ) {
            return this;
        }
    }
}
