package top.ribs.scguns.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.List;

public class VillageStructures {
	public static void addNewVillageBuilding(final ServerAboutToStartEvent event) {
		HolderLookup.RegistryLookup<StructureTemplatePool> templatePools = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
		HolderLookup.RegistryLookup<StructureProcessorList> processorLists = event.getServer().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
		VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/plains/houses"), Reference.MOD_ID + ":village/houses/plains_gunsmith_house", 8);
      VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/snowy/houses"), Reference.MOD_ID + ":village/houses/snowy_gunsmith_house", 8);
      VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/savanna/houses"), Reference.MOD_ID + ":village/houses/savanna_gunsmith_house", 8);
		VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/desert/houses"), Reference.MOD_ID + ":village/houses/desert_gunsmith_house", 8);
      VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/taiga/houses"), Reference.MOD_ID + ":village/houses/taiga_gunsmith_house", 8);
	}

	public static void addBuildingToPool(HolderLookup.RegistryLookup<StructureTemplatePool> templatePoolRegistry, HolderLookup.RegistryLookup<StructureProcessorList> processorListRegistry, Identifier poolRL, String nbtPieceRL, int weight) {
		ResourceKey<StructureTemplatePool> poolKey = ResourceKey.create(Registries.TEMPLATE_POOL, poolRL);
		StructureTemplatePool pool = templatePoolRegistry.getOrThrow(poolKey).value();
		Identifier emptyProcessor = Identifier.withDefaultNamespace("empty");
		Holder<StructureProcessorList> processorHolder = processorListRegistry.getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, emptyProcessor));

		SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, processorHolder).apply(StructureTemplatePool.Projection.RIGID);

		for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
		}

		List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
		listOfPieceEntries.add(new Pair<>(piece, weight));
		pool.rawTemplates = listOfPieceEntries;
	}
}
