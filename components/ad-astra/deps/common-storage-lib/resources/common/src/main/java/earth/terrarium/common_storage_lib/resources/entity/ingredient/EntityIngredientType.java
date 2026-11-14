package earth.terrarium.common_storage_lib.resources.entity.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record EntityIngredientType<T extends EntityIngredient>(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    public EntityIngredientType(Identifier id, MapCodec<T> codec) {
        this(id, codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
