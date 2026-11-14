package top.ribs.scguns.blockentity;



import top.ribs.scguns.util.ScgunsNbtCompat;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.client.screen.GunBenchMenu;
import top.ribs.scguns.init.ModBlockEntities;


public class GunBenchBlockEntity extends BlockEntity implements MenuProvider {
    private final SimpleContainer inventory = new SimpleContainer(12) {
        @Override
        public void setChanged() {
            super.setChanged();
            GunBenchBlockEntity.this.setChanged();  // Mark the block entity as dirty
        }
    };

    public GunBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUN_BENCH.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.gun_bench");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GunBenchMenu(id, playerInventory, this.inventory, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory.clearContent();

        NonNullList<ItemStack> items =
                NonNullList.withSize(this.inventory.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);

        boolean loadedModernItems = false;
        for (int i = 0; i < items.size(); i++) {
            if (i != GunBenchMenu.SLOT_OUTPUT && !items.get(i).isEmpty()) {
                this.inventory.setItem(i, items.get(i));
                loadedModernItems = true;
            }
        }

        if (!loadedModernItems) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (i == GunBenchMenu.SLOT_OUTPUT) {
                    continue;
                }
                ItemStack legacyStack = input.read("Item" + i, ItemStack.OPTIONAL_CODEC)
                        .orElse(ItemStack.EMPTY);
                if (!legacyStack.isEmpty()) {
                    this.inventory.setItem(i, legacyStack);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        NonNullList<ItemStack> items =
                NonNullList.withSize(this.inventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (i != GunBenchMenu.SLOT_OUTPUT) {
                items.set(i, this.inventory.getItem(i));
            }
        }
        ContainerHelper.saveAllItems(output, items);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public void dropContents(Player player) {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            if (i == GunBenchMenu.SLOT_OUTPUT) {
                continue;
            }
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                if (player != null) {
                    player.drop(itemstack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                } else {
                    this.level.addFreshEntity(new ItemEntity(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemstack));
                }
            }
        }
        this.inventory.clearContent();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        this.dropContents(null);
    }
}
