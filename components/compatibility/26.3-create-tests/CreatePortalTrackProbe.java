package valen.createprobe;

import com.zurrtum.create.api.contraption.train.PortalTrackProvider;
import com.zurrtum.create.catnip.math.BlockFace;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.betterx.betterend.portal.PortalBuilder;

/** Disposable-world check of the actual author portal and Create's exit resolver; not a train traversal test. */
public final class CreatePortalTrackProbe {
    private static final BlockPos BASE = new BlockPos(160, 200, 144);

    public static Map<String, Object> run(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        ServerLevel end = server.getLevel(Level.END);
        if (end == null) throw new AssertionError("End dimension absent");
        var portal = BuiltInRegistries.BLOCK.getValue(Identifier.parse("betterend:end_portal_block"));
        if (portal == null || portal == Blocks.AIR) throw new AssertionError("BetterEnd portal absent");
        if (!PortalTrackProvider.isSupportedPortal(portal.defaultBlockState()))
            throw new AssertionError("BetterEnd portal provider absent");
        if (!PortalTrackProvider.isSupportedPortal(Blocks.NETHER_PORTAL.defaultBlockState())
            || PortalTrackProvider.isSupportedPortal(Blocks.STONE.defaultBlockState()))
            throw new AssertionError("Portal admission changed for Nether or stone");

        // Use BetterEnd's own valid frame/interior placement, with perpendicular exit axes.
        PortalBuilder.generatePortal(overworld, BASE, Direction.Axis.X, 0);
        PortalBuilder.generatePortal(end, BASE, Direction.Axis.Z, 0);
        var first = PortalBuilder.PORTAL_POSITIONS.stream()
            .min(java.util.Comparator.<java.awt.Point>comparingInt(p -> p.y)
                .thenComparingInt(p -> Math.abs(p.x))).orElseThrow();
        BlockPos source = BASE.relative(Direction.EAST, first.x).above(first.y);
        BlockFace inbound = new BlockFace(source.south(), Direction.NORTH);
        var outbound = PortalTrackProvider.getOtherSide(overworld, inbound);
        requireExit(outbound, end, portal, Direction.Axis.X);

        var returned = PortalTrackProvider.getOtherSide(end, outbound.face());
        requireExit(returned, overworld, portal, Direction.Axis.Z);
        var result = new LinkedHashMap<String, Object>();
        result.put("test", "BetterEnd actual portal track exits resolve in both directions");
        result.put("pass", true);
        result.put("source_portal", source.toShortString());
        result.put("end_exit", outbound.face().getPos().toShortString());
        result.put("return_exit", returned.face().getPos().toShortString());
        result.put("opposite_portal_axes", true);
        result.put("author_portal_search_radius", PortalBuilder.SPIRAL_SEARCH_RADIUS);
        result.put("scope", "native PortalTrackProvider resolution, not moving train traversal");
        return result;
    }

    private static void requireExit(PortalTrackProvider.Exit exit, ServerLevel expected,
                                    net.minecraft.world.level.block.Block portal, Direction.Axis normal) {
        if (exit == null || exit.level() != expected) throw new AssertionError("Portal exit dimension missing/wrong");
        var state = expected.getBlockState(exit.face().getConnectedPos());
        if (!state.is(portal) || exit.face().getFace().getAxis() != normal
            || state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == normal)
            throw new AssertionError("Portal exit is not on the correct face");
    }
}
