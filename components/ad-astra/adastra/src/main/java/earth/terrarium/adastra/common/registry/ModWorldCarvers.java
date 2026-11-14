package earth.terrarium.adastra.common.registry;

import com.mojang.serialization.MapCodec;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.common.world.carver.CraterWorldCarver;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class ModWorldCarvers {

    public static final ResourcefulRegistry<MapCodec<? extends WorldCarver>> WORLD_CARVERS = ResourcefulRegistries.create(BuiltInRegistries.CARVER_TYPE, AdAstra.MOD_ID);

    public static final RegistryEntry<MapCodec<CraterWorldCarver>> CRATER = WORLD_CARVERS.register("crater", () -> CraterWorldCarver.CODEC);
}
