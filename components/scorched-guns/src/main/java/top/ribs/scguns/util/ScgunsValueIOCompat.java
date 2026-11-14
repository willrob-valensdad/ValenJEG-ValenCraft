package top.ribs.scguns.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Bridge between 26.2 ValueIO serializers and Scorched Guns custom-data
 * CompoundTags. This is only needed where the data is embedded in an item
 * component rather than being written directly through a ValueOutput.
 */
public final class ScgunsValueIOCompat {
    private ScgunsValueIOCompat() {}

    public static CompoundTag write(ValueIOSerializable serializable, HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        serializable.serialize(output);
        return output.buildResult();
    }

    public static void read(ValueIOSerializable serializable, HolderLookup.Provider registries, CompoundTag tag) {
        serializable.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }
}
