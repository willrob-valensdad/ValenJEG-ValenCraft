package net.neoforged.neoforge.fluids;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
/** Vanilla flowing-fluid behavior parameterized by the Scorched fluid properties. */
public abstract class BaseFlowingFluid extends FlowingFluid {
    private final Properties properties;
    protected BaseFlowingFluid(Properties properties) { this.properties = properties; }
    @Override public Fluid getFlowing() { return properties.flowing.get(); }
    @Override public Fluid getSource() { return properties.still.get(); }
    @Override public Item getBucket() { return properties.bucket == null ? Items.AIR : properties.bucket.get(); }
    @Override protected boolean canConvertToSource(ServerLevel level) { return false; }
    @Override protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        Block.dropResources(state, level, pos, level.getBlockEntity(pos));
    }
    @Override protected int getSlopeFindDistance(LevelReader level) { return properties.slopeFindDistance; }
    @Override protected int getDropOff(LevelReader level) { return properties.levelDecreasePerBlock; }
    @Override public int getTickDelay(LevelReader level) { return properties.tickRate; }
    @Override protected float getExplosionResistance() { return properties.explosionResistance; }
    @Override public boolean isSame(Fluid fluid) { return fluid == getSource() || fluid == getFlowing(); }
    @Override public BlockState createLegacyBlock(FluidState state) {
        return properties.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(FALLING);
    }
    @Override protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isSame(this);
    }
    public static final class Properties {
        private final Supplier<? extends Fluid> still;
        private final Supplier<? extends Fluid> flowing;
        private Supplier<? extends Item> bucket;
        private Supplier<? extends Block> block;
        private int slopeFindDistance = 4;
        private int levelDecreasePerBlock = 1;
        private int tickRate = 5;
        private float explosionResistance = 100.0F;
        public Properties(Supplier<?> type, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
            this.still = still;
            this.flowing = flowing;
        }
        public Properties bucket(Supplier<? extends Item> bucket) { this.bucket = bucket; return this; }
        public Properties block(Supplier<? extends Block> block) { this.block = block; return this; }
        public Properties slopeFindDistance(int distance) { this.slopeFindDistance = distance; return this; }
        public Properties levelDecreasePerBlock(int decrease) { this.levelDecreasePerBlock = decrease; return this; }
        public Properties tickRate(int rate) { this.tickRate = rate; return this; }
        public Properties explosionResistance(float resistance) { this.explosionResistance = resistance; return this; }
    }
}
