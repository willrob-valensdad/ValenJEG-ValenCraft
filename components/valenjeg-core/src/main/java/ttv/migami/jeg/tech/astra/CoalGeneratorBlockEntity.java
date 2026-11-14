package ttv.migami.jeg.tech.astra;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

/**
 * Ports the MIT donor's two slots (0 battery, 1 fuel), generation, charged battery,
 * and adjacent output into Fabric 26.3's native inventory and transaction APIs.
 */
public final class CoalGeneratorBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int BATTERY = 0, FUEL = 1;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final CoalGeneratorPower power = new CoalGeneratorPower(this::setChanged);
    private final ContainerStorage inventory = ContainerStorage.of(this, null);

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) { super(AstraTechContent.COAL_GENERATOR_ENTITY, pos, state); }
    public CoalGeneratorPower power() { return power; }

    static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof BucketItem) && stack.has(DataComponents.COOKING_FUEL);
    }

    int fuelTicks() {
        ItemStack fuel = items.get(FUEL);
        if (!isFuel(fuel) || !(level instanceof ServerLevel server)) return 0;
        var params = new LootParams.Builder(server)
            .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
            .withParameter(LootContextParams.BLOCK_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .withParameter(LootContextParams.CONTAINER, this)
            .create(LootContextParamSets.CONTAINER_PROCESS);
        var context = new LootContext.Builder(params).create(java.util.Optional.empty());
        return ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL,
            net.minecraft.world.item.component.CookingFuel::burnTime, context, 0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoalGeneratorBlockEntity generator) {
        boolean lit = generator.power.tick(generator::fuelTicks, () -> generator.items.get(FUEL).shrink(1));
        generator.chargeBattery();
        generator.pushEnergy();
        if (state.getValue(CoalGeneratorBlock.LIT) != lit)
            level.setBlock(pos, state.setValue(CoalGeneratorBlock.LIT, lit), 3);
    }

    /** Context writes the charged stack back into the real native inventory atomically. */
    long chargeBattery() {
        if (items.get(BATTERY).isEmpty() || power.getAmount() == 0) return 0;
        var target = ContainerItemContext.ofSingleSlot(inventory.getSlot(BATTERY)).find(EnergyStorage.ITEM);
        if (target == null) return 0;
        try (var transaction = Transaction.openOuter()) {
            long moved = EnergyStorageUtil.move(power.output(), target, CoalGeneratorPower.TRANSFER, transaction);
            transaction.commit();
            return moved;
        }
    }

    private void pushEnergy() {
        if (level == null || power.getAmount() == 0) return;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            // Match the author's six-sided nearby lookup without forcing remote chunk loads.
            if (!level.hasChunkAt(targetPos)) continue;
            EnergyStorage target = EnergyStorage.SIDED.find(level, targetPos, direction.getOpposite());
            if (target == null) continue;
            try (var transaction = Transaction.openOuter()) {
                EnergyStorageUtil.move(power.output(), target, CoalGeneratorPower.TRANSFER, transaction);
                transaction.commit();
            }
        }
    }

    /** Right click: insert a battery or fuel. Invalid input and full slots leave the hand intact. */
    public void insertHeld(ItemStack held) {
        int slot = EnergyStorageUtil.isEnergyStorage(held) ? BATTERY : isFuel(held) ? FUEL : -1;
        if (slot < 0) return;
        ItemStack existing = items.get(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, held)) return;
        int limit = slot == BATTERY ? 1 : held.getMaxStackSize();
        int moved = Math.min(held.getCount(), limit - existing.getCount());
        if (moved <= 0) return;
        if (existing.isEmpty()) items.set(slot, held.copyWithCount(moved));
        else existing.grow(moved);
        held.shrink(moved);
        setChanged();
    }

    /** Sneak empty-hand use retrieves both real slots; overflow follows native player drops. */
    public void returnContents(Player player) {
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = removeItemNoUpdate(slot);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        setChanged();
    }

    public void describe(Player player) {
        player.sendOverlayMessage(Component.literal("Coal Generator: " + power.getAmount() + "/" + power.getCapacity()
            + " E | burn " + power.burnRemaining() + "/" + power.burnTotal()
            + " | fuel " + items.get(FUEL).getCount() + " | battery " + (items.get(BATTERY).isEmpty() ? "empty" : items.get(BATTERY).getHoverName().getString())));
        player.sendSystemMessage(Component.literal("Coal Generator: right-click fuel/battery to insert; empty hand to inspect; sneak + empty hand to retrieve. Output: all six sides, 100 E/transfer."));
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(slot == BATTERY ? 1 : getMaxStackSize(stack));
        setChanged();
    }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == FUEL ? isFuel(stack) : slot == BATTERY && EnergyStorageUtil.isEnergyStorage(stack);
    }
    // The original donor exposes only its fuel slot to sided item automation.
    @Override public int[] getSlotsForFace(Direction side) { return new int[]{FUEL}; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == FUEL && isFuel(stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == FUEL; }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out.child("Inventory"), items);
        out.putLong("Energy", power.getAmount());
        out.putInt("CookTime", power.burnRemaining());
        out.putInt("CookTimeTotal", power.burnTotal());
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        clearContent();
        in.child("Inventory").ifPresent(child -> ContainerHelper.loadAllItems(child, items));
        power.restore(in.getLongOr("Energy", 0), in.getIntOr("CookTime", 0), in.getIntOr("CookTimeTotal", 0));
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
            clearContent();
        }
        super.preRemoveSideEffects(pos, state);
    }
}
