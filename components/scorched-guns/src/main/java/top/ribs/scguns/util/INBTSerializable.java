package top.ribs.scguns.util;

import net.minecraft.core.HolderLookup;

/**
 * Local compatibility contract replacing NeoForge's removed INBTSerializable helper.
 * Scorched Guns already used registry-aware serialization methods with this exact shape.
 */
public interface INBTSerializable<T> {
    T serializeNBT(HolderLookup.Provider registries);

    void deserializeNBT(HolderLookup.Provider registries, T nbt);
}
