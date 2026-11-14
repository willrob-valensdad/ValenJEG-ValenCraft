package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.block.AmmoBoxBlock;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
public class AmmoBoxBlockEntity extends RandomizableContainerBlockEntity {
    private static final String STRUCTURE_LOOT_BOX_TAG = "ScgunsStructureLootBox";
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter;
    private boolean structureLootBox;

    public AmmoBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AMMO_BOX.get(), pPos, pBlockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY); // 27 slots like a chest
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level pLevel, BlockPos pPos, BlockState pState) {
                AmmoBoxBlockEntity.this.playSound(pState, SoundEvents.CHEST_OPEN);
            }

            protected void onClose(Level pLevel, BlockPos pPos, BlockState pState) {
                AmmoBoxBlockEntity.this.playSound(pState, SoundEvents.CHEST_CLOSE);
            }

            protected void openerCountChanged(Level pLevel, BlockPos pPos, BlockState pState, int pOpenerCount, int pNewOpeners) {}

            public boolean isOwnContainer(Player pPlayer) {
                return pPlayer.containerMenu instanceof ChestMenu && ((ChestMenu) pPlayer.containerMenu).getContainer() == AmmoBoxBlockEntity.this;
            }
        };
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.ammo_box");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (!this.canOpen(player)) {
            return null;
        }
        this.unpackStructureLootTable(player);
        return this.createMenu(containerId, playerInventory);
    }

    @Override
    public int getContainerSize() {
        return 27; // 27 slots like a regular chest
    }

    @Override
    public void startOpen(ContainerUser containerUser) {
        LivingEntity pPlayer = containerUser.getLivingEntity();
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState(), containerUser.getContainerInteractionRange());
            BlockState state = this.getBlockState();
            if (!state.getValue(AmmoBoxBlock.OPEN)) {
                this.getLevel().setBlock(this.worldPosition, state.setValue(AmmoBoxBlock.OPEN, true), 3);
            }
        }
    }

    @Override
    public void stopOpen(ContainerUser containerUser) {
        LivingEntity pPlayer = containerUser.getLivingEntity();
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
            BlockState state = this.getBlockState();
            if (state.getValue(AmmoBoxBlock.OPEN)) {
                this.getLevel().setBlock(this.worldPosition, state.setValue(AmmoBoxBlock.OPEN, false), 3);
            }
        }
    }


    void playSound(BlockState state, SoundEvent sound) {
        this.level.playSound(null, this.worldPosition, sound, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    public void prepareForRemovalDrops() {
        this.unpackStructureLootTable(null);
    }

    public boolean shouldSuppressBlockDrop() {
        return this.structureLootBox || this.getLootTable() != null;
    }

    private void unpackStructureLootTable(@Nullable Player player) {
        if (this.getLootTable() != null) {
            this.structureLootBox = true;
        }
        this.unpackLootTable(player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.structureLootBox = input.getBooleanOr(STRUCTURE_LOOT_BOX_TAG, false);
        if (this.tryLoadLootTable(input)) {
            this.structureLootBox = true;
        } else {
            ContainerHelper.loadAllItems(input, this.items);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }
        if (this.structureLootBox) {
            output.putBoolean(STRUCTURE_LOOT_BOX_TAG, true);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.prepareForRemovalDrops();
        super.preRemoveSideEffects(pos, state);
    }
}
