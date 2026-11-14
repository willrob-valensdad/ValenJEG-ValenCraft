package ttv.migami.jeg.worldgen.structure;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Box;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Kind;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Piece;
import ttv.migami.jeg.worldgen.structure.BunkerRoomPainter.Cell;

/**
 * One source room. {@code roomMin*} is the source box and stays the local
 * origin. The piece bounding box may also cover the hub hatch. Extending it
 * does not move the room.
 */
public final class BunkerRoomPiece extends StructurePiece {
    public static final StructurePieceType TYPE = (context, tag) -> new BunkerRoomPiece(context, tag);
    private final Kind kind;
    private final int componentType;
    private final int coordMode;
    private final int[] rolls;
    private final boolean[] paths;
    private final long decorationSeed;
    private final boolean surfaceSampled;
    private final int surfaceY;
    private int roomMinX, roomMinY, roomMinZ, roomMaxX, roomMaxY, roomMaxZ;

    public BunkerRoomPiece(Piece piece, long decorationSeed) {
        this(piece, decorationSeed, 0, false);
    }

    public BunkerRoomPiece(Piece piece, long decorationSeed, int surfaceY) {
        this(piece, decorationSeed, surfaceY, true);
    }

    private BunkerRoomPiece(Piece piece, long decorationSeed, int surfaceY, boolean surfaceSampled) {
        super(TYPE, 0, new BoundingBox(piece.box.minX, piece.box.minY, piece.box.minZ, piece.box.maxX, piece.box.maxY, piece.box.maxZ));
        this.kind = piece.kind;
        this.componentType = piece.componentType;
        this.coordMode = piece.coordMode;
        this.rolls = piece.rolls.clone();
        this.paths = piece.paths.clone();
        this.decorationSeed = decorationSeed;
        this.surfaceSampled = surfaceSampled;
        this.surfaceY = surfaceY;
        copyRoom(piece.box.minX, piece.box.minY, piece.box.minZ, piece.box.maxX, piece.box.maxY, piece.box.maxZ);
    }

    public BunkerRoomPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(TYPE, tag);
        this.kind = Kind.valueOf(tag.getStringOr("kind", ""));
        this.componentType = tag.getIntOr("component", -1);
        this.coordMode = tag.getIntOr("mode", -1);
        this.rolls = tag.getIntArray("rolls").orElseThrow();
        int[] flags = tag.getIntArray("paths").orElseThrow();
        this.paths = new boolean[flags.length];
        for (int i = 0; i < flags.length; i++) paths[i] = flags[i] != 0;
        this.decorationSeed = tag.getLongOr("decor", 0L);
        int[] room = tag.getIntArray("room").orElseThrow();
        if (room.length != 6) throw new IllegalStateException("room box");
        copyRoom(room[0], room[1], room[2], room[3], room[4], room[5]);
        this.surfaceSampled = tag.contains("surface");
        this.surfaceY = tag.getIntOr("surface", 0);
        if (coordMode < 0 || coordMode > 3 || componentType < 0) throw new IllegalStateException("room tag");
    }

    /** Grow the start box around the 14 hatch calls. The room origin stays put. */
    public void includeHatch() {
        if (kind != Kind.HUB) return;
        if (!surfaceSampled) throw new IllegalStateException("hub hatch has no surface sample");
        for (Cell cell : BunkerRoomPainter.paint(roomPiece(), decorationSeed, surfaceY)) boundingBox.encapsulate(cell.pos());
    }

    public BoundingBox movedBounds() { return getBoundingBox(); }

    public Box roomBox() { return new Box(roomMinX, roomMinY, roomMinZ, roomMaxX, roomMaxY, roomMaxZ); }

    @Override
    public void move(int dx, int dy, int dz) {
        super.move(dx, dy, dz);
        roomMinX += dx; roomMinY += dy; roomMinZ += dz;
        roomMaxX += dx; roomMaxY += dy; roomMaxZ += dz;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("kind", kind.name());
        tag.putInt("component", componentType);
        tag.putInt("mode", coordMode);
        tag.putIntArray("rolls", rolls);
        int[] flags = new int[paths.length];
        for (int i = 0; i < paths.length; i++) flags[i] = paths[i] ? 1 : 0;
        tag.putIntArray("paths", flags);
        tag.putLong("decor", decorationSeed);
        tag.putIntArray("room", new int[] {roomMinX, roomMinY, roomMinZ, roomMaxX, roomMaxY, roomMaxZ});
        if (surfaceSampled) tag.putInt("surface", surfaceY);
    }

    @Override
    public void postProcess(WorldGenLevel level, net.minecraft.world.level.StructureManager manager, ChunkGenerator generator, net.minecraft.util.RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos anchor) {
        if (!surfaceSampled) throw new IllegalStateException("bunker room has no persisted surface");
        List<Cell> cells = BunkerRoomPainter.paint(roomPiece(), decorationSeed, surfaceY);
        for (Cell cell : cells) {
            if (!chunkBox.isInside(cell.pos())) continue;
            level.setBlock(cell.pos(), cell.state(), 2);
            if (cell.lootTable() == null) continue;
            BlockEntity entity = level.getBlockEntity(cell.pos());
            if (!(entity instanceof RandomizableContainer container)) throw new IllegalStateException(cell.lootTable());
            container.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(cell.lootTable())));
            container.setLootTableSeed(decorationSeed);
        }
    }

    private Piece roomPiece() {
        return new Piece(kind, componentType, coordMode, roomBox(), rolls, paths);
    }

    private void copyRoom(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        roomMinX = minX; roomMinY = minY; roomMinZ = minZ;
        roomMaxX = maxX; roomMaxY = maxY; roomMaxZ = maxZ;
    }
}
