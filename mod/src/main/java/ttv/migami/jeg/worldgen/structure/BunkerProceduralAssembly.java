package ttv.migami.jeg.worldgen.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Graph half of BunkerStart. This is the pinned ProceduralStructureStart
 * weight loop and the six room exits. It is not a jigsaw template and it is
 * not registered. Room block fills and StructureStart.markAvailableHeight
 * stay outside this class.
 */
public final class BunkerProceduralAssembly {
    private BunkerProceduralAssembly() {}

    public enum Kind {
        HUB, CORRIDOR, BEDROOM_L, FUN_JUNCTION, BATHROOM_L, LABORATORY, POWER_ROOM
    }

    public static final class Box {
        public final int minX, minY, minZ, maxX, maxY, maxZ;
        public Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }
        public boolean intersects(Box other) {
            return maxX >= other.minX && minX <= other.maxX && maxZ >= other.minZ && minZ <= other.maxZ && maxY >= other.minY && minY <= other.maxY;
        }
    }

    public static final class Piece {
        public final Kind kind;
        public final int componentType;
        public final int coordMode;
        public final Box box;
        public final int[] rolls;
        public final boolean[] paths;
        public Piece(Kind kind, int componentType, int coordMode, Box box, int[] rolls) {
            this(kind, componentType, coordMode, box, rolls, new boolean[pathCount(kind)]);
        }
        public Piece(Kind kind, int componentType, int coordMode, Box box, int[] rolls, boolean[] paths) {
            this.kind = kind; this.componentType = componentType; this.coordMode = coordMode; this.box = box;
            this.rolls = rolls.clone();
            this.paths = paths.clone();
            if (this.paths.length != pathCount(kind)) throw new IllegalArgumentException(kind.name());
        }
        static int pathCount(Kind kind) {
            return switch (kind) {
                case HUB -> 3;
                case FUN_JUNCTION, LABORATORY -> 2;
                default -> 1;
            };
        }
    }

    public static final class Graph {
        public final int sizeLimit;
        public final List<Piece> pieces;
        public Graph(int sizeLimit, List<Piece> pieces) { this.sizeLimit = sizeLimit; this.pieces = pieces; }
    }

    public static Graph assemble(Random rand, int chunkX, int chunkZ) {
        int sizeLimit = 7 + rand.nextInt(6);
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        Start start = new Start(chunkX, chunkZ, sizeLimit);
        int mode = rand.nextInt(4);
        Box hub = mode == 1 || mode == 3
                ? new Box(x, 64, z, x + 7, 69, z + 7)
                : new Box(x, 64, z, x + 7, 69, z + 7);
        Piece starter = new Piece(Kind.HUB, 0, mode, hub, new int[0]);
        start.build(rand, starter, weights());
        return new Graph(sizeLimit, start.pieces);
    }

    private static Weight[] weights() {
        return new Weight[] {
            new Weight(Kind.CORRIDOR, 6, 3),
            new Weight(Kind.BEDROOM_L, 5, 4),
            new Weight(Kind.FUN_JUNCTION, 10, 3),
            new Weight(Kind.BATHROOM_L, 5, 2),
            new Weight(Kind.LABORATORY, 7, 2),
            new Weight(Kind.POWER_ROOM, 5, 1)
        };
    }

    static Box boxFor(int posX, int posY, int posZ, int offsetX, int offsetY, int offsetZ, int maxX, int maxY, int maxZ, int coordMode) {
        return switch (coordMode) {
            case 1 -> new Box(posX - maxZ + 1 - offsetZ, posY + offsetY, posZ + offsetX, posX - offsetZ, posY + maxY - 1 + offsetY, posZ + maxX - 1 + offsetX);
            case 2 -> new Box(posX - maxX + 1 - offsetX, posY + offsetY, posZ - maxZ + 1 - offsetZ, posX - offsetX, posY + maxY - 1 + offsetY, posZ - offsetZ);
            case 3 -> new Box(posX + offsetZ, posY + offsetY, posZ - maxX + 1 - offsetX, posX + maxZ - 1 + offsetZ, posY + maxY - 1 + offsetY, posZ - offsetX);
            default -> new Box(posX + offsetX, posY + offsetY, posZ + offsetZ, posX + maxX - 1 + offsetX, posY + maxY - 1 + offsetY, posZ + maxZ - 1 + offsetZ);
        };
    }

    private static final class Start {
        final int chunkX, chunkZ, sizeLimit;
        final List<Piece> pieces = new ArrayList<>();
        final List<Piece> queued = new ArrayList<>();
        List<Weight> weights;

        Start(int chunkX, int chunkZ, int sizeLimit) {
            this.chunkX = chunkX; this.chunkZ = chunkZ; this.sizeLimit = sizeLimit;
        }

        void build(Random rand, Piece starter, Weight[] source) {
            weights = new ArrayList<>();
            for (Weight weight : source) {
                weight.spawned = 0;
                weights.add(weight);
            }
            pieces.add(starter);
            queued.add(starter);
            while (!queued.isEmpty()) {
                Piece piece = queued.remove(rand.nextInt(queued.size()));
                buildPiece(piece, rand);
            }
        }

        void buildPiece(Piece piece, Random rand) {
            switch (piece.kind) {
                case HUB -> {
                    piece.paths[0] = link(piece, rand, Exit.EAST, 5, 1);
                    piece.paths[1] = link(piece, rand, Exit.ANTI, 4, 1);
                    piece.paths[2] = link(piece, rand, Exit.WEST, 3, 1);
                }
                case CORRIDOR -> piece.paths[0] = link(piece, rand, Exit.NORMAL, 3, 1);
                case BEDROOM_L -> piece.paths[0] = link(piece, rand, Exit.WEST, 9, 1);
                case FUN_JUNCTION -> {
                    piece.paths[0] = link(piece, rand, Exit.EAST, 6, 1);
                    piece.paths[1] = link(piece, rand, Exit.NORMAL, 5, 1);
                }
                case BATHROOM_L -> piece.paths[0] = link(piece, rand, Exit.EAST, 3, 1);
                case LABORATORY -> {
                    piece.paths[0] = link(piece, rand, Exit.WEST, 3, 1);
                    piece.paths[1] = link(piece, rand, Exit.NORMAL, 6, 1);
                }
                case POWER_ROOM -> piece.paths[0] = link(piece, rand, Exit.EAST, 4, 1);
            }
        }

        boolean link(Piece caller, Random rand, Exit exit, int offset, int offsetY) {
            int mode = caller.coordMode;
            Box box = caller.box;
            return switch (exit) {
                case NORMAL -> next(caller, rand, switch (mode) {
                    case 1 -> new Anchor(box.minX - 1, box.minY + offsetY, box.minZ + offset, mode);
                    case 2 -> new Anchor(box.maxX - offset, box.minY + offsetY, box.minZ - 1, mode);
                    case 3 -> new Anchor(box.maxX + 1, box.minY + offsetY, box.maxZ - offset, mode);
                    default -> new Anchor(box.minX + offset, box.minY + offsetY, box.maxZ + 1, mode);
                });
                case ANTI -> next(caller, rand, switch (mode) {
                    case 0 -> new Anchor(box.maxX - offset, box.minY + offsetY, box.minZ - 1, 2);
                    case 1 -> new Anchor(box.maxX + 1, box.minY + offsetY, box.maxZ - offset, 3);
                    case 2 -> new Anchor(box.minX + offset, box.minY + offsetY, box.maxZ + 1, 0);
                    case 3 -> new Anchor(box.minX - 1, box.minY + offsetY, box.minZ + offset, 1);
                    default -> null;
                });
                case WEST -> next(caller, rand, switch (mode) {
                    case 0 -> new Anchor(box.minX - 1, box.minY + offsetY, box.minZ + offset, 1);
                    case 1 -> new Anchor(box.maxX - offset, box.minY + offsetY, box.minZ - 1, 2);
                    case 2 -> new Anchor(box.maxX + 1, box.minY + offsetY, box.maxZ - offset, 3);
                    case 3 -> new Anchor(box.minX + offset, box.minY + offsetY, box.maxZ + 1, 0);
                    default -> null;
                });
                case EAST -> next(caller, rand, switch (mode) {
                    case 0 -> new Anchor(box.maxX + 1, box.minY + offsetY, box.maxZ - offset, 3);
                    case 1 -> new Anchor(box.minX + offset, box.minY + offsetY, box.maxZ + 1, 0);
                    case 2 -> new Anchor(box.minX - 1, box.minY + offsetY, box.minZ + offset, 1);
                    case 3 -> new Anchor(box.maxX - offset, box.minY + offsetY, box.minZ - 1, 2);
                    default -> null;
                });
            };
        }

        boolean next(Piece caller, Random rand, Anchor anchor) {
            if (anchor == null || pieces.size() > sizeLimit) return false;
            int originX = chunkX * 16 + 8;
            int originZ = chunkZ * 16 + 8;
            if (Math.abs(anchor.x - originX) > 40 || Math.abs(anchor.z - originZ) > 40) return false;
            Piece placed = weighted(caller, rand, anchor);
            if (placed == null) return false;
            pieces.add(placed);
            queued.add(placed);
            return true;
        }

        Piece weighted(Piece last, Random rand, Anchor anchor) {
            for (int attempt = 0; attempt < 5; attempt++) {
                int total = totalWeight();
                if (total < 0) return null;
                int value = rand.nextInt(total);
                Iterator<Weight> iterator = weights.iterator();
                while (iterator.hasNext()) {
                    Weight weight = iterator.next();
                    value -= weight.weight;
                    if (value < 0) {
                        if (weight.limit >= 0 && weight.spawned >= weight.limit) break;
                        Piece piece = place(weight.kind, rand, anchor, last.componentType + 1);
                        if (piece != null) {
                            weight.spawned++;
                            if (!(weight.limit < 0 || weight.spawned < weight.limit)) weights.remove(weight);
                            return piece;
                        }
                    }
                }
            }
            return null;
        }

        int totalWeight() {
            boolean open = false;
            int total = 0;
            for (Weight weight : weights) {
                total += weight.weight;
                if (weight.limit >= 0 && weight.spawned < weight.limit) open = true;
            }
            return open ? total : -1;
        }

        Piece place(Kind kind, Random rand, Anchor anchor, int type) {
            int[] spec = spec(kind);
            Box box = boxFor(anchor.x, anchor.y, anchor.z, spec[0], spec[1], spec[2], spec[3], spec[4], spec[5], anchor.mode);
            if (box.minY <= 10 || intersects(box)) return null;
            int[] rolls = new int[0];
            if (kind == Kind.CORRIDOR) rolls = new int[] {rand.nextInt(6), rand.nextInt(6)};
            if (kind == Kind.POWER_ROOM) {
                float chance = rand.nextFloat();
                rolls = new int[] {chance < 0.2F ? 2 : chance < 0.6F ? 1 : 0};
            }
            return new Piece(kind, type, anchor.mode, box, rolls);
        }

        boolean intersects(Box box) {
            for (Piece piece : pieces) if (piece.box.intersects(box)) return true;
            return false;
        }
    }

    private static int[] spec(Kind kind) {
        return switch (kind) {
            case CORRIDOR -> new int[] {-3, -1, 0, 6, 6, 7};
            case BEDROOM_L -> new int[] {-8, -1, 0, 10, 6, 11};
            case FUN_JUNCTION -> new int[] {-5, -1, 0, 8, 6, 12};
            case BATHROOM_L -> new int[] {-3, -1, 0, 9, 6, 11};
            case LABORATORY -> new int[] {-6, -1, 0, 9, 6, 12};
            case POWER_ROOM -> new int[] {-4, -1, 0, 12, 6, 12};
            default -> throw new IllegalStateException(kind.name());
        };
    }

    private enum Exit { NORMAL, ANTI, WEST, EAST }

    private record Anchor(int x, int y, int z, int mode) {}

    private static final class Weight {
        final Kind kind;
        final int weight;
        final int limit;
        int spawned;
        Weight(Kind kind, int weight, int limit) { this.kind = kind; this.weight = weight; this.limit = limit; }
    }
}
