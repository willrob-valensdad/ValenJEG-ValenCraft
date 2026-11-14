package top.ribs.scguns.blockentity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;

import net.minecraft.world.Containers;
public class GunShelfBlockEntity extends BlockEntity {
    private ItemStack displayedItem = ItemStack.EMPTY;

    public GunShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUN_SHELF_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedItem = stack == null ? ItemStack.EMPTY : stack;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    public boolean isEmpty() {
        return displayedItem.isEmpty();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.displayedItem = input.read("DisplayedItem", ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.displayedItem.isEmpty()) {
            output.store("DisplayedItem", ItemStack.OPTIONAL_CODEC, this.displayedItem);
        }
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

    public ItemStack getItem(int i) {
        return i == 0 ? this.displayedItem : ItemStack.EMPTY;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.displayedItem.isEmpty()) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.displayedItem);
            this.displayedItem = ItemStack.EMPTY;
        }
    }
}
