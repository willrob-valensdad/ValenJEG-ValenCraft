package ttv.migami.jeg.worldgen.structure;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Resolves blocks from the NTMNext port ({@code hbm} namespace).
 * A missing id or property fails. There is no vanilla stand-in.
 */
public final class HbmRoomStates {
    private HbmRoomStates() {}

    public static BlockState get(String path, String... properties) {
        Identifier id = Identifier.fromNamespaceAndPath("hbm", path);
        Optional<Block> found = BuiltInRegistries.BLOCK.getOptional(id);
        if (found.isEmpty()) throw new IllegalStateException("unregistered hbm block " + id);
        BlockState state = found.get().defaultBlockState();
        for (String entry : properties) {
            int split = entry.indexOf('=');
            if (split <= 0) throw new IllegalStateException("bad property " + entry + " on " + id);
            String name = entry.substring(0, split);
            String value = entry.substring(split + 1);
            Property<?> property = null;
            for (Property<?> candidate : state.getProperties()) {
                if (candidate.getName().equals(name)) {
                    property = candidate;
                    break;
                }
            }
            if (property == null) throw new IllegalStateException("missing property " + name + " on " + id);
            state = assign(state, property, value, id.toString());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState assign(BlockState state, Property<T> property, String value, String id) {
        Optional<T> parsed = property.getValue(value);
        if (parsed.isEmpty()) throw new IllegalStateException("bad value " + property.getName() + "=" + value + " on " + id);
        return state.setValue(property, parsed.get());
    }
}
