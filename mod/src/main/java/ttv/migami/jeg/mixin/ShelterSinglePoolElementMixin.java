package ttv.migami.jeg.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import ttv.migami.jeg.worldgen.structure.ShelterPaintingAnchor;

@Mixin(SinglePoolElement.class)
public abstract class ShelterSinglePoolElementMixin {
    @WrapMethod(method = "place")
    private boolean jeg$scopeShelterPaintings(
        StructureTemplateManager structureTemplateManager,
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        BlockPos position,
        BlockPos referencePos,
        Rotation rotation,
        BoundingBox chunkBB,
        RandomSource random,
        LiquidSettings liquidSettings,
        boolean keepJigsaws,
        Operation<Boolean> original
    ) {
        Identifier template;
        try {
            template = ((SinglePoolElement) (Object) this).getTemplateLocation();
        } catch (RuntimeException notAnIdentifier) {
            template = null;
        }
        String previous = ShelterPaintingAnchor.begin(template);
        try {
            return original.call(
                structureTemplateManager,
                level,
                structureManager,
                generator,
                position,
                referencePos,
                rotation,
                chunkBB,
                random,
                liquidSettings,
                keepJigsaws
            );
        } finally {
            ShelterPaintingAnchor.end(previous);
        }
    }
}
