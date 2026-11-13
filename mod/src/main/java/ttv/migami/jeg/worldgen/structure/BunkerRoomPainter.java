package ttv.migami.jeg.worldgen.structure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Box;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Kind;
import ttv.migami.jeg.worldgen.structure.BunkerProceduralAssembly.Piece;

/**
 * Source room cells. Decoration randomness comes from decorationSeed, not the
 * layout Random and not the 1.7 chest-pool rolls. Unknown blocks fail.
 */
public final class BunkerRoomPainter {
    /** Every hatch call omitted by the earlier room contract. */
    public static final int HATCH_STATEMENTS = 14;
    public static final String FIELD_SUPPLIES = "jeg:chests/valen/field_supplies";
    public static final String RESEARCH_CACHE = "jeg:chests/valen/research_cache";
    private static final Direction[] STAIR = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
    private static final Direction[] DOOR = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    private static final Direction[] TRAP = {Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST};
    private static final Direction[] BED = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
    private static final Direction[] SIDE = {null, null, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private BunkerRoomPainter() {}

    public record Cell(BlockPos pos, BlockState state, String lootTable) {}

    public static List<Cell> paint(Piece piece, long decorationSeed, int surfaceY) {
        Cursor room = new Cursor(piece, new Random(decorationSeed), surfaceY);
        switch (piece.kind) {
            case HUB -> hub(room);
            case CORRIDOR -> corridor(room);
            case BEDROOM_L -> bedroom(room);
            case FUN_JUNCTION -> fun(room);
            case BATHROOM_L -> bath(room);
            case LABORATORY -> lab(room);
            case POWER_ROOM -> power(room);
        }
        return new ArrayList<>(room.cells.values());
    }

    private static void hub(Cursor room) {
        room.air(1, 1, 1, 6, 3, 6);
        room.fill(1, 0, 1, 6, 0, 6, vinyl(1));
        room.fill(1, 4, 1, 6, 4, 6, vinyl(0));
        room.fill(1, 4, 4, 3, 4, 6, cube("reinforced_stone"));
        room.fill(0, 5, 0, 7, 5, 7, cube("reinforced_stone"));
        room.bricks(0, 0, 0, 0, 4, 7);
        room.bricks(1, 0, 7, 6, 4, 7);
        room.bricks(7, 0, 0, 7, 4, 7);
        room.bricks(1, 0, 0, 6, 4, 0);
        hatch(room);
        lamp(room, 2, 5, 2); lamp(room, 5, 5, 2); lamp(room, 5, 5, 5);
        fan(room, 2, 4, 2); fan(room, 5, 4, 2); fan(room, 5, 4, 5);
        room.put(3, 1, 6, cube("deco_tungsten"), null);
        room.chest(4, 1, 6, RESEARCH_CACHE, room.deco(3));
        room.put(5, 1, 6, cube("deco_tungsten"), null);
        room.fill(3, 2, 6, 5, 2, 6, room.stair(room.stairMeta(2) | 4, false));
        room.fill(3, 3, 6, 5, 3, 6, tape(room.deco(2)));
        room.put(3, 1, 4, room.stair(room.stairMeta(1) | 4, false), null);
        room.put(4, 1, 4, room.stair(room.stairMeta(3) | 4, false), null);
        room.put(5, 1, 4, room.stair(room.stairMeta(0) | 4, false), null);
        room.put(4, 2, 4, computer(room, 1), null);
        if (room.paths[0]) room.air(7, 1, 2, 7, 2, 3);
        if (room.paths[1]) room.air(3, 1, 0, 4, 2, 0);
        if (room.paths[2]) room.air(0, 1, 2, 0, 2, 3);
    }

    private static void hatch(Cursor room) {
        int h = room.surfaceY - room.box.minY;
        room.putOutside(0, h, 5, slab(false), null);
        room.fillOutside(1, h, 4, 1, h, 6, room.stair(room.stairMeta(0), false));
        room.putOutside(2, h, 3, slab(false), null);
        room.putOutside(2, h, 4, room.stair(room.stairMeta(2), false), null);
        room.putOutside(2, h, 5, steelHatch(room.decoModel(8) >> 2), null);
        room.putOutside(2, h, 6, room.stair(room.stairMeta(3), false), null);
        room.putOutside(2, h, 7, slab(false), null);
        room.fillOutside(3, h, 4, 3, h, 6, room.stair(room.stairMeta(1), false));
        room.putOutside(4, h, 5, slab(false), null);
        room.fillOutside(1, 6, 4, 1, h - 1, 6, cube("reinforced_stone"));
        room.fillOutside(2, 1, 6, 2, h - 1, 6, cube("reinforced_stone"));
        room.fillOutside(3, 6, 4, 3, h - 1, 6, cube("reinforced_stone"));
        room.fillOutside(2, 6, 4, 2, h - 1, 4, cube("reinforced_stone"));
        room.fillOutside(2, 1, 5, 2, h - 1, 5, ladder(room.deco(2)));
        if (room.hatchStatements != HATCH_STATEMENTS) throw new IllegalStateException("hatch statement count");
    }

    private static void corridor(Cursor room) {
        room.air(1, 1, 1, 4, 3, 5);
        room.fill(1, 0, 1, 4, 0, 5, vinyl(1));
        room.fill(1, 4, 1, 4, 4, 5, vinyl(0));
        room.fill(0, 5, 0, 5, 5, 6, cube("reinforced_stone"));
        room.bricks(0, 0, 0, 0, 4, 6);
        room.bricks(1, 0, 6, 4, 4, 6);
        room.bricks(5, 0, 0, 5, 4, 6);
        room.bricks(1, 0, 0, 4, 4, 0);
        room.fill(2, 5, 3, 3, 5, 3, lamp());
        room.fill(2, 4, 3, 3, 4, 3, fanState(0));
        int west = room.stairMeta(0), east = room.stairMeta(1), north = room.stairMeta(2), south = room.stairMeta(3);
        int decoE = room.deco(4), decoW = room.deco(5);
        for (int i = 0; i <= 1; i++) {
            int x = 1 + i * 3;
            int side = i < 1 ? east : west;
            int sideDeco = i < 1 ? decoW : decoE;
            switch (room.piece.rolls[i]) {
                case 1 -> { room.put(x, 1, 2, room.stair(south | 4, false), null); room.put(x, 1, 4, room.stair(north, true), null); room.put(x, 2, 2, computer(room, 1), null); }
                case 2 -> { room.put(x, 1, 2, room.stair(south, true), null); room.put(x, 1, 3, room.stair(side, true), null); room.put(x, 1, 4, room.stair(north, true), null); }
                case 3 -> { room.put(x, 1, 2, room.stair(south | 4, false), null); room.put(x, 1, 3, room.stair(side | 4, false), null); room.put(x, 1, 4, room.stair(north | 4, false), null); room.put(x, 2, 2, Blocks.FLOWER_POT.defaultBlockState(), null); }
                case 4 -> {
                    room.fill(x, 1, 1, x, 3, 1, cube("deco_tungsten"));
                    room.put(x, 1, 3, cube("deco_tungsten"), null);
                    room.fill(x, 3, 2, x, 3, 4, room.stair(side, false));
                    room.fill(x, 1, 5, x, 3, 5, cube("deco_tungsten"));
                    room.fill(x, 1, 2, x, 2, 2, tape(sideDeco));
                    room.fill(x, 1, 4, x, 2, 4, tape(sideDeco));
                    room.put(x, 2, 3, computer(room, i < 1 ? 3 : 2), null);
                }
                case 5 -> {
                    room.put(x, 1, 1, fence(), null); room.put(x, 2, 1, plate(false), null);
                    room.put(x, 1, 3, room.stair(south | 4, false), null); room.put(x, 1, 4, room.stair(north | 4, false), null);
                    room.put(x, 2, 3, radio(i < 1 ? room.deco(4) : room.deco(5)), null);
                }
                default -> { room.put(x, 1, 2, room.stair(south, true), null); room.put(x, 1, 4, room.stair(north, true), null); room.put(x, 1, 3, fence(), null); room.put(x, 2, 3, plate(true), null); }
            }
        }
        room.door(2, 1, 0, 1, true);
        room.door(3, 1, 0, 1, false);
        if (room.paths[0]) room.air(2, 1, 6, 3, 2, 6);
    }

    private static void bedroom(Cursor room) {
        room.air(4, 1, 1, 8, 3, 4); room.air(1, 1, 5, 8, 3, 9);
        room.fill(4, 0, 1, 8, 0, 4, vinyl(1));
        room.fill(1, 0, 5, 8, 0, 9, vinyl(1));
        room.fill(4, 4, 1, 8, 4, 4, vinyl(0));
        room.fill(1, 4, 5, 8, 4, 9, vinyl(0));
        room.fill(3, 5, 0, 9, 5, 3, cube("reinforced_stone"));
        room.fill(0, 5, 4, 9, 5, 10, cube("reinforced_stone"));
        room.bricks(0, 0, 4, 0, 4, 10); room.bricks(1, 0, 10, 8, 4, 10); room.bricks(9, 0, 0, 9, 4, 10);
        room.bricks(4, 0, 0, 8, 4, 0); room.bricks(3, 0, 0, 3, 4, 4); room.bricks(1, 0, 4, 2, 4, 4);
        lamp(room, 3, 5, 7); lamp(room, 6, 5, 7); lamp(room, 6, 5, 3);
        fan(room, 3, 4, 7); fan(room, 6, 4, 7); fan(room, 6, 4, 3);
        room.bed(1, 5, 1, 1); room.bed(1, 5, 1, 3); room.bed(2, 3, 1, 6); room.bed(2, 1, 1, 6);
        int east = room.stairMeta(1), south = room.stairMeta(3), west = room.stairMeta(0), north = room.stairMeta(2);
        room.put(4, 1, 2, room.stair(east | 4, false), null); room.put(4, 1, 4, room.stair(east | 4, false), null);
        room.put(4, 1, 5, room.stair(south | 4, false), null); room.put(2, 1, 5, room.stair(south | 4, false), null);
        room.put(4, 2, 4, radio(room.deco(4)), null);
        room.put(8, 1, 3, room.stair(south | 4, false), null); room.put(8, 1, 4, room.stair(north | 4, false), null);
        room.put(8, 1, 5, Blocks.NOTE_BLOCK.defaultBlockState(), null);
        room.put(8, 2, 4, microwave(room.deco(4)), null);
        room.put(6, 1, 9, room.stair(west | 4, false), null); room.put(5, 1, 9, room.stair(north | 4, false), null);
        room.put(4, 1, 9, room.stair(east | 4, false), null); room.put(5, 1, 8, room.stair(south, true), null);
        room.put(5, 2, 9, computer(room, 0), null);
        room.cabinet(3, 1, 9, FIELD_SUPPLIES, 0);
        room.chest(8, 1, 7, FIELD_SUPPLIES, room.deco(4)); room.chest(8, 2, 7, FIELD_SUPPLIES, room.deco(4));
        room.fill(8, 1, 8, 8, 2, 8, cube("deco_tungsten"));
        room.chest(8, 1, 9, FIELD_SUPPLIES, room.deco(4)); room.chest(8, 2, 9, FIELD_SUPPLIES, room.deco(4));
        room.fill(8, 3, 7, 8, 3, 9, trap(room.decoModel(2) >> 2));
        room.door(7, 1, 0, 1, true); room.door(8, 1, 0, 1, false);
        if (room.paths[0]) room.air(0, 1, 8, 0, 2, 9);
    }

    private static void fun(Cursor room) {
        room.air(1, 1, 1, 6, 3, 10);
        room.fill(1, 0, 1, 6, 0, 10, vinyl(1));
        room.fill(1, 4, 1, 6, 4, 10, vinyl(0));
        room.fill(0, 5, 0, 7, 5, 11, cube("reinforced_stone"));
        room.bricks(0, 0, 0, 0, 4, 11); room.bricks(1, 0, 11, 6, 4, 11);
        room.bricks(7, 0, 0, 7, 4, 11); room.bricks(1, 0, 0, 6, 4, 0);
        lamp(room, 2, 5, 3); room.fill(5, 5, 5, 5, 5, 6, lamp()); lamp(room, 2, 5, 8);
        fan(room, 2, 4, 3); room.fill(5, 4, 5, 5, 4, 6, fanState(0)); fan(room, 2, 4, 8);
        int west = room.stairMeta(0), east = room.stairMeta(1), north = room.stairMeta(2), south = room.stairMeta(3);
        room.put(1, 1, 1, room.stair(east, true), null); room.put(2, 1, 1, room.stair(south, true), null); room.put(3, 1, 1, room.stair(west, true), null);
        room.put(1, 1, 4, room.stair(south, true), null); room.put(1, 1, 5, room.stair(east, true), null);
        room.fill(1, 1, 6, 2, 1, 6, room.stair(north, true)); room.put(3, 1, 6, room.stair(west, true), null);
        room.put(1, 1, 3, fence(), null); room.put(1, 2, 3, plate(false), null);
        room.put(3, 1, 4, fence(), null); room.put(3, 2, 4, plate(false), null);
        room.put(6, 1, 2, fence(), null); room.put(6, 2, 2, plate(false), null);
        room.chest(6, 1, 3, FIELD_SUPPLIES, room.deco(4));
        room.put(1, 1, 8, room.stair(south | 4, false), null); room.put(1, 1, 9, room.stair(east | 4, false), null); room.put(1, 1, 10, room.stair(north | 4, false), null);
        room.put(2, 1, 8, room.stair(south, true), null); room.put(1, 2, 9, computer(room, 3), null);
        if (room.rand.nextBoolean()) { room.put(1, 2, 8, Blocks.FLOWER_POT.defaultBlockState(), null); room.rand.nextInt(16); }
        room.fill(6, 1, 8, 6, 2, 8, Blocks.NOTE_BLOCK.defaultBlockState());
        room.put(6, 1, 9, cube("deco_tungsten"), null);
        room.put(6, 2, 9, tape(room.deco(4)), null);
        room.fill(6, 3, 8, 6, 3, 9, slab(false));
        room.lever(5, 1, 9, 2, true);
        room.door(4, 1, 0, 1, true); room.door(5, 1, 0, 1, false);
        if (room.paths[0]) room.air(7, 1, 5, 7, 2, 6);
        if (room.paths[1]) room.air(4, 1, 11, 5, 2, 11);
    }

    private static void bath(Cursor room) {
        room.air(1, 1, 1, 7, 3, 9);
        room.fill(1, 0, 1, 7, 0, 9, vinyl(1));
        room.fill(1, 4, 1, 7, 4, 9, vinyl(0));
        room.fill(0, 5, 0, 8, 5, 10, cube("reinforced_stone"));
        room.bricks(0, 0, 0, 0, 4, 10); room.bricks(1, 0, 10, 7, 4, 10);
        room.bricks(8, 0, 0, 8, 4, 10); room.bricks(1, 0, 0, 7, 4, 0);
        for (int[] p : new int[][] {{2, 3}, {2, 7}, {5, 7}, {5, 3}}) { lamp(room, p[0], 5, p[1]); fan(room, p[0], 4, p[1]); }
        for (int i = 2; i <= 8; i += 2) {
            int level = room.rand.nextInt(4);
            room.put(1, 1, i, level == 0 ? Blocks.CAULDRON.defaultBlockState() : Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, level), null);
            room.put(1, 1, i + 1, slab(true), null);
            room.put(1, 2, i, hook(room.tripwire(3)), null);
        }
        room.put(4, 1, 9, pole(3), null); fan(room, 4, 2, 9, room.deco(2)); room.put(3, 2, 9, button(room.buttonMeta(2)), null);
        room.put(6, 1, 9, pole(3), null); fan(room, 6, 2, 9, room.deco(2)); room.put(7, 2, 9, button(room.buttonMeta(1)), null);
        for (int i = 1; i <= 5; i += 2) {
            room.door(5, 1, i, 0, false);
            room.fill(5, 1, i + 1, 5, 2, i + 1, steelCorner(room.deco(2)));
            room.fill(6, 1, i + 1, 7, 2, i + 1, steelWall(room.deco(2)));
            room.put(7, 1, i, pipe("deco_pipe_rim", 0, room), null);
            room.put(7, 2, i, trap(room.decoModel(2) >> 2), null);
        }
        room.door(2, 1, 0, 1, true); room.door(3, 1, 0, 1, false);
        if (room.paths[0]) room.air(8, 1, 7, 8, 2, 8);
    }

