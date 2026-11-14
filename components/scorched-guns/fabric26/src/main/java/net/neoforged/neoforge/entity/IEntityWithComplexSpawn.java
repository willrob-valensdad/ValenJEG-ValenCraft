package net.neoforged.neoforge.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/** Extra bytes that used to ride the add-entity packet. SpawnExtras delivers them. */
public interface IEntityWithComplexSpawn {
    void writeSpawnData(RegistryFriendlyByteBuf buffer);
    void readSpawnData(RegistryFriendlyByteBuf buffer);
}
