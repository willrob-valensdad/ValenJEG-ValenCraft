package ttv.migami.jeg.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Native transform contract for the shelter painting anchor. No world and no entity spawn. */
public final class ShelterPaintingAnchorContract {
    private static final BlockPos PIECE = new BlockPos(377096, 55, 593992);

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        int cases = scope() + transforms();
        System.out.println("SHELTER_PAINTING_ANCHOR_CONTRACT_PASS " + cases + " scope/4rotation cases; no world claim");
    }

    private static int scope() {
        check(!ShelterPaintingAnchor.active(), "Scope starts clear");
        String outer = ShelterPaintingAnchor.begin(ShelterPaintingAnchor.TEMPLATE);
        check(outer == null && ShelterPaintingAnchor.active(), "Shelter place opens the scope");
        String foreign = ShelterPaintingAnchor.begin(Identifier.parse("minecraft:village/plains/houses"));
        check(ShelterPaintingAnchor.TEMPLATE_ID.equals(foreign) && !ShelterPaintingAnchor.active(), "Nested foreign place suspends the scope");
        ShelterPaintingAnchor.end(foreign);
        check(ShelterPaintingAnchor.active(), "Nested foreign place restores the shelter scope");
        String again = ShelterPaintingAnchor.begin(ShelterPaintingAnchor.TEMPLATE);
        check(ShelterPaintingAnchor.active(), "Nested shelter place keeps the scope");
        ShelterPaintingAnchor.end(again);
        String missing = ShelterPaintingAnchor.begin(null);
        check(!ShelterPaintingAnchor.active(), "Nested element without an id suspends the scope");
        ShelterPaintingAnchor.end(missing);
        ShelterPaintingAnchor.end(outer);
        check(!ShelterPaintingAnchor.active(), "Scope closes");
        String exceptionScope = ShelterPaintingAnchor.begin(ShelterPaintingAnchor.TEMPLATE);
        try {
            try {
                throw new IllegalStateException("contract placement failure");
            } finally {
                ShelterPaintingAnchor.end(exceptionScope);
            }
        } catch (IllegalStateException expected) {
            check(!ShelterPaintingAnchor.active(), "Placement exception clears the scope");
        }
        String targetScope = ShelterPaintingAnchor.begin(ShelterPaintingAnchor.TEMPLATE);
        try {
            String nested = ShelterPaintingAnchor.begin(null);
            try {
                throw new IllegalArgumentException("contract nested failure");
            } finally {
                ShelterPaintingAnchor.end(nested);
            }
        } catch (IllegalArgumentException expected) {
            check(ShelterPaintingAnchor.active(), "Nested exception restores the target scope");
        } finally {
            ShelterPaintingAnchor.end(targetScope);
        }
        check(!ShelterPaintingAnchor.active(), "Outer scope clears after nested failure");
        String miner = ShelterPaintingAnchor.begin(ShelterPaintingAnchor.MINER_TEMPLATE);
        check(miner == null && ShelterPaintingAnchor.active(), "Miner opens authored-anchor scope");
        String suspended = ShelterPaintingAnchor.begin(Identifier.parse("minecraft:village/plains/houses"));
        check(!ShelterPaintingAnchor.active(), "Foreign pool suspends miner scope");
        ShelterPaintingAnchor.end(suspended);
        check(ShelterPaintingAnchor.active(), "Foreign pool restores miner scope");
        ShelterPaintingAnchor.end(miner);
        check(!ShelterPaintingAnchor.active(), "Miner scope closes");
        return 14;
    }

    private static int transforms() {
        int cases = 0;
        Authored changing = new Authored("changing", 23.0, 5.0, 17.03125, 22, 4, 17, Direction.SOUTH, 4, 2, true);
        Authored baroque = new Authored("baroque", 6.03125, 4.0, 30.0, 6, 3, 30, Direction.EAST, 2, 2, true);
        Authored meditative = new Authored("meditative", 4.5, 4.5, 17.03125, 4, 4, 17, Direction.SOUTH, 1, 1, false);
        Authored miner = new Authored("miner_wanderer", 11.96875, 4.0, 5.5, 11, 3, 5, Direction.WEST, 1, 2, true);
        Authored frame = new Authored("item_frame", 19.03125, 4.5, 21.5, 19, 4, 21, Direction.EAST, 0, 0, false);
        for (Rotation rotation : Rotation.values()) {
            cases += changing.verify(rotation);
            cases += miner.verify(rotation);
            cases += baroque.verify(rotation);
            cases += meditative.verify(rotation);
            cases += frame.verify(rotation);
        }
        return cases;
    }

    private static Direction rotateHorizontal(Direction facing, Rotation rotation) {
        if (facing.getAxis() == Direction.Axis.Y) return facing;
        return switch (rotation) {
            case CLOCKWISE_180 -> facing.getOpposite();
            case COUNTERCLOCKWISE_90 -> facing.getCounterClockWise();
            case CLOCKWISE_90 -> facing.getClockWise();
            default -> facing;
        };
    }

    /** Same center and sizes as Painting.calculateBoundingBox / offsetForPaintingSize. */
    private static AABB paintingBox(BlockPos anchor, Direction facing, int width, int height) {
        Vec3 attached = Vec3.atCenterOf(anchor).relative(facing, -0.46875);
        double horizontal = width % 2 == 0 ? 0.5 : 0.0;
        double vertical = height % 2 == 0 ? 0.5 : 0.0;
        Vec3 center = attached.relative(facing.getCounterClockWise(), horizontal).relative(Direction.UP, vertical);
        Direction.Axis axis = facing.getAxis();
        double xSize = axis == Direction.Axis.X ? 0.0625 : width;
        double zSize = axis == Direction.Axis.Z ? 0.0625 : width;
        return AABB.ofSize(center, xSize, height, zSize);
    }

    private record Authored(String name, double x, double y, double z, int bx, int by, int bz, Direction facing, int width, int height, boolean even) {
        private int verify(Rotation rotation) {
            BlockPos anchor = StructureTemplate.transform(new BlockPos(bx, by, bz), Mirror.NONE, rotation, BlockPos.ZERO).offset(PIECE);
            Vec3 center = StructureTemplate.transform(new Vec3(x, y, z), Mirror.NONE, rotation, BlockPos.ZERO).add(PIECE.getX(), PIECE.getY(), PIECE.getZ());
            BlockPos containing = BlockPos.containing(center);
            if (even) {
                check(!containing.equals(anchor), name + " containing(center) matches the authored anchor at " + rotation);
                Direction rotated = rotateHorizontal(facing, rotation);
                AABB authored = paintingBox(anchor, rotated, width, height);
                AABB shifted = paintingBox(containing, rotated, width, height);
                check(authored.getCenter().equals(center), name + " authored box center left the transformed position at " + rotation);
                check(!authored.equals(shifted), name + " shifted anchor kept the authored box at " + rotation);
            } else {
                check(containing.equals(anchor), name + " odd attachment moved at " + rotation);
            }
            if (name.equals("changing") && rotation == Rotation.CLOCKWISE_180) {
                check(anchor.equals(new BlockPos(377074, 59, 593975)), "R32 changing authored anchor moved");
                check(containing.equals(new BlockPos(377074, 60, 593975)), "R32 changing containing anchor moved");
                check(center.equals(new Vec3(377074.0, 60.0, 593975.96875)), "R32 changing authored center moved");
            }
            return 1;
        }
    }
}
