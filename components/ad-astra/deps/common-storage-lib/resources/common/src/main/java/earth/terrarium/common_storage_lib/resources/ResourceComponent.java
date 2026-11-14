package earth.terrarium.common_storage_lib.resources;

import net.minecraft.core.component.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ResourceComponent implements Resource, DataComponentHolder {
    protected final DataComponentMap components;
    protected final DataComponentPatch dataPatch;

    protected ResourceComponent(DataComponentPatch patch) {
        this.components = patch == DataComponentPatch.EMPTY ? DataComponentMap.EMPTY : PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
        this.dataPatch = patch;
    }

    public DataComponentPatch getDataPatch() {
        return dataPatch;
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return components;
    }

    public boolean componentsMatch(DataComponentPatch other) {
        return Objects.equals(getDataPatch(), other);
    }

    public static DataComponentPatch mergeChanges(DataComponentPatch base, DataComponentPatch applied) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        writeChangesTo(base, builder);
        writeChangesTo(applied, builder);
        return builder.build();
    }

    public static <T> DataComponentPatch addChanges(DataComponentPatch base, DataComponentType<T> type, T value) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        writeChangesTo(base, builder);
        builder.set(type, value);
        return builder.build();
    }

    private static void writeChangesTo(DataComponentPatch changes, DataComponentPatch.Builder builder) {
        DataComponentPatch.SplitResult split = changes.split();
        for (TypedDataComponent<?> added : split.added()) {
            builder.set(added);
        }
        for (DataComponentType<?> removed : split.removed()) {
            builder.remove(removed);
        }
    }
}
