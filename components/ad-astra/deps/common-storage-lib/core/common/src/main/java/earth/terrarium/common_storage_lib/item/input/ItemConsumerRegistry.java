package earth.terrarium.common_storage_lib.item.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import earth.terrarium.common_storage_lib.item.input.consumers.CompoundConsumer;
import earth.terrarium.common_storage_lib.item.input.consumers.EnergyConsumer;
import earth.terrarium.common_storage_lib.item.input.consumers.FluidConsumer;
import earth.terrarium.common_storage_lib.item.input.consumers.SizedConsumer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemConsumerRegistry {
    public static final Map<Identifier, ConsumerType<?>> CONSUMER_TYPES = new HashMap<>();
    public static final Codec<ConsumerType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(ItemConsumerRegistry::decode, ConsumerType::id);
    public static final StreamCodec<ByteBuf, ConsumerType<?>> STREAM_CODEC = Identifier.STREAM_CODEC.map(CONSUMER_TYPES::get, ConsumerType::id);

    static {
        register(SizedConsumer.TYPE);
        register(CompoundConsumer.TYPE);
        register(EnergyConsumer.TYPE);
        register(FluidConsumer.TYPE);
    }

    public static void register(ConsumerType<?> type) {
        CONSUMER_TYPES.put(type.id(), type);
    }

    public static void init() {
        // Called from Botarium#init
    }

    private static DataResult<? extends ConsumerType<?>> decode(Identifier id) {
        return Optional.ofNullable(CONSUMER_TYPES.get(id)).map(DataResult::success).orElse(DataResult.error(() -> "No ritual component type found."));
    }
}
