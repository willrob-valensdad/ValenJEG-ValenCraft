package earth.terrarium.common_storage_lib.item.input;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ConsumerType<T extends ItemConsumer>(Identifier id, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
    public ConsumerType(Identifier location, MapCodec<T> codec) {
        this(location, codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
