package valen.spacereloadedprobe;

import com.mojang.authlib.GameProfile;
import dev.valen.compat.energy.GravityBridge;
import dev.valen.compat.energy.mixin.NeroGravityInvoker;
import io.netty.channel.ChannelFutureListener;
import java.lang.reflect.Field;
import java.util.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.*;
import org.alex_melan.spacereloaded.planet.*;
import org.alex_melan.spacereloaded.registry.ModBlocks;
import org.alex_melan.spacereloaded.station.*;
import za.co.neroland.nerospace.world.gravity.GravityManager;

/** Test-only: native players with packet sink and a real hub whose mechanical state is set explicitly. */
final class SpaceGravityProbe {
    static final Identifier SPACE = Identifier.parse("spacereloaded:planet_gravity");
    static final Identifier NERO = Identifier.parse("nerospace:dimension_gravity");
    static final Identifier OTHER = Identifier.parse("valen_probe:unrelated_gravity");
    static final Vec3 POSITION = new Vec3(160.5, 232, 160.5);
    static final List<String> checks = new ArrayList<>();
    static final List<ServerPlayer> players = new ArrayList<>();
    static final Map<ServerLevel,BlockPos> hubs = new LinkedHashMap<>();
    static final Map<ServerPlayer,Integer> pendingAcks = new IdentityHashMap<>();

