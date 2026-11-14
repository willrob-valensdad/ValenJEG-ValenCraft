package top.ribs.scguns.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Small compatibility bridge for Scorched Guns' legacy CompoundTag payloads.
 * Minecraft 26.2 serializes ItemStack through codecs instead of save/parseOptional.
 */
public final class ScgunsNbtCompat {
    private ScgunsNbtCompat() {}

    public static ItemStack readItemStack(HolderLookup.Provider registries, Tag tag) {
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.OPTIONAL_CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .result()
                .orElse(ItemStack.EMPTY);
    }

    public static CompoundTag writeItemStack(HolderLookup.Provider registries, ItemStack stack) {
        Tag encoded = ItemStack.OPTIONAL_CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result()
                .orElseGet(CompoundTag::new);
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }
}
