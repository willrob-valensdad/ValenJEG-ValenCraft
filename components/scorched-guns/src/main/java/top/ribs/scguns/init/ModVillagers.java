package top.ribs.scguns.init;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.ribs.scguns.Reference;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Reference.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Reference.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> GUNSMITH_POI = POI_TYPES.register("gunsmith_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.GUN_BENCH.get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> GUNSMITH =
            VILLAGER_PROFESSIONS.register("gunsmith", () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.scguns.gunsmith"),
                    holder -> holder.value() == GUNSMITH_POI.get(),
                    holder -> holder.value() == GUNSMITH_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_ARMORER,
                    createGunsmithTradeSets()));

    private static Int2ObjectMap<ResourceKey<TradeSet>> createGunsmithTradeSets() {
        Int2ObjectOpenHashMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            tradeSets.put(level, ResourceKey.create(
                    Registries.TRADE_SET,
                    Identifier.fromNamespaceAndPath(Reference.MOD_ID, "gunsmith/level_" + level)));
        }
        return tradeSets;
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
