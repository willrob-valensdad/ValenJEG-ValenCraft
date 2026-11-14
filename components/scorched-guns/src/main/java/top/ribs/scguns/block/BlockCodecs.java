package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 26.3 Block has no codec. The registry constructs blocks; this field only typechecks the old static. */
public final class BlockCodecs {
    private BlockCodecs() {}

    public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> factory) {
        return MapCodec.unit(() -> factory.apply(BlockBehaviour.Properties.of()));
    }
}
