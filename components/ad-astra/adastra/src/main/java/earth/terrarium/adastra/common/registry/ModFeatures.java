package earth.terrarium.adastra.common.registry;

import com.mojang.serialization.MapCodec;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.common.world.features.InfernalSpireColumnFeature;
import earth.terrarium.adastra.common.world.features.MarsBlockBlobFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ModFeatures {

    public static final ResourcefulRegistry<MapCodec<? extends Feature>> FEATURES = ResourcefulRegistries.create(BuiltInRegistries.FEATURE_TYPE, AdAstra.MOD_ID);

    public static final RegistryEntry<MapCodec<InfernalSpireColumnFeature>> INFERNAL_SPIRE_COLUMN = FEATURES.register("infernal_spire_column", () -> InfernalSpireColumnFeature.CODEC);
    public static final RegistryEntry<MapCodec<MarsBlockBlobFeature>> MARS_BLOCK_BLOB_FEATURE = FEATURES.register("mars_block_blob", () -> MarsBlockBlobFeature.CODEC);
}
