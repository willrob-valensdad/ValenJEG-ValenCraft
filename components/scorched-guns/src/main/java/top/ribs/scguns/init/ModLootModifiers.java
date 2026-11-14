package top.ribs.scguns.init;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.ribs.scguns.Reference;
import top.ribs.scguns.world.loot.AddItemModifier;
import top.ribs.scguns.world.loot.AddLootTableModifier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> ADD_ITEM =
            LOOT_MODIFIERS.register("add_item", codec(AddItemModifier.CODEC));
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> ADD_LOOT_TABLE =
            LOOT_MODIFIERS.register("add_loot_table", codec(AddLootTableModifier.CODEC));

    @SuppressWarnings("unchecked")
    private static <M extends IGlobalLootModifier> java.util.function.Supplier<MapCodec<? extends IGlobalLootModifier>> codec(java.util.function.Supplier<MapCodec<M>> source) {
        return () -> (MapCodec<? extends IGlobalLootModifier>) (MapCodec<?>) source.get();
    }
}

