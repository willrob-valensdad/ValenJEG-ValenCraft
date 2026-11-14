package ttv.migami.jeg.station;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
/** Original MrCrayfish/JEG station cuboids and rotations, ported from the pinned original block classes. */
final class StationShapes {
    private static final Map<String,Map<Direction,VoxelShape>> CACHE=new java.util.concurrent.ConcurrentHashMap<>();
    static VoxelShape get(String station,Direction facing) {
        return CACHE.computeIfAbsent(station,StationShapes::build).get(facing);
    }
    private static Map<Direction,VoxelShape> build(String station) {
        double[][] boxes=switch(station) {
            case "mo_workbench" -> new double[][]{{0,11,0,16,16,16},{0,0,0,2,11,2},{14,0,0,16,11,2},{14,0,14,16,11,16},{0,0,14,2,11,16}};
            case "scrap_workbench" -> new double[][] {{0.0,11.0,0.0,16.0,16.0,16.0},{14.0,3.0,2.0,16.0,5.0,14.0},{0.0,3.0,2.0,2.0,5.0,14.0},{0.0,0.0,0.0,2.0,11.0,2.0},{14.0,0.0,0.0,16.0,11.0,2.0},{14.0,0.0,14.0,16.0,11.0,16.0},{0.0,0.0,14.0,2.0,11.0,16.0},{2.0,3.0,0.0,14.0,5.0,16.0}};
            case "gunmetal_workbench" -> new double[][] {{0.0,11.0,0.0,16.0,16.0,16.0},{0.0,0.0,0.0,2.0,11.0,2.0},{14.0,0.0,0.0,16.0,11.0,2.0},{14.0,0.0,14.0,16.0,11.0,16.0},{0.0,0.0,14.0,2.0,11.0,16.0},{0.0,16.0,14.0,16.0,20.0,16.0},{0.0,16.0,8.0,2.0,20.0,14.0},{14.0,16.0,8.0,16.0,20.0,14.0},{3.0,16.0,9.0,13.0,21.0,14.0},{0.0,3.0,2.0,2.0,11.0,14.0},{14.0,3.0,2.0,16.0,11.0,14.0}};
            case "gunnite_workbench" -> new double[][] {{0.0,11.0,0.0,16.0,16.0,16.0},{0.0,0.0,0.0,2.0,11.0,2.0},{14.0,0.0,0.0,16.0,11.0,2.0},{14.0,0.0,14.0,16.0,11.0,16.0},{0.0,0.0,14.0,2.0,11.0,16.0},{2.0,3.0,0.0,14.0,5.0,2.0},{0.0,3.0,2.0,16.0,5.0,14.0},{2.0,3.0,14.0,14.0,11.0,16.0},{0.0,16.0,14.0,16.0,24.0,16.0}};
            case "blueprint_workbench" -> new double[][] {{0.0,11.0,0.0,16.0,16.0,16.0},{0.0,0.0,0.0,2.0,11.0,2.0},{14.0,0.0,0.0,16.0,11.0,2.0},{14.0,0.0,14.0,16.0,11.0,16.0},{0.0,0.0,14.0,2.0,11.0,16.0},{2.0,3.0,0.0,14.0,5.0,2.0},{0.0,3.0,2.0,16.0,5.0,14.0},{2.0,3.0,14.0,14.0,5.0,16.0},{0.0,16.0,14.0,16.0,24.0,16.0}};
            case "schematic_station" -> new double[][] {{0.0,11.0,0.0,16.0,16.0,5.0},{0.0,13.0,5.0,16.0,18.0,10.0},{0.0,15.0,10.0,16.0,20.0,15.0},{0.0,0.0,0.0,2.0,11.0,2.0},{14.0,0.0,0.0,16.0,11.0,2.0},{14.0,0.0,14.0,16.0,17.0,16.0},{0.0,0.0,14.0,2.0,17.0,16.0},{2.0,3.0,0.0,14.0,5.0,2.0},{2.0,3.0,14.0,14.0,5.0,16.0},{0.0,16.0,14.0,16.0,24.0,16.0}};
            case "recycler" -> new double[][] {{2.0,9.0,2.0,14.0,14.0,14.0},{0.0,0.0,14.0,4.0,3.0,16.0},{0.0,0.0,12.0,2.0,3.0,14.0},{12.0,0.0,14.0,14.0,3.0,16.0},{14.0,0.0,12.0,16.0,3.0,16.0},{12.0,0.0,0.0,16.0,3.0,2.0},{14.0,0.0,2.0,16.0,3.0,4.0},{2.0,0.0,0.0,4.0,3.0,2.0},{0.0,0.0,0.0,2.0,3.0,4.0},{2.0,3.0,0.0,16.0,16.0,2.0},{0.0,3.0,14.0,14.0,16.0,16.0},{0.0,3.0,0.0,2.0,16.0,14.0},{14.0,3.0,2.0,16.0,16.0,16.0},{0.0,19.0,-3.0,16.0,23.0,-1.0},{0.0,16.0,-1.0,16.0,20.0,1.0},{0.0,19.0,17.0,16.0,23.0,19.0},{0.0,16.0,15.0,16.0,20.0,17.0},{15.0,16.0,0.0,17.0,20.0,16.0},{-1.0,16.0,0.0,1.0,20.0,16.0},{-3.0,19.0,0.0,-1.0,23.0,16.0},{17.0,19.0,0.0,19.0,23.0,16.0}};
            default -> throw new IllegalArgumentException(station);
        };
        Map<Direction,VoxelShape> result=new EnumMap<>(Direction.class);
        for(Direction facing:List.of(Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST)) {
            VoxelShape shape=Shapes.empty();
            int turns=station.equals("scrap_workbench")||station.equals("recycler") ?0 : switch(facing) {case EAST->1;case SOUTH->2;case WEST->3;default->0;};
            for(double[] box:boxes) {
                double x0=box[0],y0=box[1],z0=box[2],x1=box[3],y1=box[4],z1=box[5];
                for(int i=0;i<turns;i++) {double nextX0=16-z1,nextZ0=x0,nextX1=16-z0,nextZ1=x1;x0=nextX0;z0=nextZ0;x1=nextX1;z1=nextZ1;}
                shape=Shapes.joinUnoptimized(shape,Block.box(x0,y0,z0,x1,y1,z1),BooleanOp.OR);
            }
            result.put(facing,shape.optimize());
        }
        return result;
    }
}
