package ttv.migami.jeg.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Inert authored Combined Arms helipad marker; structural appearance only.
 * Source: NeoAlive/tacz-simple-enemies-vehicular-warfare
 * 662f28a92896e02a3aec336edd94ea4eb3f5d0fd HelipadBlock.java.
 * Retains source nine part values, 0.5/16 height and center-only source H model.
 * No block entity, GUI, registry/traffic/clearance, NPC or vehicle behavior.
 */
public final class HelipadMarkerBlock extends Block {
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    private static final VoxelShape PAD_SHAPE=Block.box(0,0,0,16,0.5,16);
    public HelipadMarkerBlock(BlockBehaviour.Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(PART,Part.CENTER));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(PART);}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return PAD_SHAPE;}
    @Override public RenderShape getRenderShape(BlockState state){return state.getValue(PART)==Part.CENTER?RenderShape.MODEL:RenderShape.INVISIBLE;}
    public enum Part implements StringRepresentable {
        NW("nw"),N("n"),NE("ne"),W("w"),CENTER("center"),E("e"),SW("sw"),S("s"),SE("se");
        private final String serialized;Part(String serialized){this.serialized=serialized;}
        @Override public String getSerializedName(){return serialized;}
    }
}
