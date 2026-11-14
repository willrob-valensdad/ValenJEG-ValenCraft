package top.ribs.scguns.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.ribs.scguns.Reference;
import top.ribs.scguns.world.GeothermalVentFeature;
import top.ribs.scguns.world.SulfurVentFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature> FEATURES = DeferredRegister.create(Registries.FEATURE, Reference.MOD_ID);
    public static final DeferredHolder<Feature, Feature> GEOTHERMAL_VENT_FEATURE = FEATURES.register("geothermal_vent", GeothermalVentFeature::new);
    public static final DeferredHolder<Feature, Feature> SULFUR_VENT_FEATURE = FEATURES.register("sulfur_vent", SulfurVentFeature::new);

    public static void register(IEventBus bus) {
        Registry.register(BuiltInRegistries.FEATURE_TYPE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "geothermal_vent"), GeothermalVentFeature.CODEC);
        Registry.register(BuiltInRegistries.FEATURE_TYPE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "sulfur_vent"), SulfurVentFeature.CODEC);
        FEATURES.register(bus);
    }
}
