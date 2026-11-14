package top.ribs.scguns.common;


import top.ribs.scguns.util.ScgunsNbtCompat;
import top.ribs.scguns.annotation.Ignored;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import top.ribs.scguns.util.INBTSerializable;

/**
 * Author: MrCrayfish
 */
public class CustomGun implements INBTSerializable<CompoundTag>
{
    @Ignored
    public ItemStack model;
    public Gun gun;

    public ItemStack getModel()
    {
        return this.model;
    }

    public Gun getGun()
    {
        return this.gun;
    }

    public CompoundTag serializeNBT()
    {
        return this.serializeNBT(Gun.builtInRegistryProvider());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries)
    {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", ScgunsNbtCompat.writeItemStack(registries, this.model));
        compound.put("Gun", this.gun.serializeNBT(registries));
        return compound;
    }

    public void deserializeNBT(CompoundTag compound)
    {
        this.deserializeNBT(Gun.builtInRegistryProvider(), compound);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag compound)
    {
        this.model = ScgunsNbtCompat.readItemStack(registries, compound.getCompound("Model").orElseGet(CompoundTag::new));
        this.gun = Gun.create(registries, compound.getCompound("Gun").orElseGet(CompoundTag::new));
    }
}
