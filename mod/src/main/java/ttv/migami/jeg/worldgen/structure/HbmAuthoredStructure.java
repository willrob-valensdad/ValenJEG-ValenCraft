package ttv.migami.jeg.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

/**
 * Authored HBM prefabs use vanilla JigsawPlacement and its native pieces.
 * Only donor height averaging/clamping is adapted. No replacement piece,
 * template renderer, room builder, terrain filter or placement-search system.
 *
 * Parity limit: 1.7 HBM averaged live heights in the first processing chunk.
 * Native modern center-surface projection replaces that late 256-column average;
 * donor offsets/clamps remain. Source 2D biome filtering uses the surface point
 * before piece allocation, not the buried/waterline template base. The native
 * outer biome predicate remains intact. Cross-version terrain equality is not
 * claimed. Native waterline, terrain, access and rotation proof remain pending.
 */
public final class HbmAuthoredStructure extends Structure {
    public static final MapCodec<HbmAuthoredStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            StringRepresentable.fromEnum(Profile::values).fieldOf("source_profile").forGetter(s -> s.profile)
    ).apply(instance, HbmAuthoredStructure::new));
    public static final StructureType<HbmAuthoredStructure> TYPE = () -> CODEC;

    // Exact registrations in pinned NTMWorldGenerator.java: 81-87, 96-102,
    // 261-265, 276-280. SpawnCondition default bounds are 1..128.
    public enum Profile implements StringRepresentable {
        SHELTER("falloutshelter_house", -8, 1, 128),
        PATROL("beached_patrol", -5, 58, 67),
        RIG("oil_rig", -20, 11, 12),
        SUBMARINE("submarine", -10, 1, 128),
        MINIPURGE_BUNKER("minipurge_bunker", -24, -63, Integer.MAX_VALUE);
        private final String name;
        final int offset, min, max;
        Profile(String name, int offset, int min, int max) {
            this.name = name; this.offset = offset; this.min = min; this.max = max;
        }
        @Override public String getSerializedName() { return name; }
    }

    private final Holder<StructureTemplatePool> startPool;
    private final Profile profile;

    public HbmAuthoredStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Profile profile) {
        super(settings);
        this.startPool = startPool;
        this.profile = profile;
    }

    @Override public StructureType<?> type() { return TYPE; }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (profile == Profile.MINIPURGE_BUNKER) return findMinipurgeGenerationPoint(context);
        BlockPos anchor = context.chunkPos().getMiddleBlockPosition(0);
        int surfaceHeight = context.chunkGenerator().getFirstFreeHeight(anchor.getX(), anchor.getZ(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        BlockPos surfaceAnchor = anchor.atY(surfaceHeight - 1);
        // HBM GenStructure.getSpawnAtCoords filters its 2D center biome before
        // placement. Reuse GenerationContext.isValidBiome's native resolver rule.
        var surfaceBiome = context.biomeResolver().getNoiseBiome(
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getX()),
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getY()),
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getZ()));
        if (!context.validBiome().test(surfaceBiome)) return Optional.empty();
        // Native 26.3 publishes the root only when maxDepth >= 1. These
        // authored templates have no connectors; accept exactly one piece.
        var generated = JigsawPlacement.addPieces(context, startPool, Optional.empty(), 1,
                anchor, false, Optional.empty(), new JigsawStructure.MaxDistance(128),
                PoolAliasLookup.EMPTY, DimensionPadding.ZERO, LiquidSettings.IGNORE_WATERLOGGING);
        if (generated.isEmpty()) return Optional.empty();
        var pieces = generated.get().getPiecesBuilder();
        if (pieces.isEmpty() || pieces.build().pieces().size() != 1) return Optional.empty();
        int baseY = HbmSourceHeight.baseY(surfaceHeight, profile.offset, profile.min, profile.max,
                context.chunkGenerator() instanceof FlatLevelSource);
        // Native jigsaw ground-level delta is accounted from realized bounds,
        // avoiding an assumed +/-1 adjustment or a second random piece draw.
        pieces.offsetPiecesVertically(baseY - pieces.getBoundingBox().minY());
        return Optional.of(new GenerationStub(surfaceAnchor, builder -> {
            for (var piece : pieces.build().pieces()) builder.addPiece(piece);
        }));
    }
    /**
     * Minipurge:Black authored shelter uses the same native single-root machinery.
     * Its original6x6spiral is at template origin, so sample that realized origin,
     * not the jigsaw center; retain source24..25below-surface/min-63rule.
     * No ARR helper/event code is copied. Native rotations remain, mirror NONE
     * and native base-terrain height versus source live tree/mushroom scan are
     * explicit parity limits. Actual entrance/access still requires native QA.
     */
    private Optional<GenerationStub> findMinipurgeGenerationPoint(GenerationContext context) {
        var generated = JigsawPlacement.addPieces(context, startPool, Optional.empty(), 1,
                context.chunkPos().getMiddleBlockPosition(0), false, Optional.empty(),
                new JigsawStructure.MaxDistance(128), PoolAliasLookup.EMPTY,
                DimensionPadding.ZERO, LiquidSettings.IGNORE_WATERLOGGING);
        if (generated.isEmpty()) return Optional.empty();
        var pieces = generated.get().getPiecesBuilder();
        var realized = pieces.build().pieces();
        if (realized.size() != 1 ||
                !(realized.getFirst() instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece root))
            return Optional.empty();
        // Pool must be the reviewed exact authored template, not a different root.
        if (!(root.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement single) ||
                !single.getTemplateLocation().toString().equals("jeg:valen_minipurge/underground_shelter"))
            return Optional.empty();
        BlockPos origin = root.getPosition();
        int surfaceAirY = context.chunkGenerator().getFirstFreeHeight(origin.getX(), origin.getZ(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        BlockPos surfaceAnchor = new BlockPos(origin.getX(), surfaceAirY - 1, origin.getZ());
        var biome = context.biomeResolver().getNoiseBiome(
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getX()),
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getY()),
                net.minecraft.core.QuartPos.fromBlock(surfaceAnchor.getZ()));
        if (!context.validBiome().test(biome)) return Optional.empty();
        int baseY = MinipurgeSourceHeight.baseY(surfaceAirY, 24 + context.random().nextInt(2));
        pieces.offsetPiecesVertically(baseY - origin.getY());
        return Optional.of(new GenerationStub(surfaceAnchor, builder -> {
            for (var piece : pieces.build().pieces()) builder.addPiece(piece);
        }));
    }

}
