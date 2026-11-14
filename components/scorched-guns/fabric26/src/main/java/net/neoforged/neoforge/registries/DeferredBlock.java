package net.neoforged.neoforge.registries;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    public DeferredBlock(Supplier<T> supplier) { super(supplier); }
}
