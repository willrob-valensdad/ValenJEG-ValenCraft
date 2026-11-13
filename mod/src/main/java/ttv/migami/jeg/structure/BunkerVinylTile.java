package ttv.migami.jeg.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Source BlockEnumMulti cube. Meta 0 is TileType.LARGE and meta 1 is TileType.SMALL. */
public final class BunkerVinylTile extends Block {
    public static final IntegerProperty META = IntegerProperty.create("meta", 0, 1);

    public BunkerVinylTile(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(META, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(META);
    }
}
