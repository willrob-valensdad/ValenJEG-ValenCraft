package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.*;

import java.sql.Ref;


public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Reference.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MechanicalPressRecipe>> MECHANICAL_PRESS_TYPE =
            TYPES.register("mechanical_pressing", () -> MechanicalPressRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PoweredMechanicalPressRecipe>> POWERED_MECHANICAL_PRESS_TYPE =
            TYPES.register("powered_mechanical_pressing", () -> PoweredMechanicalPressRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MaceratorRecipe>> MACERATOR_TYPE =
            TYPES.register("macerating", () -> MaceratorRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PoweredMaceratorRecipe>> POWERED_MACERATOR_TYPE =
            TYPES.register("powered_macerating", () -> PoweredMaceratorRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, RecipeType<GunBenchRecipe>> GUN_BENCH_TYPE =
            TYPES.register("gun_bench", () -> GunBenchRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, RecipeType<LightningBatteryRecipe>> LIGHTNING_BATTERY_TYPE =
            TYPES.register("lightning_battery", () -> LightningBatteryRecipe.Type.INSTANCE);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Reference.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MechanicalPressRecipe>> MECHANICAL_PRESS_SERIALIZER =
            SERIALIZERS.register("mechanical_pressing", () -> MechanicalPressRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PoweredMechanicalPressRecipe>> POWERED_MECHANICAL_PRESS_SERIALIZER =
            SERIALIZERS.register("powered_mechanical_pressing", () -> PoweredMechanicalPressRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MaceratorRecipe>> MACERATOR_SERIALIZER =
            SERIALIZERS.register("macerating", () -> MaceratorRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PoweredMaceratorRecipe>> POWERED_MACERATOR_SERIALIZER =
            SERIALIZERS.register("powered_macerating", () -> PoweredMaceratorRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GunBenchRecipe>> GUN_BENCH_SERIALIZER =
            SERIALIZERS.register("gun_bench", () -> GunBenchRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LightningBatteryRecipe>> LIGHTNING_BATTERY_SERIALIZER =
            SERIALIZERS.register("lightning_battery", () -> LightningBatteryRecipe.Serializer.INSTANCE);
    public static void register(IEventBus eventBus) {
        // Minecraft 26.2 recipe synchronization serializes RecipeType through the
        // built-in recipe_type registry. The original 1.20.1 singleton-only types
        // worked for server-side lookup, but cannot be sent through recipe_content
        // until the exact same singleton instances are registered here.
        TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }
}








