package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.EntitySpawnReason;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
public class MobTrapBlockEntity extends BlockEntity {
    private final List<EntityType<?>> storedMobs = new ArrayList<>();
    private static final int MAX_MOBS = 20;
    private final int radius;
    private final int heightRadius;
    private int tickCounter;

    public MobTrapBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MOB_TRAP.get(), pPos, pBlockState);
        this.radius = 10;
        this.heightRadius = 3;
        this.tickCounter = 0;
    }

    public boolean addMob(EntityType<?> mobType) {
        if (storedMobs.size() < MAX_MOBS) {
            storedMobs.add(mobType);
            setChanged();
            return true;
        }
        return false;
    }

    public void releaseMobs(ServerLevel level, BlockPos pos) {
        if (!storedMobs.isEmpty()) {
            for (EntityType<?> mob : storedMobs) {
                Entity entity = mob.create(level, EntitySpawnReason.TRIGGERED);
                if (entity != null) {
                    entity.snapTo(pos.getX() + level.getRandom().nextDouble(), pos.getY(), pos.getZ() + level.getRandom().nextDouble(), level.getRandom().nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(entity);
                    for (int i = 0; i < 20; i++) {
                        level.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(), entity.getZ(), 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }
            storedMobs.clear();
            setChanged();
            level.destroyBlock(pos, true);
        }
    }

    public List<EntityType<?>> getStoredMobs() {
        return this.storedMobs;
    }


    public void serverTick(Level level, BlockPos pos) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;

                AABB detectionBox = new AABB(
                        pos.getX() - radius, pos.getY() - heightRadius, pos.getZ() - radius,
                        pos.getX() + radius, pos.getY() + heightRadius, pos.getZ() + radius
                );
                List<Player> players = serverLevel.getEntitiesOfClass(Player.class, detectionBox, player ->
                        !player.isCreative() && !player.isSpectator()
                );
                if (!players.isEmpty()) {
                    releaseMobs(serverLevel, pos);
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedMobs.clear();
        for (String mobId : input.read("StoredMobs", Codec.STRING.listOf()).orElse(List.of())) {
            BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(mobId))
                    .ifPresent(this.storedMobs::add);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<String> mobIds = new ArrayList<>(this.storedMobs.size());
        for (EntityType<?> mob : this.storedMobs) {
            mobIds.add(BuiltInRegistries.ENTITY_TYPE.getKey(mob).toString());
        }
        output.store("StoredMobs", Codec.STRING.listOf(), mobIds);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public void handleUpdateTag(ValueInput input) {
        this.loadCustomOnly(input);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(Connection net, ValueInput input) {
        this.loadAdditional(input);
    }
}
