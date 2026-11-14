package earth.terrarium.adastra.common.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import earth.terrarium.adastra.AdAstra;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import earth.terrarium.adastra.common.handlers.base.SpaceStation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class SpaceStationHandler extends SavedData {

    private final Map<UUID, Set<SpaceStation>> spaceStationData = new HashMap<>();

    public void loadData(CompoundTag tag) {
        tag.getAllKeys().forEach(id -> {
            ListTag stationsTag = tag.getList(id, Tag.TAG_COMPOUND);
            Set<SpaceStation> stations = new HashSet<>();
            stationsTag.forEach(stationTag -> {
                CompoundTag stationCompoundTag = (CompoundTag) stationTag;
                Component name = Component.Serializer.fromJson(stationCompoundTag.getString("Name"), RegistryAccess.EMPTY);
                ChunkPos position = new ChunkPos(stationCompoundTag.getLong("Position"));
                stations.add(new SpaceStation(position, name));
            });
            spaceStationData.put(UUID.fromString(id), stations);
        });
    }

    public void saveData(CompoundTag tag) {
        spaceStationData.forEach((id, stations) -> {
            ListTag ownerTag = new ListTag();
            for (var station : stations) {
                CompoundTag stationsTag = new CompoundTag();
                stationsTag.putString("Name", Component.Serializer.toJson(station.name(), RegistryAccess.EMPTY));
                stationsTag.putLong("Position", station.position().toLong());
                ownerTag.add(stationsTag);
            }
            tag.put(id.toString(), ownerTag);
        });
    }

    public static final Codec<SpaceStationHandler> CODEC = Codec.PASSTHROUGH.xmap(
        dynamic -> {
            SpaceStationHandler handler = new SpaceStationHandler();
            Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
            if (tag instanceof CompoundTag compound) handler.loadData(compound);
            return handler;
        },
        handler -> {
            CompoundTag tag = new CompoundTag();
            handler.saveData(tag);
            return new Dynamic<>(NbtOps.INSTANCE, tag);
        }
    );

    public static final SavedDataType<SpaceStationHandler> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "adastra_space_station_data"),
        SpaceStationHandler::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    public static SpaceStationHandler read(ServerLevel level) {
        SpaceStationHandler handler = level.getDataStorage().computeIfAbsent(TYPE);
        handler.attach(level);
        return handler;
    }

    public void attach(ServerLevel level) {
    }

    public static Map<UUID, Set<SpaceStation>> getAllSpaceStations(ServerLevel level) {
        return read(level).spaceStationData;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public static void constructSpaceStation(ServerPlayer player, ServerLevel level, Component name) {
        var data = read(level).spaceStationData;
        var stations = data.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        stations.add(new SpaceStation(player.chunkPosition(), name));
    }

    public static boolean isInSpaceStation(ServerPlayer player, ServerLevel level) {
        for (var stations : read(level).spaceStationData.values()) {
            for (var station : stations) {
                if (station.position().getChessboardDistance(player.chunkPosition()) <= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<SpaceStation> getOwnedSpaceStations(ServerPlayer player, ServerLevel level) {
        return getOwnedSpaceStations(player.getUUID(), level);
    }

    public static Set<SpaceStation> getOwnedSpaceStations(UUID id, ServerLevel level) {
        return read(level).spaceStationData.getOrDefault(id, Set.of());
    }
}
