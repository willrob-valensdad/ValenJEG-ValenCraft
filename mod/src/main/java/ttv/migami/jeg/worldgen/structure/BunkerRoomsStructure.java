package ttv.migami.jeg.worldgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * Native bunker-room structure. The weighted 7..12 graph stays in
 * {@link BunkerProceduralAssembly}. Hatch height is the integer mean of the
 * hub's 8×8 columns. WorldGenLevel has no getHeight. Layout and decoration
 * seeds come from the native feature random and are not a 1.7 world-seed or
 * Y match. Placement density is the structure set, not {@code bunkerSpawnWeight}.
 */
public final class BunkerRoomsStructure extends Structure {
    public static final MapCodec<BunkerRoomsStructure> CODEC = simpleCodec(BunkerRoomsStructure::new);
    public static final StructureType<BunkerRoomsStructure> TYPE = () -> CODEC;
    /** Source {@code markAvailableHeight} margin. Native parameter name is {@code offset}. */
    public static final int SOURCE_HEIGHT_MARGIN = 20;

    public BunkerRoomsStructure(StructureSettings settings) { super(settings); }

    @Override public StructureType<?> type() { return TYPE; }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int chunkX = context.chunkPos().x();
        int chunkZ = context.chunkPos().z();
        int surfaceY = hubSurface(context, chunkX, chunkZ);
        long layoutSeed = context.random().nextLong();
        BunkerProceduralAssembly.Graph graph = BunkerProceduralAssembly.assemble(new java.util.Random(layoutSeed), chunkX, chunkZ);
        if (graph.pieces.isEmpty()) throw new IllegalStateException("Bunker assembly returned no pieces");
        long decorationSeed = context.random().nextLong();
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        java.util.ArrayList<BunkerRoomPiece> rooms = new java.util.ArrayList<>();
        for (BunkerProceduralAssembly.Piece piece : graph.pieces) {
            BunkerRoomPiece room = new BunkerRoomPiece(piece, decorationSeed, surfaceY);
            rooms.add(room);
            builder.addPiece(room);
        }
        int seaLevel = context.chunkGenerator().getSeaLevel();
        int minY = context.chunkGenerator().getMinY();
        int dy = builder.moveBelowSeaLevel(seaLevel, minY, context.random(), SOURCE_HEIGHT_MARGIN);
        for (BunkerRoomPiece room : rooms) room.includeHatch();
        BlockPos position = new BlockPos(chunkX * 16 + 8, 64 + dy, chunkZ * 16 + 8);
        return Optional.of(new GenerationStub(position, Either.right(builder)));
    }

    /**
     * Hub box is {@code (chunk*16+8, 64, chunk*16+8)} through {@code +7,+5,+7}
     * for every facing. Sixty-four {@code getBaseHeight(WORLD_SURFACE_WG)}
     * samples, each clamped with {@code max(seaLevel + 1, height)}, then
     * Java integer division. This is not a 1.7 column-height match.
     */
    private static int hubSurface(GenerationContext context, int chunkX, int chunkZ) {
        int originX = chunkX * 16 + 8;
        int originZ = chunkZ * 16 + 8;
        int floor = context.chunkGenerator().getSeaLevel() + 1;
        int total = 0;
        for (int dz = 0; dz < 8; dz++) {
            for (int dx = 0; dx < 8; dx++) {
                int height = context.chunkGenerator().getBaseHeight(originX + dx, originZ + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                total += Math.max(floor, height);
            }
        }
        return total / 64;
    }
}
