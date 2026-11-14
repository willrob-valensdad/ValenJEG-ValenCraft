package top.ribs.scguns.entity.raid;


import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RaidSaveData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Codec<RaidSaveData> CODEC = CompoundTag.CODEC.xmap(RaidSaveData::load, RaidSaveData::save);
    private static final SavedDataType<RaidSaveData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("scguns", "raid_save_data"), RaidSaveData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private final Map<UUID, ActiveRaidData> activeRaidData = new HashMap<>();
    private final Map<Identifier, ScheduledRaidData> scheduledRaids = new HashMap<>();
    private final Map<Identifier, Long> lastRaidDayByDimension = new HashMap<>();

    public static RaidSaveData get(ServerLevel level) {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag activeRaidsList = new ListTag();
        for (ActiveRaidData data : this.activeRaidData.values()) {
            activeRaidsList.add(data.save());
        }
        tag.put("ActiveRaids", activeRaidsList);

        ListTag scheduledRaidsList = new ListTag();
        for (ScheduledRaidData data : this.scheduledRaids.values()) {
            scheduledRaidsList.add(data.save());
        }
        tag.put("ScheduledRaids", scheduledRaidsList);

        CompoundTag lastRaidDays = new CompoundTag();
        for (Map.Entry<Identifier, Long> entry : this.lastRaidDayByDimension.entrySet()) {
            lastRaidDays.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("LastRaidDays", lastRaidDays);
        return tag;
    }

    private static RaidSaveData load(CompoundTag tag) {
        RaidSaveData data = new RaidSaveData();
        if (tag.contains("ActiveRaids")) {
            ListTag list = tag.getList("ActiveRaids").orElseGet(ListTag::new);
            for (int i = 0; i < list.size(); i++) {
                ActiveRaidData raidData = ActiveRaidData.load(list.getCompoundOrEmpty(i));
                if (raidData != null) {
                    data.activeRaidData.put(raidData.raidId(), raidData);
                }
            }
        }
        if (tag.contains("ScheduledRaids")) {
            ListTag list = tag.getList("ScheduledRaids").orElseGet(ListTag::new);
            for (int i = 0; i < list.size(); i++) {
                ScheduledRaidData raidData = ScheduledRaidData.load(list.getCompoundOrEmpty(i));
                if (raidData != null) {
                    data.scheduledRaids.put(raidData.dimension(), raidData);
                }
            }
        }
        if (tag.contains("LastRaidDays")) {
            CompoundTag lastRaidDays = tag.getCompoundOrEmpty("LastRaidDays");
            for (String key : lastRaidDays.keySet()) {
                try {
                    data.lastRaidDayByDimension.put(Identifier.parse(key), lastRaidDays.getLong(key).orElse(0L));
                } catch (Exception e) {
                    LOGGER.warn("Failed to load Scorched Guns last raid day for dimension {}", key);
                }
            }
        }
        return data;
    }

    public void saveActiveRaid(ActiveRaid raid) {
        this.activeRaidData.put(raid.getRaidId(), new ActiveRaidData(
                raid.getRaidId(),
                raid.getConfig().raidId(),
                raid.getRaidLevel(),
                raid.getSpawnCenter(),
                raid.getStartTime(),
                raid.getBossUUID(),
                raid.getMountUUID(),
                raid.getTargetPlayerUUID(),
                raid.getHenchmenUUIDs(),
                raid.getSpawnTimer(),
                raid.getTotalHenchmenSpawned(),
                raid.isActive(),
                raid.isBossConfirmed()
        ));
        this.setDirty();
    }

    public void removeActiveRaid(UUID raidId) {
        if (this.activeRaidData.remove(raidId) != null) {
            this.setDirty();
        }
    }

    public Collection<ActiveRaidData> getActiveRaidData() {
        return new ArrayList<>(this.activeRaidData.values());
    }

    public void scheduleRaid(Identifier dimension, ServerPlayer player, String raidId, long day) {
        this.scheduledRaids.put(dimension, new ScheduledRaidData(dimension, player.getUUID(), raidId, day));
        this.setDirty();
    }

    @Nullable
    public ScheduledRaidData getScheduledRaid(Identifier dimension) {
        return this.scheduledRaids.get(dimension);
    }

    public void removeScheduledRaid(Identifier dimension) {
        if (this.scheduledRaids.remove(dimension) != null) {
            this.setDirty();
        }
    }

    public void setLastRaidDay(Identifier dimension, long day) {
        this.lastRaidDayByDimension.put(dimension, day);
        this.setDirty();
    }

    public long getLastRaidDay(Identifier dimension) {
        return this.lastRaidDayByDimension.getOrDefault(dimension, -1000L);
    }

    public boolean canScheduleRaid(Identifier dimension, long currentDay, int minDaysBetween) {
        return currentDay - getLastRaidDay(dimension) >= minDaysBetween;
    }

    public void cleanupInvalidRaids(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveRaidData>> iterator = this.activeRaidData.entrySet().iterator();
        boolean dirty = false;
        while (iterator.hasNext()) {
            ActiveRaidData data = iterator.next().getValue();
            if (!data.isActive() || data.bossConfirmed() && data.bossUUID() != null && level.getEntity(data.bossUUID()) == null) {
                iterator.remove();
                dirty = true;
            }
        }
        if (dirty) {
            this.setDirty();
        }
    }

    public record ActiveRaidData(UUID raidId, String configRaidId, @Nullable Integer raidLevel, Vec3 spawnCenter,
                                 long startTime, @Nullable UUID bossUUID, @Nullable UUID mountUUID,
                                 @Nullable UUID targetPlayerUUID, Set<UUID> henchmenUUIDs, int spawnTimer,
                                 int totalSpawned, boolean isActive, boolean bossConfirmed) {
        public ActiveRaidData {
            henchmenUUIDs = new HashSet<>(henchmenUUIDs);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.store("RaidId", UUIDUtil.CODEC, this.raidId);
            tag.putString("ConfigRaidId", this.configRaidId);
            if (this.raidLevel != null) {
                tag.putInt("RaidLevel", this.raidLevel);
            }
            tag.putDouble("SpawnX", this.spawnCenter.x);
            tag.putDouble("SpawnY", this.spawnCenter.y);
            tag.putDouble("SpawnZ", this.spawnCenter.z);
            tag.putLong("StartTime", this.startTime);
            if (this.bossUUID != null) tag.store("BossUUID", UUIDUtil.CODEC, this.bossUUID);
            if (this.mountUUID != null) tag.store("MountUUID", UUIDUtil.CODEC, this.mountUUID);
            if (this.targetPlayerUUID != null) tag.store("TargetPlayerUUID", UUIDUtil.CODEC, this.targetPlayerUUID);
            ListTag henchmen = new ListTag();
            for (UUID uuid : this.henchmenUUIDs) {
                CompoundTag henchmanTag = new CompoundTag();
                henchmanTag.store("UUID", UUIDUtil.CODEC, uuid);
                henchmen.add(henchmanTag);
            }
            tag.put("Henchmen", henchmen);
            tag.putInt("SpawnTimer", this.spawnTimer);
            tag.putInt("TotalSpawned", this.totalSpawned);
            tag.putBoolean("IsActive", this.isActive);
            tag.putBoolean("BossConfirmed", this.bossConfirmed);
            return tag;
        }

        @Nullable
        public static ActiveRaidData load(CompoundTag tag) {
            try {
                UUID raidId = tag.read("RaidId", UUIDUtil.CODEC).orElseThrow();
                String configRaidId = tag.getStringOr("ConfigRaidId", "");
                Integer raidLevel = tag.contains("RaidLevel") ? tag.getIntOr("RaidLevel", 0) : null;
                Vec3 spawnCenter = new Vec3(tag.getDoubleOr("SpawnX", 0.0D), tag.getDoubleOr("SpawnY", 0.0D), tag.getDoubleOr("SpawnZ", 0.0D));
                UUID bossUUID = tag.read("BossUUID", UUIDUtil.CODEC).isPresent() ? tag.read("BossUUID", UUIDUtil.CODEC).orElseThrow() : null;
                UUID mountUUID = tag.read("MountUUID", UUIDUtil.CODEC).isPresent() ? tag.read("MountUUID", UUIDUtil.CODEC).orElseThrow() : null;
                UUID targetPlayerUUID = tag.read("TargetPlayerUUID", UUIDUtil.CODEC).isPresent() ? tag.read("TargetPlayerUUID", UUIDUtil.CODEC).orElseThrow() : null;
                Set<UUID> henchmen = new HashSet<>();
                ListTag henchmenList = tag.getList("Henchmen").orElseGet(ListTag::new);
                for (int i = 0; i < henchmenList.size(); i++) {
                    CompoundTag henchmanTag = henchmenList.getCompoundOrEmpty(i);
                    if (henchmanTag.read("UUID", UUIDUtil.CODEC).isPresent()) {
                        henchmen.add(henchmanTag.read("UUID", UUIDUtil.CODEC).orElseThrow());
                    }
                }
                return new ActiveRaidData(raidId, configRaidId, raidLevel, spawnCenter, tag.getLongOr("StartTime", 0L), bossUUID, mountUUID,
                        targetPlayerUUID, henchmen, tag.getIntOr("SpawnTimer", 0), tag.getIntOr("TotalSpawned", 0),
                        tag.getBooleanOr("IsActive", false), tag.getBooleanOr("BossConfirmed", false));
            } catch (Exception e) {
                LOGGER.warn("Failed to load Scorched Guns active raid data: {}", e.getMessage());
                return null;
            }
        }
    }

    public record ScheduledRaidData(Identifier dimension, UUID targetPlayerUUID, String raidId, long scheduledDay) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", this.dimension.toString());
            tag.store("PlayerUUID", UUIDUtil.CODEC, this.targetPlayerUUID);
            tag.putString("RaidId", this.raidId);
            tag.putLong("ScheduledDay", this.scheduledDay);
            return tag;
        }

        @Nullable
        public static ScheduledRaidData load(CompoundTag tag) {
            try {
                return new ScheduledRaidData(Identifier.parse(tag.getStringOr("Dimension", "")), tag.read("PlayerUUID", UUIDUtil.CODEC).orElseThrow(),
                        tag.getStringOr("RaidId", ""), tag.getLongOr("ScheduledDay", 0L));
            } catch (Exception e) {
                LOGGER.warn("Failed to load Scorched Guns scheduled raid data: {}", e.getMessage());
                return null;
            }
        }
    }
}