    private static void lab(Cursor room) {
        room.air(1, 1, 1, 7, 3, 11);
        room.fill(1, 0, 1, 7, 0, 11, vinyl(1));
        room.fill(1, 4, 1, 7, 4, 11, vinyl(0));
        room.fill(0, 5, 0, 8, 5, 12, cube("reinforced_stone"));
        room.fill(0, 0, 0, 0, 4, 12, cube("brick_concrete"));
        room.fill(1, 0, 12, 7, 4, 12, cube("brick_concrete"));
        room.fill(8, 0, 0, 8, 4, 12, cube("brick_concrete"));
        room.fill(1, 0, 0, 7, 4, 0, cube("brick_concrete"));
        for (int x = 3; x <= 5; x += 2) for (int z = 3; z <= 9; z += 3) { lamp(room, x, 5, z); fan(room, x, 4, z); }
        int west = room.stairMeta(0), east = room.stairMeta(1), north = room.stairMeta(2), south = room.stairMeta(3);
        room.put(1, 1, 1, room.stair(east, true), null); room.put(2, 1, 1, room.stair(south, true), null);
        room.put(3, 1, 1, room.stair(west, true), null); room.put(4, 1, 1, fence(), null); room.put(4, 2, 1, plate(false), null);
        int decoE = room.deco(4), decoW = room.deco(5);
        room.fill(1, 1, 5, 1, 3, 5, cube("deco_tungsten"));
        room.put(1, 1, 6, steel(), null); room.put(1, 2, 6, computer(room, 3), null); room.put(1, 3, 6, tape(decoW), null);
        room.fill(1, 1, 7, 1, 3, 7, tape(decoW));
        room.fill(1, 1, 8, 1, 3, 8, cube("deco_tungsten"));
        room.fill(1, 1, 9, 1, 1, 10, tape(decoW));
        room.fill(1, 2, 9, 1, 2, 10, room.stair(east | 4, false));
        room.fill(1, 3, 9, 1, 3, 10, tape(decoW));
        room.fill(1, 1, 11, 1, 3, 11, cube("deco_tungsten"));
        room.chest(3, 1, 4, FIELD_SUPPLIES, room.deco(2));
        room.put(3, 1, 5, room.stair(south | 4, false), null);
        room.fill(4, 1, 5, 4, 1, 7, room.stair(west | 4, false));
        room.put(3, 1, 7, room.stair(north, true), null);
        room.put(3, 1, 9, room.stair(south | 4, false), null);
        room.fill(4, 1, 9, 4, 1, 11, room.stair(west | 4, false));
        room.put(3, 1, 11, room.stair(north, true), null);
        room.put(3, 2, 5, Blocks.FLOWER_POT.defaultBlockState(), null);
        room.put(4, 2, 6, computer(room, 2), null); room.put(4, 2, 10, computer(room, 2), null);
        room.put(7, 1, 3, room.stair(west | 4, false), null);
        room.put(7, 2, 3, cube("deco_red_copper"), null);
        room.put(7, 3, 3, room.stair(west, false), null);
        room.lever(6, 2, 3, 2, true);
        room.fill(7, 1, 4, 7, 2, 4, pole(decoE));
        room.put(7, 3, 4, steel(), null);
        room.put(7, 1, 5, cube("deco_tungsten"), null);
        room.put(7, 1, 6, tape(decoE), null);
        room.put(7, 1, 7, cube("deco_tungsten"), null);
        room.fill(7, 2, 5, 7, 2, 7, room.stair(west | 4, false));
        room.fill(7, 3, 5, 7, 3, 7, tape(decoE));
        room.put(7, 1, 9, fence(), null); room.put(7, 2, 9, plate(false), null);
        room.chest(7, 1, 10, RESEARCH_CACHE, room.deco(4));
        room.door(5, 1, 0, 1, true); room.door(6, 1, 0, 1, false);
        if (room.paths[0]) room.air(0, 1, 2, 0, 2, 3);
        if (room.paths[1]) room.air(5, 1, 12, 6, 2, 12);
    }