    static Map<String,Object> run(MinecraftServer server) throws Exception {
        checks.clear();
        var moon = level(server, "spacereloaded:moon");
        var nero = level(server, "nerospace:greenxertz");
        try {
            var player = player(server, server.overworld(), "GravityTravel", true);
            double crewBefore = CrewState.get(server).days(player.getUUID());
            seed(player);
            ServerPlayerEvents.JOIN.invoker().onJoin(player);
            expectAmbient(player, "join callback clears both stale modifiers");
            final int[] transitions = {0};
            ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((changed, oldLevel, newLevel) -> {
                if (changed == player) transitions[0]++;
            });
            travel(player, moon);
            expectAmbient(player, "native Overworld to Space transition refreshes immediately");
            travel(player, nero);
            expectAmbient(player, "native Space to Nero transition refreshes immediately");
            travel(player, moon);
            expectAmbient(player, "native Nero to Space transition refreshes immediately");
            require(transitions[0] == 3, "three native transition callbacks fired");

            // Existing Nero period must use the same ring hook, irrespective of which mod updated first.
            for (ServerLevel level : List.of(nero, moon)) {
                travel(player, level);
                ring(level, player);
                PlanetEffects.refreshGravity(player);
                NeroGravityInvoker.valen$apply(player, GravityManager.factorAt(level, player.blockPosition()));
                expectRing(player, "Space then Nero ring update in " + level.dimension().identifier());
                NeroGravityInvoker.valen$apply(player, 0.0);
                expectRing(player, "zero Nero factor does not cancel ring gravity in " + level.dimension().identifier());
                PlanetEffects.refreshGravity(player);
                expectRing(player, "Nero then Space ring update in " + level.dimension().identifier());
                require(level.getGameTime() % 10 == 0, "Nero actual periodic gate reached");
                GravityManager.tick(server);
                expectRing(player, "actual Nero periodic refresh preserves ring in " + level.dimension().identifier());
                player.setPos(POSITION.x + 30, POSITION.y, POSITION.z);
                GravityBridge.refresh(player);
                expectAmbient(player, "ring exit restores ambient in " + level.dimension().identifier());
                GravityManager.tick(server);
                expectAmbient(player, "periodic ambient remains restored in " + level.dimension().identifier());
            }
            var respawned = player(server, moon, "GravityRespawn", false);
            respawned.setPos(POSITION.x + 30, POSITION.y, POSITION.z);
            seed(respawned);
            ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(player, respawned, false);
            expectAmbient(respawned, "respawn callback replaces stale dimension modifiers");
            respawned.setPos(POSITION);
            ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(player, respawned, false);
            expectRing(respawned, "Space ring respawn retains ring-owned gravity");
            var joining = player(server, nero, "GravityJoin", false);
            joining.setPos(POSITION.x + 30, POSITION.y, POSITION.z);
            seed(joining);
            ServerPlayerEvents.JOIN.invoker().onJoin(joining);
            expectAmbient(joining, "Nero join callback replaces stale Space modifier");
            joining.setPos(POSITION);
            ServerPlayerEvents.JOIN.invoker().onJoin(joining);
            expectRing(joining, "Nero ring join retains ring-owned gravity");

            // Artificially seed nonzero exposure; refresh must not call the author's periodic crew update.
            player.setGameMode(GameType.SURVIVAL);
            CrewState.get(server).update(player, 0, 0.25);
            double seeded = CrewState.get(server).days(player.getUUID());
            for (int i=0;i<5;i++) GravityBridge.refresh(player);
            require(CrewState.get(server).days(player.getUUID()) == seeded && seeded > crewBefore,
                "pure refresh does not advance crew exposure");
            return Map.of("pass", true, "checks", List.copyOf(checks),
                "scope", "native teleports and actual periodic updater; Fabric join/respawn callbacks invoked with packet-sink native players; ring mechanical state is test-set, not construction acceptance");
        } finally {
            for (var player : players) if (!player.isRemoved())
                player.level().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            players.clear();
            for (var entry : hubs.entrySet()) entry.getKey().removeBlock(entry.getValue(), false);
            hubs.clear();
        }
    }
    static ServerLevel level(MinecraftServer server, String id) {
        return Objects.requireNonNull(server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(id))), id);
    }
    static ServerPlayer player(MinecraftServer server, ServerLevel level, String name, boolean add) {
        var profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)), name);
        var player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        var connection = new Connection(PacketFlow.SERVERBOUND) {
            @Override public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
                if (packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket position) pendingAcks.put(player, position.id());
                if (name.equals("SpaceRocket") && packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat) System.out.println("SPACE_ROCKET_MESSAGE " + chat.content().getString());
            }
        };
        new ServerGamePacketListenerImpl(server, connection, player, CommonListenerCookie.createInitial(profile, false));
        player.setPos(POSITION);
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.getAttribute(Attributes.GRAVITY).addTransientModifier(
            new AttributeModifier(OTHER, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (add) level.addNewPlayer(player);
        players.add(player);
        return player;
    }
    static void acknowledgeLoaded(ServerPlayer player) {
        Integer id = pendingAcks.remove(player);
        if (id != null) player.connection.handleAcceptTeleportPacket(new net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket(
            id, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
        player.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
    }
    static void seed(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.GRAVITY);
        attribute.addOrUpdateTransientModifier(new AttributeModifier(SPACE, -0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        attribute.addOrUpdateTransientModifier(new AttributeModifier(NERO, -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    static void travel(ServerPlayer player, ServerLevel level) {
        require(player.teleport(new TeleportTransition(level, POSITION, Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING)) == player,
            "native teleport accepted " + level.dimension().identifier());
    }
    static void ring(ServerLevel level, ServerPlayer player) throws Exception {
        BlockPos pos = new BlockPos(160, 240, 160);
        level.setBlockAndUpdate(pos, ModBlocks.SPIN_HUB.defaultBlockState());
        var hub = (SpinHubBlockEntity) level.getBlockEntity(pos);
        field(hub, "isolated", true);
        field(hub, "dirty", false);
        field(hub, "bounds", new AABB(158, 228, 158, 164, 242, 164));
        field(hub, "omega", Math.sqrt(3.7 / (pos.getY() + 0.5 - player.getY())));
        SpinRings.register(level, pos);
        hubs.put(level, pos);
        require(SpinRings.gravityFor(level, player).isPresent(), "actual ring selected " + level.dimension().identifier());
    }
    static void field(Object object, String name, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object, value);
    }
    static void expectAmbient(ServerPlayer player, String label) {
        double factor = PlanetManager.gravity(player.level()) / PlanetManager.EARTH_GRAVITY
            * GravityManager.factorAt(player.level(), player.blockPosition());
        expect(player, factor, label);
    }
    static void expectRing(ServerPlayer player, String label) {
        expect(player, 3.7 / PlanetManager.EARTH_GRAVITY, label);
        require(player.getAttribute(Attributes.GRAVITY).getModifier(NERO) == null, label + " removes only Nero ambient");
    }
    static void expect(ServerPlayer player, double factor, String label) {
        var attribute = player.getAttribute(Attributes.GRAVITY);
        double expected = attribute.getBaseValue() * factor * 1.25;
        require(Math.abs(attribute.getValue() - expected) < 1e-9,
            label + " gravity " + attribute.getValue() + " expected " + expected);
        require(attribute.getModifier(OTHER) != null, label + " retains unrelated modifier");
    }
    static void require(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks.add(label);
    }
}