    private static void power(Cursor room) {
        room.air(1, 1, 1, 10, 3, 10);
        room.fill(1, 0, 1, 10, 0, 10, vinyl(1));
        room.fill(1, 4, 1, 10, 4, 10, vinyl(0));
        room.fill(0, 5, 0, 11, 5, 11, cube("reinforced_stone"));
        room.bricks(0, 0, 0, 11, 4, 0); room.bricks(0, 0, 1, 0, 4, 10);
        room.bricks(0, 0, 11, 11, 4, 11); room.bricks(11, 0, 1, 11, 4, 10);
        room.bricks(5, 1, 1, 5, 3, 6); room.bricks(6, 1, 6, 10, 3, 6);
        for (int[] p : new int[][] {{3, 2}, {3, 5}, {3, 8}, {6, 8}, {9, 8}}) { lamp(room, p[0], 5, p[1]); fan(room, p[0], 4, p[1]); }
        room.fill(7, 2, 6, 9, 2, 6, glass());
        int decoE = room.deco(5), decoW = room.deco(4), decoN = room.deco(3), decoS = room.deco(2);
        int south = room.stairMeta(3), north = room.stairMeta(2), west = room.stairMeta(1), east = room.stairMeta(0);
        int powerType = room.piece.rolls[0];
        if (powerType == 1) {
            room.put(6, 1, 1, cube("concrete_lime"), null);
            room.put(6, 2, 1, HbmRoomStates.get("cable_detector", "open=false"), null);
            room.put(6, 3, 1, cube("concrete_lime"), null);
            room.lever(6, 2, 2, 3, false);
            for (int i = 7; i <= 9; i += 2) { room.put(i, 1, 1, scaffold(8), null); room.put(i, 2, 1, machine("machine_diesel", decoE), null); }
            room.put(8, 2, 1, pipe("deco_pipe_rim_rusted", 4, room), null);
            room.fill(7, 3, 1, 9, 3, 1, room.stair(south, false));
            room.fill(10, 1, 1, 10, 1, 3, steel());
            room.put(10, 2, 1, cube("deco_red_copper"), null);
            room.put(10, 3, 1, steel(), null);
            room.put(10, 2, 2, grate(7), null);
            room.put(10, 3, 2, computer(room, 2), null);
            room.fill(10, 2, 3, 10, 3, 3, tape(decoW));
            room.fill(9, 1, 2, 9, 1, 3, grate(7));
            room.fill(9, 1, 5, 10, 1, 5, barrel("barrel_corroded"));
            room.put(10, 2, 5, barrel("barrel_corroded"), null);
            room.fill(6, 1, 5, 6, 2, 5, barrel("barrel_corroded"));
            room.put(6, 1, 2, barrel("barrel_corroded"), null);
        } else if (powerType == 2) {
            for (int i = 7; i <= 9; i += 2) {
                room.fill(i, 1, 2, i, 1, 4, cube("deco_lead"));
                room.fill(i, 2, 2, i, 2, 4, cube("block_lead"));
                room.fill(i, 3, 2, i, 3, 4, cube("deco_lead"));
            }
            room.put(8, 1, 4, cube("concrete_lime"), null);
            room.put(8, 2, 4, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, false), null);
            room.put(8, 3, 4, cube("concrete_lime"), null);
            room.lever(8, 2, 5, 3, true);
            room.put(8, 1, 3, cube("pwr_fuel"), null);
            room.put(8, 2, 3, cube("pwr_control"), null);
            room.put(8, 3, 3, cube("pwr_fuel"), null);
            room.put(8, 1, 2, cube("block_copper"), null);
            room.put(8, 2, 2, cube("block_lead"), null);
            room.put(8, 3, 2, cube("block_copper"), null);
            room.put(8, 1, 1, cube("pwr_channel"), null);
            room.put(8, 2, 1, cube("machine_condenser"), null);
            room.put(8, 3, 1, cube("pwr_channel"), null);
            room.fill(9, 1, 1, 9, 3, 1, steel());
            room.put(10, 1, 1, grate(7), null);
            room.put(10, 2, 1, computer(room, 1), null);
            room.put(10, 3, 1, tape(decoN), null);
            room.fill(6, 1, 1, 7, 1, 1, pipe("deco_pipe_quad_rusted", 4, room));
            room.put(7, 3, 1, pipe("deco_pipe_quad_rusted", 4, room), null);
            room.put(6, 3, 1, gauge(decoN), null);
            room.chest(6, 1, 2, RESEARCH_CACHE, decoN);
        } else {
            room.fill(6, 1, 1, 6, 3, 1, pipe("deco_pipe_framed_rusted", 0, room));
            for (int i = 7; i <= 9; i += 2) { room.put(i, 1, 1, machine("machine_electric_furnace", decoN), null); room.put(i, 2, 1, beam(2), null); room.put(i, 3, 1, machine("machine_electric_furnace", decoN), null); }
            room.put(8, 1, 1, cube("deco_red_copper"), null);
            room.put(8, 2, 1, cube("concrete_lime"), null);
            room.put(8, 3, 1, cube("deco_red_copper"), null);
            room.lever(8, 2, 2, 3, true);
            for (int i = 1; i <= 3; i += 2) {
                room.put(10, i, 1, steel(), null);
                room.fill(10, i, 2, 10, i, 4, pipe("deco_pipe_quad_rusted", 8, room));
                room.put(10, i, 5, steel(), null);
            }
            room.put(10, 2, 1, pipe("deco_pipe_framed_rusted", 0, room), null);
            room.put(10, 2, 5, gauge(decoW), null);
            room.put(6, 1, 5, barrel("barrel_plastic"), null);
            room.chest(7, 1, 5, FIELD_SUPPLIES, decoS); room.chest(9, 1, 5, FIELD_SUPPLIES, decoS);
        }
        room.fill(1, 1, 1, 1, 1, 5, room.stair(west | 4, false));
        room.fill(1, 1, 6, 1, 3, 6, HbmRoomStates.get("concrete_pillar", "axis=y"));
        room.fill(1, 3, 1, 1, 3, 5, room.stair(west, false));
        room.put(1, 2, 1, cube("machine_transformer"), null);
        room.put(1, 2, 2, diode(decoN), null);
        room.put(1, 2, 3, HbmRoomStates.get("capacitor_copper", "facing=down"), null);
        room.put(1, 2, 4, cube("deco_red_copper"), null);
        room.put(1, 2, 5, HbmRoomStates.get("cable_switch", "open=false"), null);
        for (int i = 1; i <= 5; i += 4) {
            room.put(i, 1, 10, cube("deco_beryllium"), null);
            room.put(i, 2, 10, scaffold(0), null);
            room.put(i, 3, 10, cube("deco_beryllium"), null);
        }
        room.put(2, 1, 10, scaffold(0), null);
        room.put(3, 1, 10, cube("deco_tungsten"), null);
        room.put(4, 1, 10, scaffold(0), null);
        room.put(2, 2, 10, tape(decoS), null); room.put(3, 2, 10, computer(room, 0), null); room.put(4, 2, 10, tape(decoS), null);
        room.fill(2, 3, 10, 4, 3, 10, tape(decoS));
        room.fill(8, 1, 10, 10, 1, 10, room.stair(north | 4, false));
        room.put(10, 1, 9, room.stair(east | 4, false), null);
        room.put(9, 1, 9, room.stair(south, true), null);
        room.put(8, 2, 10, Blocks.FLOWER_POT.defaultBlockState(), null);
        room.put(9, 2, 10, computer(room, 0), null);
        room.chest(1, 1, 7, FIELD_SUPPLIES, decoE); room.cabinet(7, 1, 10, FIELD_SUPPLIES, 0);
        room.door(3, 1, 0, 1, true); room.door(4, 1, 0, 1, false); room.doorFixed(5, 1, 3, 0);
        if (room.paths[0]) room.air(11, 1, 7, 11, 2, 8);
    }

    private static void lamp(Cursor room, int x, int y, int z) { room.put(x, y, z, lamp(), null); }
    private static void fan(Cursor room, int x, int y, int z) { fan(room, x, y, z, 0); }
    private static void fan(Cursor room, int x, int y, int z, int meta) { room.put(x, y, z, fanState(meta), null); }
    private static BlockState fanState(int meta) {
        Direction facing = switch (meta) {
            case 0 -> Direction.DOWN; case 1 -> Direction.UP; case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH; case 4 -> Direction.WEST; case 5 -> Direction.EAST;
            default -> throw new IllegalStateException("fan meta " + meta);
        };
        return HbmRoomStates.get("fan", "facing=" + facing.getSerializedName(), "powered=false");
    }
    private static BlockState vinyl(int meta) {
        if (meta == 0) return cube("vinyl_tile_large");
        if (meta == 1) return cube("vinyl_tile_small");
        throw new IllegalStateException("vinyl meta " + meta);
    }
    private static BlockState lamp() { return HbmRoomStates.get("reinforced_lamp", "lit=false"); }
    private static BlockState steel() { return cube("deco_steel"); }
    private static BlockState glass() { return cube("reinforced_glass"); }
    private static BlockState cube(String path) { return HbmRoomStates.get(path); }
    private static BlockState tape(int meta) { return HbmRoomStates.get("tape_recorder", "facing=" + horizontal(meta).getSerializedName()); }
    private static BlockState beam(int meta) { return HbmRoomStates.get("steel_beam", "facing=" + horizontal(meta).getSerializedName(), "waterlogged=false"); }
    private static BlockState pole(int meta) { return beam(meta); }
    private static Direction horizontal(int meta) {
        if (meta < 0 || meta >= SIDE.length || SIDE[meta] == null) throw new IllegalStateException("horizontal meta " + meta);
        return SIDE[meta];
    }
    private static Direction modelFacing(Cursor room, int meta) {
        int bits = room.decoModel(meta) >> 2;
        return switch (bits & 3) { case 0 -> Direction.NORTH; case 1 -> Direction.SOUTH; case 2 -> Direction.WEST; default -> Direction.EAST; };
    }
    private static BlockState computer(Cursor room, int meta) { return HbmRoomStates.get("deco_computer", "facing=" + modelFacing(room, meta).getSerializedName()); }
    private static BlockState radio(int meta) { return HbmRoomStates.get("radiorec", "facing=" + horizontal(meta).getSerializedName()); }
    private static BlockState microwave(int meta) { return HbmRoomStates.get("machine_microwave", "facing=" + horizontal(meta).getSerializedName()); }
    private static BlockState machine(String path, int meta) {
        if (path.equals("machine_electric_furnace")) return HbmRoomStates.get(path, "facing=" + horizontal(meta).getSerializedName(), "lit=false");
        return HbmRoomStates.get(path, "facing=" + horizontal(meta).getSerializedName());
    }
    private static BlockState steelWall(int meta) { return HbmRoomStates.get("steel_wall", "facing=" + horizontal(meta).getSerializedName(), "waterlogged=false"); }
    private static BlockState steelCorner(int meta) { return HbmRoomStates.get("steel_corner", "facing=" + horizontal(meta).getSerializedName()); }
    private static BlockState grate(int height) { return HbmRoomStates.get("steel_grate", "height=" + height, "waterlogged=false"); }
    private static BlockState scaffold(int meta) {
        String orient = meta == 0 ? "ns_upright" : meta == 8 ? "ew_flat" : null;
        if (orient == null) throw new IllegalStateException("scaffold meta " + meta);
        return HbmRoomStates.get("steel_scaffold", "orient=" + orient, "waterlogged=false");
    }
    private static BlockState barrel(String path) { return HbmRoomStates.get(path, "north=false", "east=false", "south=false", "west=false"); }
    private static BlockState pipe(String path, int meta, Cursor room) {
        if (room.piece.coordMode % 2 != 0) meta ^= 12;
        String axis = switch (meta & 12) { case 0, 12 -> "y"; case 4 -> "x"; case 8 -> "z"; default -> throw new IllegalStateException("pillar meta " + meta); };
        return HbmRoomStates.get(path, "axis=" + axis);
    }
    private static BlockState gauge(int meta) { return HbmRoomStates.get("fluid_duct_gauge", "facing=" + horizontal(meta).getSerializedName()); }
    private static BlockState diode(int meta) {
        return HbmRoomStates.get("cable_diode", "facing=" + horizontal(meta).getSerializedName(), "north=false", "east=false", "south=false", "west=false", "up=false", "down=false");
    }
    private static BlockState slab(boolean top) { return HbmRoomStates.get("concrete_slab", "type=" + (top ? "top" : "bottom"), "waterlogged=false"); }
    private static BlockState fence() { return Blocks.OAK_FENCE.defaultBlockState(); }
    private static BlockState plate(boolean powered) { return Blocks.OAK_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWERED, powered); }
    private static BlockState ladder(int meta) { return HbmRoomStates.get("ladder_sturdy", "facing=" + horizontal(meta).getSerializedName(), "waterlogged=false"); }
    private static BlockState steelHatch(int meta) {
        return HbmRoomStates.get("trapdoor_steel", "facing=" + TRAP[meta & 3].getSerializedName(), "half=" + ((meta & 8) != 0 ? "top" : "bottom"), "open=" + ((meta & 4) != 0), "powered=false", "waterlogged=false");
    }
    private static BlockState trap(int meta) {
        return Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, TRAP[meta & 3]).setValue(BlockStateProperties.HALF, (meta & 8) != 0 ? Half.TOP : Half.BOTTOM).setValue(BlockStateProperties.OPEN, (meta & 4) != 0);
    }
    private static BlockState hook(int meta) {
        return Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, BED[meta & 3]).setValue(BlockStateProperties.ATTACHED, false);
    }
    private static BlockState button(int meta) {
        int bits = meta & 7;
        AttachFace face = bits == 0 ? AttachFace.CEILING : bits == 5 ? AttachFace.FLOOR : AttachFace.WALL;
        Direction facing = Direction.NORTH;
        if (bits == 1) facing = Direction.EAST; else if (bits == 2) facing = Direction.WEST; else if (bits == 3) facing = Direction.SOUTH; else if (bits == 4) facing = Direction.NORTH;
        return Blocks.STONE_BUTTON.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, face).setValue(BlockStateProperties.HORIZONTAL_FACING, facing).setValue(BlockStateProperties.POWERED, false);
    }

    private static final class Cursor {
        final Piece piece; final Random rand; final Box box; final boolean[] paths; final int surfaceY;
        final Map<BlockPos, Cell> cells = new LinkedHashMap<>();
        int hatchStatements;
        Cursor(Piece piece, Random rand, int surfaceY) {
            if (piece.kind == null || piece.box == null || piece.paths == null) throw new IllegalStateException("incomplete piece");
            this.piece = piece; this.rand = rand; this.box = piece.box; this.paths = piece.paths; this.surfaceY = surfaceY;
        }
        void air(int x0, int y0, int z0, int x1, int y1, int z1) { fill(x0, y0, z0, x1, y1, z1, Blocks.AIR.defaultBlockState()); }
        void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) { span(x0, y0, z0, x1, y1, z1, state, true); }
        void fillOutside(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) { hatchStatements++; span(x0, y0, z0, x1, y1, z1, state, false); }
        void put(int x, int y, int z, BlockState state, String loot) { if (state == null) throw new IllegalStateException("null state"); place(x, y, z, state, loot, true); }
        void putOutside(int x, int y, int z, BlockState state, String loot) { hatchStatements++; place(x, y, z, state, loot, false); }
        void chest(int x, int y, int z, String table, int meta) { put(x, y, z, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, horizontal(meta)).setValue(BlockStateProperties.CHEST_TYPE, net.minecraft.world.level.block.state.properties.ChestType.SINGLE).setValue(BlockStateProperties.WATERLOGGED, false), table); }
        void cabinet(int x, int y, int z, String table, int modelMeta) { put(x, y, z, HbmRoomStates.get("filing_cabinet_green", "facing=" + modelFacing(this, modelMeta).getSerializedName()), table); }
        void span(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state, boolean clipToPiece) {
            if (clipToPiece && (box.minY + y0 < box.minY || box.minY + y1 > box.maxY)) return;
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int y = y0; y <= y1; y++) place(x, y, z, state, null, clipToPiece);
        }
        void place(int x, int y, int z, BlockState state, String loot, boolean clipToPiece) {
            int wx, wz;
            switch (piece.coordMode) {
                case 1 -> { wx = box.maxX - z; wz = box.minZ + x; }
                case 2 -> { wx = box.maxX - x; wz = box.maxZ - z; }
                case 3 -> { wx = box.minX + z; wz = box.maxZ - x; }
                default -> { wx = box.minX + x; wz = box.minZ + z; }
            }
            int wy = box.minY + y;
            if (clipToPiece && (wx < box.minX || wx > box.maxX || wy < box.minY || wy > box.maxY || wz < box.minZ || wz > box.maxZ)) return;
            cells.put(new BlockPos(wx, wy, wz), new Cell(new BlockPos(wx, wy, wz), state, loot));
        }
        void bricks(int x0, int y0, int z0, int x1, int y1, int z1) {
            if (box.minY + y0 < box.minY || box.minY + y1 > box.maxY) return;
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int y = y0; y <= y1; y++) {
                float chance = rand.nextFloat();
                BlockState state = chance < 0.4F ? cube("brick_concrete") : chance < 0.7F ? cube("brick_concrete_mossy") : chance < 0.9F ? cube("brick_concrete_cracked") : cube("brick_concrete_broken");
                place(x, y, z, state, null, true);
            }
        }
        void door(int x, int y, int z, int facing, boolean right) { doorState(x, y, z, facing, right, rand.nextBoolean()); }
        void doorFixed(int x, int y, int z, int facing) { doorState(x, y, z, facing, false, false); }
        void doorState(int x, int y, int z, int facing, boolean right, boolean open) {
            int wx = worldX(x, z), wz = worldZ(x, z), below = box.minY + y - 1;
            Cell floor = cells.get(new BlockPos(wx, below, wz));
            if (floor == null || floor.state.isAir()) return;
            int meta = facing;
            if (piece.coordMode == 1) meta = jmod(meta + 1, 4);
            else if (piece.coordMode == 2) meta ^= 2;
            else if (piece.coordMode == 3) meta = jmod(meta + 3, 4);
            BlockState door = HbmRoomStates.get("door_bunker", "facing=" + DOOR[meta & 3].getSerializedName(), "hinge=" + (right ? "right" : "left"), "open=" + open, "powered=false");
            place(x, y, z, door.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), null, true);
            place(x, y + 1, z, door.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), null, true);
        }
        void lever(int x, int y, int z, int meta, boolean consume) {
            boolean on = consume && rand.nextBoolean();
            if (!consume && meta == 3) on = false;
            if (meta <= 0 || meta >= 7) { if (piece.coordMode == 1 || piece.coordMode == 3) meta ^= 7; }
            else if (meta >= 5) { if (piece.coordMode == 1 || piece.coordMode == 3) meta = jmod(meta + 1, 2) + 5; }
            else meta = buttonMeta(meta);
            int bits = meta & 7;
            AttachFace face = bits == 0 || bits >= 5 ? AttachFace.FLOOR : AttachFace.WALL;
            Direction facing = Direction.NORTH;
            if (bits == 1) facing = Direction.EAST; else if (bits == 2) facing = Direction.WEST; else if (bits == 3) facing = Direction.SOUTH; else if (bits == 4) facing = Direction.NORTH;
            put(x, y, z, Blocks.LEVER.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, face).setValue(BlockStateProperties.HORIZONTAL_FACING, facing).setValue(BlockStateProperties.POWERED, on), null);
        }
        void bed(int meta, int x, int y, int z) {
            int dx = 0, dz = 0;
            switch (meta & 3) { case 1 -> dx = -1; case 2 -> dz = -1; case 3 -> dx = 1; default -> dz = 1; }
            if (piece.coordMode == 1) meta = jmod(meta + 1, 4);
            else if (piece.coordMode == 2) meta ^= 2;
            else if (piece.coordMode == 3) meta = jmod(meta - 1, 4);
            Direction facing = BED[meta & 3];
            BlockState foot = Blocks.BED.gray().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing).setValue(BlockStateProperties.BED_PART, BedPart.FOOT).setValue(BlockStateProperties.OCCUPIED, false);
            put(x, y, z, foot, null);
            put(x + dx, y, z + dz, foot.setValue(BlockStateProperties.BED_PART, BedPart.HEAD), null);
        }
        BlockState stair(int meta, boolean oak) {
            BlockState state = oak ? Blocks.OAK_STAIRS.defaultBlockState() : HbmRoomStates.get("concrete_smooth_stairs");
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, STAIR[meta & 3]).setValue(BlockStateProperties.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM).setValue(BlockStateProperties.STAIRS_SHAPE, net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT);
        }
        int deco(int meta) {
            return switch (piece.coordMode) {
                case 1 -> switch (meta) { case 2 -> 5; case 3 -> 4; case 4 -> 2; case 5 -> 3; default -> fail(meta); };
                case 2 -> switch (meta) { case 2 -> 3; case 3 -> 2; case 4 -> 5; case 5 -> 4; default -> fail(meta); };
                case 3 -> switch (meta) { case 2 -> 4; case 3 -> 5; case 4 -> 3; case 5 -> 2; default -> fail(meta); };
                default -> switch (meta) { case 2 -> 2; case 3 -> 3; case 4 -> 4; case 5 -> 5; default -> fail(meta); };
            };
        }
        int decoModel(int meta) {
            if (piece.coordMode == 1) meta = (meta & 3) < 2 ? meta ^ 3 : meta ^ 2;
            else if (piece.coordMode == 2) meta ^= 1;
            else if (piece.coordMode == 3) meta = (meta & 3) < 2 ? meta ^ 2 : meta ^ 3;
            return meta << 2;
        }
        int stairMeta(int meta) {
            if (piece.coordMode == 1) meta = (meta & 3) < 2 ? meta ^ 2 : meta ^ 3;
            else if (piece.coordMode == 2) meta ^= 1;
            else if (piece.coordMode == 3) meta = (meta & 3) < 2 ? meta ^ 3 : meta ^ 2;
            return meta;
        }
        int buttonMeta(int meta) {
            if (piece.coordMode == 1) return meta <= 2 ? meta + 2 : meta < 4 ? meta - 1 : meta - 3;
            if (piece.coordMode == 2) return meta + (meta % 2 == 0 ? -1 : 1);
            if (piece.coordMode == 3) return meta <= 1 ? meta + 3 : meta <= 2 ? meta + 1 : meta - 2;
            return meta;
        }
        int tripwire(int meta) {
            if (piece.coordMode == 1) return jmod(meta + 1, 4);
            if (piece.coordMode == 2) return meta ^ 2;
            if (piece.coordMode == 3) return jmod(meta - 1, 4);
            return meta;
        }
        int worldX(int x, int z) { return switch (piece.coordMode) { case 1 -> box.maxX - z; case 2 -> box.maxX - x; case 3 -> box.minX + z; default -> box.minX + x; }; }
        int worldZ(int x, int z) { return switch (piece.coordMode) { case 1 -> box.minZ + x; case 2 -> box.maxZ - z; case 3 -> box.maxZ - x; default -> box.minZ + z; }; }
        static int fail(int meta) { throw new IllegalStateException("deco meta " + meta); }
        static int jmod(int value, int bound) { return value - bound * (value / bound); }
    }
}
